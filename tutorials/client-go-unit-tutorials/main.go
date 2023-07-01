package main

import (
	"client-go-unit-tutorials/initor"
	kubernetesservice "client-go-unit-tutorials/kubernetes_service"
)

func main() {
	// 初始化kubernetes相关配置
	kubernetesservice.DoInit()

	router := initor.InitRouter()
	_ = router.Run(":18080")
}
