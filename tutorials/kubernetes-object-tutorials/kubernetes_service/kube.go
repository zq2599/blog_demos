package kubernetesservice

import (
	"context"
	"flag"
	"log"
	"path/filepath"
	"sync"

	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"

	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/selection"
)

var CLIENT_SET kubernetes.Interface
var ONCE sync.Once

// DoInit Indexer相关的初始化操作，这里确保只执行一次
func DoInit() {
	ONCE.Do(initInKubernetesEnv)
}

// GetClient 调用此方法返回clientSet对象
func GetClient() kubernetes.Interface {
	return CLIENT_SET
}

// SetClient 可以通过initInKubernetesEnv在kubernetes初始化，如果有准备好的clientSet，也可以调用SetClient直接设置，而无需初始化
func SetClient(clientSet kubernetes.Interface) {
	CLIENT_SET = clientSet
}

// initInKubernetesEnv 这里是真正的初始化逻辑
func initInKubernetesEnv() {
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
	CLIENT_SET, err = kubernetes.NewForConfig(config)
	if err != nil {
		panic(err.Error())
	}

	log.Println("kubernetes服务初始化成功")
}

// QueryPodNameByLabelApp 根据指定的namespace和label值搜索
func QueryPodNameByLabelApp(context context.Context, namespace, app string) ([]string, error) {
	log.Printf("QueryPodNameByLabelApp, namespace [%s], app [%s]", namespace, app)

	equalRequirement, err := labels.NewRequirement("app", selection.Equals, []string{app})

	if err != nil {
		return nil, err
	}

	selector := labels.NewSelector().Add(*equalRequirement)

	// 查询pod列表
	pods, err := CLIENT_SET.
		CoreV1().
		Pods(namespace).
		List(context, metav1.ListOptions{
			// 传入的selector在这里用到
			LabelSelector: selector.String(),
		})

	if err != nil {
		return nil, err
	}

	names := make([]string, 0)

	for _, v := range pods.Items {
		names = append(names, v.GetName())
	}

	return names, nil
}

// CreateNamespace 单元测试的辅助工具，用于创建namespace
func CreateNamespace(context context.Context, client kubernetes.Interface, name string) error {
	namespaceObj := &v1.Namespace{
		ObjectMeta: metav1.ObjectMeta{
			Name: name,
		},
	}

	_, err := client.CoreV1().Namespaces().Create(context, namespaceObj, metav1.CreateOptions{})
	return err
}

// DeleteeNamespace 单元测试的辅助工具，用于创建namespace
func DeleteNamespace(context context.Context, client kubernetes.Interface, name string) error {
	err := client.CoreV1().Namespaces().Delete(context, name, metav1.DeleteOptions{})
	return err
}

// QueryTypeMetaByPodName 根据指定的namespace和pod名称获取该pod的TypeMeta信息
func QueryTypeMetaByPodName(context context.Context, namespace, podName string) (*metav1.TypeMeta, error) {

	pod, err := CLIENT_SET.CoreV1().Pods(namespace).Get(context, podName, metav1.GetOptions{
		TypeMeta: metav1.TypeMeta{},
	})

	if err != nil {
		return nil, err
	}

	kind := pod.GetObjectKind()
	log.Printf("kind, %v", kind)
	return &pod.TypeMeta, nil
}
