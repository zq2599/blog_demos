package main

import (
	"fmt"
	"time"

	"k8s.io/klog/v2"

	"k8s.io/apimachinery/pkg/api/meta"
	"k8s.io/apimachinery/pkg/fields"
	objectruntime "k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/util/runtime"
	"k8s.io/apimachinery/pkg/util/wait"
	"k8s.io/client-go/tools/cache"
	"k8s.io/client-go/util/workqueue"
)

// 自定义controller数据结构，嵌入了真实的控制器
type Controller struct {
	// 本地缓存，关注的对象都会同步到这里
	indexer cache.Indexer
	// 消息队列，用来触发对真实对象的处理事件
	queue workqueue.RateLimitingInterface
	// 实际运行运行的控制器
	informer cache.Controller
}

// processNextItem 不间断从队列中取得数据并处理
func (c *Controller) processNextItem() bool {
	// 注意，队列里面不是对象，而是key，这是个阻塞队列，会一直等待
	key, quit := c.queue.Get()
	if quit {
		return false
	}
	// Tell the queue that we are done with processing this key. This unblocks the key for other workers
	// This allows safe parallel processing because two pods with the same key are never processed in
	// parallel.
	defer c.queue.Done(key)

	// 注意，这里的syncToStdout应该是业务代码，处理对象变化的事件
	err := c.syncToStdout(key.(string))

	// 如果前面的业务逻辑遇到了错误，就在此处理
	c.handleErr(err, key)

	// 外面的调用逻辑是：返回true就继续调用processNextItem方法
	return true
}

// runWorker 这是个无限循环，不断地从队列取出数据处理
func (c *Controller) runWorker() {
	for c.processNextItem() {
	}
}

// syncToStdout 这是业务逻辑代码，被调用意味着key对应的对象有变化(新增或者修改)
func (c *Controller) syncToStdout(key string) error {
	// 从本地缓存中取出完整的对象
	obj, exists, err := c.indexer.GetByKey(key)
	if err != nil {
		klog.Errorf("Fetching object with key %s from store failed with %v", key, err)
		return err
	}

	// 如果不存在，就表示这是个删除事件
	if !exists {
		fmt.Printf("Pod %s does not exist anymore\n", key)
	} else {
		// 这里无视了obj具体是什么类型的对象(deployment、pod这些都有可能)，
		// 用meta.Accessor转换出metav1.Object对象后就能获取该对象的所有meta信息
		objMeta, err := meta.Accessor(obj)

		if err != nil {
			klog.Errorf("get meta accessor error, [%s], failed with %v", key, err)
			return err
		}

		// 取得资源的所有属性
		labels := objMeta.GetLabels()

		if labels == nil {
			klog.Infof("name [%s], namespace [%s], label is empty", objMeta.GetName(), objMeta.GetNamespace())
			return nil
		}

		// 遍历每个属性，打印出来
		for key, value := range labels {
			klog.Infof("name [%s], namespace [%s], key [%s], value [%s]",
				objMeta.GetName(),
				objMeta.GetNamespace(),
				key,
				value)
		}
	}
	return nil
}

// handleErr 如果前面的业务逻辑执行出现错误，就在此集中处理错误，本例中主要是重试次数的控制
func (c *Controller) handleErr(err error, key interface{}) {
	if err == nil {
		// Forget about the #AddRateLimited history of the key on every successful synchronization.
		// This ensures that future processing of updates for this key is not delayed because of
		// an outdated error history.
		c.queue.Forget(key)
		return
	}

	// 如果重试次数未超过5次，就继续重试
	if c.queue.NumRequeues(key) < 5 {
		klog.Infof("Error syncing pod %v: %v", key, err)

		// Re-enqueue the key rate limited. Based on the rate limiter on the
		// queue and the re-enqueue history, the key will be processed later again.
		c.queue.AddRateLimited(key)
		return
	}

	// 代码走到这里，意味着有错误并且重试超过了5次，应该立即丢弃
	c.queue.Forget(key)
	// 这种连续五次重试还未成功的错误，交给全局处理逻辑
	runtime.HandleError(err)
	klog.Infof("Dropping pod %q out of the queue: %v", key, err)
}

// Run 开始常规的控制器模式（持续响应资源变化事件）
func (c *Controller) Run(threadiness int, stopCh chan struct{}) {
	defer runtime.HandleCrash()

	// Let the workers stop when we are done
	defer c.queue.ShutDown()
	klog.Info("Starting Pod controller")

	go c.informer.Run(stopCh)

	// Wait for all involved caches to be synced, before processing items from the queue is started
	// 刚开始启动，从api-server一次性全量同步所有数据
	if !cache.WaitForCacheSync(stopCh, c.informer.HasSynced) {
		runtime.HandleError(fmt.Errorf("timed out waiting for caches to sync"))
		return
	}

	// 支持多个线程并行从队列中取得数据进行处理
	for i := 0; i < threadiness; i++ {
		go wait.Until(c.runWorker, time.Second, stopCh)
	}

	<-stopCh
	klog.Info("Stopping Pod controller")
}

// CreateAndStartController 为了便于外部使用，这里将controller的创建和启动封装在一起
func CreateAndStartController(c cache.Getter, objType objectruntime.Object, resource string, namespace string, stopCh chan struct{}) {
	// ListWatcher用于获取数据并监听资源的事件
	podListWatcher := cache.NewListWatchFromClient(c, resource, NAMESPACE, fields.Everything())

	// 限速队列，里面存的是有事件发生的对象的身份信息，而非对象本身
	queue := workqueue.NewRateLimitingQueue(workqueue.DefaultControllerRateLimiter())

	// 创建本地缓存并对指定类型的资源开始监听
	// 注意，如果业务上有必要，其实可以将新增、修改、删除等事件放入不同队列，然后分别做针对性处理，
	// 但是，controller对应的模式，主要是让status与spec达成一致，也就是说增删改等事件，对应的都是查到实际情况，令其与期望情况保持一致，
	// 因此，多数情况下增删改用一个队列即可，里面放入变化的对象的身份，至于处理方式只有一种：查到实际情况，令其与期望情况保持一致
	indexer, informer := cache.NewIndexerInformer(podListWatcher, objType, 0, cache.ResourceEventHandlerFuncs{
		AddFunc: func(obj interface{}) {
			key, err := cache.MetaNamespaceKeyFunc(obj)
			if err == nil {
				// 再次注意：这里放入队列的并非对象，而是对象的身份，作用是仅仅告知消费方，该对象有变化，
				// 至于有什么变化，需要消费方自行判断，然后再做针对性处理
				queue.Add(key)
			}
		},
		UpdateFunc: func(old interface{}, new interface{}) {
			key, err := cache.MetaNamespaceKeyFunc(new)
			if err == nil {
				queue.Add(key)
			}
		},
		DeleteFunc: func(obj interface{}) {
			key, err := cache.DeletionHandlingMetaNamespaceKeyFunc(obj)
			if err == nil {
				queue.Add(key)
			}
		},
	}, cache.Indexers{})

	controller := &Controller{
		informer: informer,
		indexer:  indexer,
		queue:    queue,
	}

	go controller.Run(1, stopCh)
}
