package main

import (
	"kubernetes-object-tutorials/initor"
	kubernetesservice "kubernetes-object-tutorials/kubernetes_service"
)

func main() {
	// 初始化kubernetes相关配置
	kubernetesservice.DoInit()

	router := initor.InitRouter()
	_ = router.Run(":18080")
}
