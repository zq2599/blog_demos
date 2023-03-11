package action

import (
	"fmt"
	"time"

	v1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/util/runtime"
	"k8s.io/apimachinery/pkg/util/wait"
	"k8s.io/client-go/tools/cache"
	"k8s.io/client-go/util/workqueue"
	"k8s.io/klog/v2"
)

type Controller struct {
	indexer  cache.Indexer
	queue    workqueue.RateLimitingInterface
	informer cache.Controller
}

func (c *Controller) processNextItem() bool {
	// 阻塞等待，直到队列中有数据可以被取出，
	// 另外有可能是多协程并发获取数据，此key会被放入processing中，表示正在被处理
	key, quit := c.queue.Get()
	// 如果最外层调用了队列的Shutdown，这里的quit就会返回true，
	// 调用processNextItem的地方发现processNextItem返回false，就不会再次调用processNextItem了
	if quit {
		return false
	}

	// 表示该key已经被处理完成(从processing中移除)
	defer c.queue.Done(key)

	// 调用业务方法，实现具体的业务需求
	err := c.syncToStdout(key.(string))
	// Handle the error if something went wrong during the execution of the business logic

	// 判断业务逻辑处理是否出现异常，如果出现就重新放入队列，以此实现重试，如果已经重试过5次，就放弃
	c.handleErr(err, key)

	// 调用processNextItem的地方发现processNextItem返回true，就会再次调用processNextItem
	return true
}

func (c *Controller) syncToStdout(key string) error {
	// 根据key从本地存储中获取完整的pod信息
	// 由于有长连接与apiserver保持同步，因此本地的pod信息与kubernetes集群内保持一致
	obj, exists, err := c.indexer.GetByKey(key)
	if err != nil {
		klog.Errorf("Fetching object with key %s from store failed with %v", key, err)
		return err
	}

	if !exists {
		fmt.Printf("Pod %s does not exist anymore\n", key)
	} else {
		// 这里就是真正的业务逻辑代码了，一般会比较spce和status的差异，然后做出处理使得status与spce保持一致，
		// 此处为了代码简单仅仅打印一行日志
		fmt.Printf("Sync/Add/Update for Pod %s\n", obj.(*v1.Pod).GetName())
	}
	return nil
}

func (c *Controller) handleErr(err error, key interface{}) {
	// 没有错误时的处理逻辑
	if err == nil {
		// 确认这个key已经被成功处理，在队列中彻底清理掉
		// 假设之前在处理该key的时候曾报错导致重新进入队列等待重试，那么也会因为这个Forget方法而不再被重试
		c.queue.Forget(key)
		return
	}

	// 代码走到这里表示前面执行业务逻辑的时候发生了错误，
	// 检查已经重试的次数，如果不操作5次就继续重试，这里可以根据实际需求定制
	if c.queue.NumRequeues(key) < 5 {
		klog.Infof("Error syncing pod %v: %v", key, err)
		c.queue.AddRateLimited(key)
		return
	}

	// 如果重试超过了5次就彻底放弃了，也像执行成功那样调用Forget做彻底清理（否则就没完没了了）
	c.queue.Forget(key)
	// 向外部报告错误，走通用的错误处理流程
	runtime.HandleError(err)
	klog.Infof("Dropping pod %q out of the queue: %v", key, err)
}

func NewController(queue workqueue.RateLimitingInterface, indexer cache.Indexer, informer cache.Controller) *Controller {
	return &Controller{
		informer: informer,
		indexer:  indexer,
		queue:    queue,
	}
}

func (c *Controller) runWorker() {
	for c.processNextItem() {
	}
}

func (c *Controller) Run(workers int, stopCh chan struct{}) {
	defer runtime.HandleCrash()

	// 只要工作队列的ShutDown方法被调用，processNextItem方法就会返回false，runWorker的无限循环就会结束
	defer c.queue.ShutDown()
	klog.Info("Starting Pod controller")

	// informer的Run方法执行后，就开始接受apiserver推送的资源变更事件，并更新本地存储
	go c.informer.Run(stopCh)

	// 等待本地存储和apiserver完成同步
	if !cache.WaitForCacheSync(stopCh, c.informer.HasSynced) {
		runtime.HandleError(fmt.Errorf("Timed out waiting for caches to sync"))
		return
	}

	// 启动worker，并发从工作队列取数据，然后执行业务逻辑
	for i := 0; i < workers; i++ {
		go wait.Until(c.runWorker, time.Second, stopCh)
	}

	<-stopCh
	klog.Info("Stopping Pod controller")
}
