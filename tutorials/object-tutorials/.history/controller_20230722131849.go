package main

import (
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

// NewController 简单封装了数据结构的实例化
func NewController(queue workqueue.RateLimitingInterface, indexer cache.Indexer, informer cache.Controller) *Controller {
	return &Controller{
		informer: informer,
		indexer:  indexer,
		queue:    queue,
	}
}
