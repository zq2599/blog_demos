package action

import (
	"client-go-tutorials/constant"
	"errors"
	"log"
	"time"

	v1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/meta"
	"k8s.io/apimachinery/pkg/fields"
	"k8s.io/apimachinery/pkg/util/runtime"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/cache"
)

type MetaDemo struct{}

// accessorDemo 演示Accessor的用法
func accessorDemo(prefix string, obj interface{}) {
	meta, err := meta.Accessor(obj)

	if err != nil {
		log.Printf("%s, Accessor error : %s", prefix, err.Error())
		return
	}

	log.Printf("%s, namespace [%s], name [%s]", prefix, meta.GetNamespace(), meta.GetName())
}

// typeAccessorDemo 演示TypeAccessor的用法
func typeAccessorDemo(prefix string, obj interface{}) {
	objType, err := meta.TypeAccessor(obj)

	if err != nil {
		log.Printf("%s, TypeAccessor error : %s", prefix, err.Error())
		return
	}

	log.Printf("%s, kind [%s], version [%s]", prefix, objType.GetKind(), objType.GetAPIVersion())
}

func (metaDemo MetaDemo) DoAction(clientset *kubernetes.Clientset) error {

	// 创建ListWatch对象，指定要监控的资源类型是pod，namespace是client-go-tutorials
	podListWatcher := cache.NewListWatchFromClient(clientset.CoreV1().RESTClient(), "pods", constant.NAMESPACE_DEMO, fields.Everything())

	_, controller := cache.NewInformer(
		podListWatcher,
		&v1.Pod{},
		time.Millisecond*100,
		cache.ResourceEventHandlerFuncs{
			AddFunc: func(obj interface{}) {
				accessorDemo("AddFunc", obj)
				typeAccessorDemo("AddFunc", obj)
			},
			DeleteFunc: func(obj interface{}) {
				accessorDemo("DeleteFunc", obj)
				typeAccessorDemo("DeleteFunc", obj)
			},
		},
	)

	log.Println("创建controller成功，开始全量同步pod信息")

	stop := make(chan struct{})
	defer close(stop)

	go controller.Run(stop)

	// 等待本地存储和apiserver完成同步
	if !cache.WaitForCacheSync(stop, controller.HasSynced) {
		runtime.HandleError(errors.New("timed out waiting for caches to sync"))
	}

	log.Println("全量同步pod信息成功")

	select {}
}
