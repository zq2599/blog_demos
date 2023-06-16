package remote

import (
	"client-go-indexer-tutorials/basic"
	"fmt"

	"github.com/gin-gonic/gin"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// GetObjByObjKey 远程请求，根据指定key查询pod对象
func GetObjByObjKey(c *gin.Context) {
	rawObj, err := basic.ClientSet.
		CoreV1().
		Pods(basic.NAMESPACE).
		Get(c, c.DefaultQuery("pod_name", ""), metav1.GetOptions{})

	if err != nil {
		c.String(500, fmt.Sprintf("g. get pod failed, %v", err))
	} else {
		c.JSON(200, rawObj)
	}
}
