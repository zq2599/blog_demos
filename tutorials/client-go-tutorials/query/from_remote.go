package query

import (
	"context"
	"fmt"
	"log"

	"github.com/gin-gonic/gin"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/selection"
	"k8s.io/client-go/kubernetes"
)

const (
	NAMESPACE   = "kube-system"
	LABEL_NAME  = "component"
	LABEL_VALUE = "kube-apiserver"
)

var LABEL_SELECTOR string
var CLIENTSET *kubernetes.Clientset

type QueryFromRemote struct{}

// 一次性构造好条件
func (queryFromRemote QueryFromRemote) onceInit(clientset *kubernetes.Clientset) {
	log.Println("start init of QueryFromRemote")

	// 放入全局变量存储，后续业务操作时重复使用
	CLIENTSET = clientset

	requirement, err := labels.NewRequirement(LABEL_NAME, selection.Equals, []string{LABEL_VALUE})

	if err != nil {
		log.Panic("create equalRequirement fai")
	}

	// label查询字符串也放入全局变量重复使用
	LABEL_SELECTOR = labels.NewSelector().Add(*requirement).String()
}

func handleRemote(c *gin.Context) {

	pods, err := CLIENTSET.CoreV1().Pods(NAMESPACE).List(context.TODO(), metav1.ListOptions{
		// 传入的selector在这里用到
		LabelSelector: LABEL_SELECTOR,
	})

	if err != nil {
		c.String(500, fmt.Sprintf("1. get pod failed, %v", err))
	} else if pods.Size() < 1 {
		c.String(500, fmt.Sprintf("1. get pod failed, %v", err))
	} else {
		c.String(200, fmt.Sprintf("1. find pod %s", pods.Items[0].Name))
	}

}

func (queryFromRemote QueryFromRemote) DoAction(clientset *kubernetes.Clientset) error {

	queryFromRemote.onceInit(clientset)

	r := gin.Default()
	r.GET("/performance", handleRemote)
	r.Run()

	return nil
}
