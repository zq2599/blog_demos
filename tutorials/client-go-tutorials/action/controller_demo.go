package action

import (
	v1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/fields"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/cache"
	"k8s.io/client-go/util/workqueue"
)

type ControllerDemo struct{}

func (controllerDemo ControllerDemo) DoAction(clientset *kubernetes.Clientset) error {
	/*
		setSelector := labels.SelectorFromSet(labels.Set(map[string]string{"app": "nginx"}))
		optionsModifer := func(options *metav1.ListOptions) {
			options.LabelSelector = setSelector.String()
		}

		podListWatcher := cache.NewFilteredListWatchFromClient(clientset.CoreV1().RESTClient(), "pods", metav1.NamespaceDefault, optionsModifer)
	*/
	// 创建ListWatch对象，指定要监控的资源类型是pod，namespace是default
	podListWatcher := cache.NewListWatchFromClient(clientset.CoreV1().RESTClient(), "pods", v1.NamespaceDefault, fields.Everything())

	// 创建工作队列
	queue := workqueue.NewRateLimitingQueue(workqueue.DefaultControllerRateLimiter())

	// 创建informer，并将返回的存储对象保存在变量indexer中
	indexer, informer := cache.NewIndexerInformer(podListWatcher, &v1.Pod{}, 0, cache.ResourceEventHandlerFuncs{
		// 响应新增资源事件的方法，可以按照业务需求来定制，
		// 这里的做法比较常见：写入工作队列
		AddFunc: func(obj interface{}) {
			key, err := cache.MetaNamespaceKeyFunc(obj)
			if err == nil {
				queue.Add(key)
			}
		},
		// 响应修改资源事件的方法，可以按照业务需求来定制，
		// 这里的做法比较常见：写入工作队列
		UpdateFunc: func(old interface{}, new interface{}) {
			key, err := cache.MetaNamespaceKeyFunc(new)
			if err == nil {
				queue.Add(key)
			}
		},
		// 响应修改资源事件的方法，可以按照业务需求来定制，
		// 这里的做法比较常见：写入工作队列，注意删除的时候生成key的方法和新增修改不一样
		DeleteFunc: func(obj interface{}) {
			// IndexerInformer uses a delta queue, therefore for deletes we have to use this
			// key function.
			key, err := cache.DeletionHandlingMetaNamespaceKeyFunc(obj)
			if err == nil {
				queue.Add(key)
			}
		},
	}, cache.Indexers{})

	// 创建Controller对象，将所需的三个变量对象传入
	controller := NewController(queue, indexer, informer)

	// Now let's start the controller
	stop := make(chan struct{})
	defer close(stop)
	// 在协程中启动controller
	go controller.Run(1, stop)

	// Wait forever
	select {}
	return nil
}
