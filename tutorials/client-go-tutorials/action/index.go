package action

import (
	"context"
	"errors"
	"time"

	"log"

	v1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/fields"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/selection"
	"k8s.io/apimachinery/pkg/util/runtime"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/cache"
)

const (
	INDEXER_POD_NAME = "pod_name"
)

type Index struct{}

// listPods 根据传入的selector过滤
func simpleList(clientset *kubernetes.Clientset, selector labels.Selector, prefix string) {
	namespace := "default"

	// 查询pod列表
	pods, err := clientset.CoreV1().Pods(namespace).List(context.TODO(), metav1.ListOptions{
		// 传入的selector在这里用到
		LabelSelector: selector.String(),
	})

	if err != nil {
		panic(err.Error())
	}

	nums := len(pods.Items)

	log.Printf("[%v] 查到[%d]个pod\n", prefix, nums)

	// 如果没有pod就返回了
	if nums < 1 {
		return
	}

	// 遍历列表中的每个pod
	for index, pod := range pods.Items {
		log.Printf("[%v] %v. pod : %v\n", prefix, index+1, pod.Name)
	}
}

// remoteFind 请求apiserver获取结果
func remoteFind(clientset *kubernetes.Clientset) error {
	namespace := "kube-system"
	// 第一种： 创建Requirement对象，指定类型是Equals(等于)
	equalRequirement, err := labels.NewRequirement("component", selection.Equals, []string{"kube-apiserver"})

	if err != nil {
		log.Println("1. create equalRequirement fail, ", err)
		return err
	}

	selector := labels.NewSelector().Add(*equalRequirement)

	startTime := time.Now()

	pods, err := clientset.CoreV1().Pods(namespace).List(context.TODO(), metav1.ListOptions{
		// 传入的selector在这里用到
		LabelSelector: selector.String(),
	})

	log.Printf("远程调用耗时[%d]毫秒", time.Since(startTime).Milliseconds())

	if err != nil {
		panic(err.Error())
	}

	log.Printf("查到[%d]个pod\n", len(pods.Items))

	// 遍历列表中的每个pod
	for index, pod := range pods.Items {
		log.Printf("%v. pod : %v\n", index+1, pod.Name)
	}

	return nil
}

func localFind(clientset *kubernetes.Clientset, stopCh chan struct{}) error {
	var err error

	podListWatcher := cache.NewListWatchFromClient(clientset.CoreV1().RESTClient(), "pods", "kube-system", fields.Everything())

	indexers := cache.Indexers{
		INDEXER_POD_NAME: func(obj interface{}) ([]string, error) {
			var object metav1.Object
			object, err = meta.Accessor(obj)
			if err != nil {
				return []string{}, nil
			}

			podName := object.GetName()
			if podName == "" {
				return []string{}, nil
			}
			return []string{podName}, nil
		},
	}

	indexer, informer := cache.NewIndexerInformer(podListWatcher, &v1.Pod{}, 0, cache.ResourceEventHandlerFuncs{
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

	defer runtime.HandleCrash()

	// informer的Run方法执行后，就开始接受apiserver推送的资源变更事件，并更新本地存储
	go informer.Run(stopCh)

	// 等待本地存储和apiserver完成同步
	if !cache.WaitForCacheSync(stopCh, informer.HasSynced) {
		err = errors.New("timed out waiting for caches to sync")
		runtime.HandleError(err)
		return err
	}

	v, err := indexer.ByIndex(INDEXER_POD_NAME, "kube-apiserver-hedy")

	if err != nil {
		log.Printf("get by index fail, %v", err)
		return err
	}

	log.Printf("get pod by index, %v", v)

	<-stopCh
	log.Println("Stopping Pod controller")

	return nil
}

// NewFilteredLeaseInformer(client, f.namespace, resyncPeriod, cache.Indexers{cache.NamespaceIndex: cache.MetaNamespaceIndexFunc}, f.tweakListOptions)

func (index Index) DoAction(clientset *kubernetes.Clientset) error {

	_ = remoteFind(clientset)

	stop := make(chan struct{})
	defer close(stop)
	// 在协程中启动controller
	go localFind(clientset, stop)

	// Wait forever
	select {}
	return nil
}
