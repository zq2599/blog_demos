package handler

import (
	"log"
	"net/http"

	"github.com/gin-gonic/gin"

	kubernetesservice "client-go-unit-tutorials/kubernetes_service"
)

const (
	PARAM_NAMESPACE = "namespace"
	PARAM_APP       = "label_app"
)

func QueryPodsByLabelApp(context *gin.Context) {
	rlt := make(map[string]interface{})
	namespace := context.DefaultQuery(PARAM_NAMESPACE, "")
	app := context.DefaultQuery(PARAM_APP, "")

	log.Printf("query param, namespace [%s], app [%s]", namespace, app)
	names, err := kubernetesservice.QueryPodNameByLabelApp(context, namespace, app)

	if err != nil {
		rlt["message"] = err.Error()
		context.JSON(http.StatusInternalServerError, rlt)
		return
	}

	rlt["message"] = "success"
	rlt["names"] = names
	context.JSON(http.StatusOK, rlt)
}
