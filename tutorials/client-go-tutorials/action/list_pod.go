package action

import (
	"context"
	"fmt"

	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
)

type ListPod struct{}

func (listPod ListPod) DoAction(clientset *kubernetes.Clientset) error {
	namespace := "kube-system"

	// 查询pod列表
	pods, err := clientset.CoreV1().Pods(namespace).List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		panic(err.Error())
	}

	nums := len(pods.Items)

	fmt.Printf("There are %d pods in the cluster\n", nums)

	// 如果没有pod就返回了
	if nums < 1 {
		return nil
	}

	// 遍历列表中的每个pod
	for index, pod := range pods.Items {
		fmt.Printf("%v. pod name : %v\n", index, pod.Name)

		// 用pod name精确搜索单个pod
		podObj, err := clientset.CoreV1().Pods(namespace).Get(context.TODO(), pod.Name, metav1.GetOptions{})
		if errors.IsNotFound(err) {
			fmt.Printf("Pod %s in namespace %s not found\n", pod.Name, namespace)
		} else if statusError, isStatus := err.(*errors.StatusError); isStatus {
			fmt.Printf("Error getting pod %s in namespace %s: %v\n",
				pod.Name, namespace, statusError.ErrStatus.Message)
		} else if err != nil {
			panic(err.Error())
		} else {
			fmt.Printf("Found pod %s in namespace %s\n", podObj.Name, namespace)
		}
	}

	return nil
}
