package basic

import (
	"errors"
	"flag"
	"fmt"
	"log"
	"path/filepath"
	"sync"

	"github.com/gin-gonic/gin"
	v1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/fields"
	"k8s.io/apimachinery/pkg/util/runtime"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/cache"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"
)

const (
	NAMESPACE      = "indexer-tutorials"
	PARAM_LANGUAGE = "language"
	PARAM_OBJ_KEY  = "obj_key"

	LANGUAGE_C = "c"

	INDEXER_LANGUAGE              = "indexer_language"
	INDEXER_BUSINESS_SERVICE_TYPE = "indexer_business_service_type"

	LABEL_LANGUAGE              = "language"
	LABEL_BUSINESS_SERVICE_TYPE = "business-service-type"
)

var ClientSet *kubernetes.Clientset
var once sync.Once
var INDEXER cache.Indexer

// DoInit Indexer相关的初始化操作，这里确保只执行一次
func DoInit() {
	once.Do(initIndexer)
}

// initIndexer 这里是真正的初始化逻辑
func initIndexer() {
	log.Println("开始初始化Indexer")

	var kubeconfig *string

	// 试图取到当前账号的家目录
	if home := homedir.HomeDir(); home != "" {
		// 如果能取到，就把家目录下的.kube/config作为默认配置文件
		kubeconfig = flag.String("kubeconfig", filepath.Join(home, ".kube", "config"), "(optional) absolute path to the kubeconfig file")
	} else {
		// 如果取不到，就没有默认配置文件，必须通过kubeconfig参数来指定
		kubeconfig = flag.String("kubeconfig", "", "absolute path to the kubeconfig file")
	}

	// 加载配置文件
	config, err := clientcmd.BuildConfigFromFlags("", *kubeconfig)
	if err != nil {
		panic(err.Error())
	}

	// 用clientset类来执行后续的查询操作
	ClientSet, err = kubernetes.NewForConfig(config)
	if err != nil {
		panic(err.Error())
	}

	log.Println("kubernetes配置文件加载成功")

	// 确定从apiserver订阅的类型
	podListWatcher := cache.NewListWatchFromClient(ClientSet.CoreV1().RESTClient(), "pods", NAMESPACE, fields.Everything())

	// Indexers对象的类型是map，key是自定义字符串，value是个function，用于根据业务逻辑返回一个对象的字符串
	indexers := cache.Indexers{
		INDEXER_LANGUAGE: func(obj interface{}) ([]string, error) {
			var object metav1.Object
			object, err = meta.Accessor(obj)
			if err != nil {
				return []string{}, nil
			}

			labelValue := object.GetLabels()[LABEL_LANGUAGE]
			if labelValue == "" {
				return []string{}, nil
			}
			return []string{labelValue}, nil
		},
		INDEXER_BUSINESS_SERVICE_TYPE: func(obj interface{}) ([]string, error) {
			var object metav1.Object
			object, err = meta.Accessor(obj)
			if err != nil {
				return []string{}, nil
			}

			labelValue := object.GetLabels()[LABEL_BUSINESS_SERVICE_TYPE]
			if labelValue == "" {
				return []string{}, nil
			}
			return []string{labelValue}, nil
		},
	}

	var informer cache.Controller

	INDEXER, informer = cache.NewIndexerInformer(podListWatcher, &v1.Pod{}, 0, cache.ResourceEventHandlerFuncs{}, indexers)

	log.Println("Indexer初始化成功")

	stopCh := make(chan struct{})

	// informer的Run方法执行后，就开始接受apiserver推送的资源变更事件，并更新本地存储
	go informer.Run(stopCh)

	// 等待本地存储和apiserver完成同步
	if !cache.WaitForCacheSync(stopCh, informer.HasSynced) {
		err = errors.New("timed out waiting for caches to sync")
		runtime.HandleError(err)
		return
	}

	log.Println("pod加载完成")

}

// language 辅助方法，从请求参数中获取语言类型，默认返回c
func language(c *gin.Context) string {
	return c.DefaultQuery(PARAM_LANGUAGE, LANGUAGE_C)
}

// ObjKey 辅助方法，从请求参数中获取对象key
func ObjKey(c *gin.Context) string {
	return c.DefaultQuery(PARAM_OBJ_KEY, "")
}

// getObjKeysByLanguageName a. 查询指定语言的所有对象的key(演示2. IndexKeys方法)
func GetObjKeysByLanguageName(c *gin.Context) {
	language := language(c)

	v, err := INDEXER.IndexKeys(INDEXER_LANGUAGE, language)

	if err != nil {
		c.String(500, fmt.Sprintf("a. get pod failed, %v", err))
	} else if nil == v || len(v) < 1 {
		c.String(500, fmt.Sprintf("a. get empty pod, %v", err))
	} else {
		m := make(map[string][]string)
		m["language"] = v
		c.JSON(200, m)
	}
}

// GetObjByObjKey b. 根据对象的key返回(演示Store.Get方法)
func GetObjByObjKey(c *gin.Context) {
	rawObj, exists, err := INDEXER.GetByKey(ObjKey(c))

	if err != nil {
		c.String(500, fmt.Sprintf("b. get pod failed, %v", err))
	} else if !exists {
		c.String(500, fmt.Sprintf("b. get empty pod, %v", err))
	} else {
		if v, ok := rawObj.(*v1.Pod); ok {
			c.JSON(200, v)
		} else {
			c.String(500, "b. convert interface to pod failed")
		}
	}
}

// getObjByLanguageName c. 查询指定语言的所有对象(演示4. ByIndex方法)
func GetObjByLanguageName(c *gin.Context) {
	v, err := INDEXER.ByIndex(INDEXER_LANGUAGE, language(c))

	if err != nil {
		c.String(500, fmt.Sprintf("c. get pod failed, %v", err))
	} else if v == nil {
		c.String(500, fmt.Sprintf("c. get empty pod, %v", err))
	} else {
		m := make(map[string][]interface{})
		m["language"] = v
		c.JSON(200, m)
	}
}

// getAllObjByOneName d. 根据某个对象的key，获取同语言类型的所有对象(演示1. Index方法)
func GetAllObjByOneName(c *gin.Context) {
	// 注意，Index方法的第二个入参是对象，所以这里要先根据对象key查询到对象，然后再调用Index方法
	rawObj, exists, err := INDEXER.GetByKey(ObjKey(c))

	if err != nil {
		c.String(500, fmt.Sprintf("d1. get pod failed, %v", err))
	} else if !exists {
		c.String(500, fmt.Sprintf("d1. get empty pod, %v", err))
	} else {
		// 先得到pod对象，再根据pod对象查询同类型的所有对象
		if podObj, ok := rawObj.(*v1.Pod); ok {
			rawArray, err := INDEXER.Index(INDEXER_LANGUAGE, podObj)

			if err != nil {
				c.String(500, fmt.Sprintf("d2. get pod failed, %v", err))
			} else if len(rawArray) < 1 {
				c.String(500, fmt.Sprintf("d2. get empty pod, %v", err))
			} else {
				m := make(map[string][]interface{})
				m["language"] = rawArray
				c.JSON(200, m)
			}
		} else {
			c.String(500, "d1. convert interface to pod failed")
		}
	}
}

// getAllClassType e. 返回所有语言类型(演示3. ListIndexFuncValues方法)
func GetAllLanguange(c *gin.Context) {
	languages := INDEXER.ListIndexFuncValues(INDEXER_LANGUAGE)

	m := make(map[string][]string)
	m["language"] = languages

	c.JSON(200, m)
}

// getAllClassType f. 返回所有分类方式，这里应该是按服务类型和按语言类型两种(演示5. GetIndexers方法)
func GetAllClassType(c *gin.Context) {
	indexers := INDEXER.GetIndexers()
	// indexers是个map，其value是cache.IndexFunc类型，无法被序列化，所以这里只返回key
	names := make([]string, 0)
	for key, _ := range indexers {
		names = append(names, key)
	}
	c.JSON(200, names)
}
