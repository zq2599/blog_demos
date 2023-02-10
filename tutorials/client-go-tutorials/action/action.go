package action

import "k8s.io/client-go/kubernetes"

type Action interface {
	DoAction(clientset *kubernetes.Clientset) error
}
