package initor

import (
	"github.com/gin-gonic/gin"

	"kubernetes-object-tutorials/handler"
)

const (
	PATH_QUERY_TYPE_META_BY_POD_NAME = "/query_type_meta_by_pod_name"
)

func InitRouter() *gin.Engine {
	r := gin.Default()

	// 绑定path的handler
	r.GET(PATH_QUERY_TYPE_META_BY_POD_NAME, handler.QueryTypeMetaByPodName)

	return r
}
