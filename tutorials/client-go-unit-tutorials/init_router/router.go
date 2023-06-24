package initrouter

import (
	"client-go-unit-tutorials/handler"

	"github.com/gin-gonic/gin"
)

const (
	PATH_QUERY_PODS_BY_LABEL_APP = "/query_pods_by_label_app"
)

func InitRouter() *gin.Engine {
	r := gin.Default()
	r.GET("/ping", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"message": "pong",
		})
	})

	r.GET(PATH_QUERY_PODS_BY_LABEL_APP, handler.QueryPodsByLabelApp)

	return r
}
