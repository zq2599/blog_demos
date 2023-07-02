package initor

import (
	"github.com/gin-gonic/gin"

	"client-go-unit-tutorials/handler"
)

const (
	PATH_QUERY_PODS_BY_LABEL_APP = "/query_pods_by_label_app"
)

func InitRouter() *gin.Engine {
	r := gin.Default()

	// 绑定path的handler
	r.GET(PATH_QUERY_PODS_BY_LABEL_APP, handler.QueryPodsByLabelApp)

	return r
}
