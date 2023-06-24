package handler

import (
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/selection"

	kubernetesservice "client-go-unit-tutorials/kubernetes_service"
)

const (
	PARAM_NAMESPACE = "namespace"
	PARAM_APP       = "label_app"
)

func QueryPodsByLabelApp(context *gin.Context) {
	rlt := make(map[string]interface{})

	clientSet := kubernetesservice.GetClient()

	if clientSet == nil {
		errorMsg := "kubernetes enviroment is not ready"
		log.Println(errorMsg)
		rlt["message"] = errorMsg
		context.JSON(http.StatusInternalServerError, rlt)
		return
	}

	namespace := context.DefaultQuery(PARAM_NAMESPACE, "")
	app := context.DefaultQuery(PARAM_APP, "")

	log.Printf("query param, namespace [%s], app [%s]", namespace, app)

	equalRequirement, err := labels.NewRequirement("app", selection.Equals, []string{app})

	if err != nil {
		log.Fatalf("1. create equalRequirement fail, %d", err)
	}

	selector := labels.NewSelector().Add(*equalRequirement)

	// 查询pod列表
	pods, err := clientSet.
		CoreV1().
		Pods(namespace).
		List(context, metav1.ListOptions{
			// 传入的selector在这里用到
			LabelSelector: selector.String(),
		})

	if err != nil {
		rlt["message"] = err.Error()
		context.JSON(http.StatusInternalServerError, rlt)
		return
	}

	rlt["message"] = "success"
	names := make([]string, 0)

	for _, v := range pods.Items {
		names = append(names, v.GetName())
	}

	rlt["pods"] = names
	context.JSON(http.StatusOK, rlt)
}
