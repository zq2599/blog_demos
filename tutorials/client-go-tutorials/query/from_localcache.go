package query

import (
	"errors"
	"fmt"
	"log"

	"github.com/gin-gonic/gin"
	v1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/fields"
	"k8s.io/apimachinery/pkg/util/runtime"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/cache"
)

const (
	INDEXER_POD_NAME = "pod_name"
)

var INDEXER cache.Indexer

type QueryFromLocalCache struct{}

// 一次性构造好条件
func (queryFromLocalCache QueryFromLocalCache) onceInit(clientset *kubernetes.Clientset, stopCh chan struct{}) {
	log.Println("start init of QueryFromLocalCache")

	var err error

	// 确定从apiserver订阅的类型
	podListWatcher := cache.NewListWatchFromClient(clientset.CoreV1().RESTClient(), "pods", NAMESPACE, fields.Everything())

	// Indexers对象的类型是map，key是自定义字符串，value是个function，用于根据业务逻辑返回一个对象的字符串
	indexers := cache.Indexers{
		INDEXER_POD_NAME: func(obj interface{}) ([]string, error) {
			var object metav1.Object
			object, err = meta.Accessor(obj)
			if err != nil {
				return []string{}, nil
			}

			labelValue := object.GetLabels()[LABEL_NAME]
			if labelValue == "" {
				return []string{}, nil
			}
			return []string{labelValue}, nil
		},
	}

	var informer cache.Controller

	INDEXER, informer = cache.NewIndexerInformer(podListWatcher, &v1.Pod{}, 0, cache.ResourceEventHandlerFuncs{
		AddFunc: func(obj interface{}) {
			log.Printf("AddFunc execute, %v", obj)
		},
		UpdateFunc: func(old interface{}, new interface{}) {
			log.Printf("UpdateFunc execute, old is %v, new is %v", old, new)
		},
		DeleteFunc: func(obj interface{}) {
			log.Printf("DeleteFunc execute, %v", obj)
		},
	}, indexers)

	// informer的Run方法执行后，就开始接受apiserver推送的资源变更事件，并更新本地存储
	go informer.Run(stopCh)

	// 等待本地存储和apiserver完成同步
	if !cache.WaitForCacheSync(stopCh, informer.HasSynced) {
		err = errors.New("timed out waiting for caches to sync")
		runtime.HandleError(err)
		return
	}
}

func handleLocalCache(c *gin.Context) {
	v, err := INDEXER.ByIndex(INDEXER_POD_NAME, LABEL_VALUE)

	if err != nil {
		c.String(500, fmt.Sprintf("2. get pod failed, %v", err))
	} else if nil == v || len(v) < 1 {
		c.String(500, fmt.Sprintf("2. get empty pod, %v", err))
	} else {
		c.String(200, fmt.Sprintf("2. find pod [%s]", v[0].(*v1.Pod).Name))
	}
}

func (queryFromLocalCache QueryFromLocalCache) DoAction(clientset *kubernetes.Clientset) error {
	queryFromLocalCache.onceInit(clientset, make(chan struct{}))

	r := gin.Default()
	r.GET("/performance", handleLocalCache)
	r.Run()

	return nil
}
