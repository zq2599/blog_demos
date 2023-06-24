package main

import (
	initrouter "client-go-unit-tutorials/init_router"
	kubernetesservice "client-go-unit-tutorials/kubernetes_service"
)

func main() {
	kubernetesservice.DoInit()

	router := initrouter.InitRouter()
	_ = router.Run(":18080")
}
