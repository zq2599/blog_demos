package unitfake

import (
	"context"

	apps "k8s.io/api/apps/v1"
	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
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

// CreateDeployment 单元测试的辅助工具，用于创建namespace
func CreatePods(context context.Context, client kubernetes.Interface, namespace, name, image, app string) error {
	_, err := client.CoreV1().Pods(namespace).Create(context, &v1.Pod{
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
	}, metav1.CreateOptions{})

	return err

}
