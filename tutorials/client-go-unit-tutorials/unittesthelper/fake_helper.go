package unittesthelper

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"net/http/httptest"

	"github.com/gin-gonic/gin"
	apps "k8s.io/api/apps/v1"
	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
)

const (
	TEST_NAMESPACE       = "client-go-tutorials"
	TEST_POD_NAME_PREFIX = "nginx-pod-"
	TEST_IMAGE           = "nginx:latest"
	TEST_LABEL_APP       = "nginx-app"
	TEST_POD_NUM         = 3
)

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

// CreateDeployment 单元测试的辅助工具，用于创建namespace
func CreateDeployment(context context.Context, client kubernetes.Interface, namespace, name, image, app string, replicas int32) error {
	_, err := client.AppsV1().Deployments(namespace).Create(context, &apps.Deployment{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Deployment",
			APIVersion: "apps/v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      name,
			Namespace: namespace,
			Labels: map[string]string{
				"app": app,
			},
		},
		Spec: apps.DeploymentSpec{
			Replicas: &replicas,
			Template: v1.PodTemplateSpec{
				Spec: v1.PodSpec{
					Containers: []v1.Container{
						{
							Image: image,
						},
					},
				},
			},
		},
	}, metav1.CreateOptions{})

	return err

}

func CreatePodObj(namespace, name, app, image string) *v1.Pod {
	return &v1.Pod{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Deployment",
			APIVersion: "apps/v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      name,
			Namespace: namespace,
			Labels: map[string]string{
				"app": app,
			},
		},
		Spec: v1.PodSpec{
			Containers: []v1.Container{
				{
					Image: image,
				},
			},
		},
	}
}

// CreateDeployment 单元测试的辅助工具，用于创建namespace
func CreatePods(context context.Context, client kubernetes.Interface, namespace, name, image, app string) error {
	_, err := client.CoreV1().Pods(namespace).Create(context, CreatePodObj(namespace, name, app, image), metav1.CreateOptions{})
	return err
}

// CreatePod 辅助方法，用于创建多个pod
func CreatePod(context context.Context, client kubernetes.Interface, num int) {
	for i := 0; i < num; i++ {
		if err := CreatePods(context,
			client,
			TEST_NAMESPACE,
			fmt.Sprintf("%s%d", TEST_POD_NAME_PREFIX, i),
			TEST_IMAGE,
			TEST_LABEL_APP); err != nil {
			log.Fatalf("create pod [%d] error, %s", i, err.Error())
		}
	}
}

// SingleTest 辅助方法，发请求，返回响应
func SingleTest(router *gin.Engine, url string) (int, string, error) {
	log.Printf("start SingleTest, request url : %s", url)
	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, url, nil)
	router.ServeHTTP(w, req)
	return w.Code, w.Body.String(), nil
}
