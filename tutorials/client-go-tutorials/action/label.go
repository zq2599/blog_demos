package action

import (
	"context"

	"log"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/selection"
	"k8s.io/client-go/kubernetes"
)

type Lable struct{}

// listPods 根据传入的selector过滤
func listPods(clientset *kubernetes.Clientset, selector labels.Selector, prefix string) {
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

func (lable Lable) DoAction(clientset *kubernetes.Clientset) error {
	// 第一种： 创建Requirement对象，指定类型是Equals(等于)
	equalRequirement, err := labels.NewRequirement("app", selection.Equals, []string{"other"})

	if err != nil {
		log.Println("1. create equalRequirement fail, ", err)
		return err
	}

	selector := labels.NewSelector().Add(*equalRequirement)

	// 验证，应该只查到app等于other的pod
	listPods(clientset, selector, "用Requirement创建，Equal操作")

	// 第一种： 创建Requirement对象，指定类型是In，not_exists不会有任何pod匹配到
	inRequirement, err := labels.NewRequirement("app", selection.In, []string{"other", "nginx", "not_exists"})

	if err != nil {
		log.Println("2. create equalRequirement fail, ", err)
		return err
	}

	selector = labels.NewSelector().Add(*inRequirement)

	// 验证，应该查到app=other的pod
	listPods(clientset, selector, "用Requirement创建，In操作")

	// 第二种：labels.Parse方法
	parsedSelector, err := labels.Parse("bind-service=none,app notin (not_exists)")

	if err != nil {
		log.Println("3. create equalRequirement fail, ", err)
		return err
	}

	// 验证，应该查到app=other的pod
	listPods(clientset, parsedSelector, "用Parse创建")

	// 第三种：labels.SelectorFromSet方法
	setSelector := labels.SelectorFromSet(labels.Set(map[string]string{"app": "nginx"}))

	// 验证，应该查到app=nginx的pod
	listPods(clientset, setSelector, "用SelectorFromSet创建")

	// 第四种：metav1.LabelSelectorAsSelector方法
	// 适用于当前环境已有资源对象的场景，可以取出LabelSelector对象来转换成labels.Selector
	// 先创建一个LabelSelector
	labelSelector := &metav1.LabelSelector{
		MatchLabels: map[string]string{"app": "other"},
	}

	// 将LabelSelector转为labels.Selector
	convertSelector, err := metav1.LabelSelectorAsSelector(labelSelector)

	if err != nil {
		log.Println("4. create equalRequirement fail, ", err)
		return err
	}

	// 验证，应该查到app=nginx的pod
	listPods(clientset, convertSelector, "用LabelSelector转换")

	return nil
}
