package handler

import (
	"log"
	"net/http"

	"github.com/gin-gonic/gin"

	kubernetesservice "kubernetes-object-tutorials/kubernetes_service"
)

const (
	PARAM_NAMESPACE = "namespace"
	PARAM_POD_NAME  = "pod_name"
)

// QueryTypeMetaByPodName 根据指定的namespace和pod名称获取该pod的TypeMeta信息
func QueryTypeMetaByPodName(context *gin.Context) {
	rlt := make(map[string]interface{})
	namespace := context.DefaultQuery(PARAM_NAMESPACE, "")
	podName := context.DefaultQuery(PARAM_POD_NAME, "")

	log.Printf("query param, namespace [%s], podName [%s]", namespace, podName)
	typeMeta, err := kubernetesservice.QueryTypeMetaByPodName(context, namespace, podName)

	if err != nil {
		rlt["message"] = err.Error()
		context.JSON(http.StatusInternalServerError, rlt)
		return
	}

	rlt["message"] = "success"
	rlt["kind"] = typeMeta.Kind
	rlt["api_version"] = typeMeta.APIVersion
	context.JSON(http.StatusOK, rlt)
}
