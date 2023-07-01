package initor

import (
	"github.com/gin-gonic/gin"
	"k8s.io/apimachinery/pkg/fields"
	"k8s.io/client-go/tools/cache"

	"client-go-unit-tutorials/customizecontroller"
	"client-go-unit-tutorials/dao"
	"client-go-unit-tutorials/handler"
	kubernetesservice "client-go-unit-tutorials/kubernetes_service"
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

// InitController 初始化controller
func InitController() {
	clientset := kubernetesservice.GetClient()
	polListWatcher := cache.NewListWatchFromClient(clientset.CoreV1().RESTClient(), "pods", customizecontroller.CUSTOMIZE_NAMESPACE, fields.Everything())

	// 创建并运行controller
	customizecontroller.StartNewController(polListWatcher)
}

// InitDB 初始化gorm
func InitDB() {
	dao.InitDB(nil)
}
