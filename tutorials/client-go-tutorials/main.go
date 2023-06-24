package main

import (
	"client-go-tutorials/action"
	"client-go-tutorials/query"
	"flag"
	"fmt"
	"log"
	"path/filepath"

	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"
)

func main() {
	var kubeconfig *string
	var actionFlag *string

	// 试图取到当前账号的家目录
	if home := homedir.HomeDir(); home != "" {
		// 如果能取到，就把家目录下的.kube/config作为默认配置文件
		kubeconfig = flag.String("kubeconfig", filepath.Join(home, ".kube", "config"), "(optional) absolute path to the kubeconfig file")
	} else {
		// 如果取不到，就没有默认配置文件，必须通过kubeconfig参数来指定
		kubeconfig = flag.String("kubeconfig", "", "absolute path to the kubeconfig file")
	}

	actionFlag = flag.String("action", "list-pod", "指定实际操作功能")

	flag.Parse()

	log.Println("解析命令完毕，开始加载配置文件")

	// 加载配置文件
	config, err := clientcmd.BuildConfigFromFlags("", *kubeconfig)
	if err != nil {
		panic(err.Error())
	}

	// 用clientset类来执行后续的查询操作
	clientset, err := kubernetes.NewForConfig(config)
	if err != nil {
		panic(err.Error())
	}

	log.Printf("加载配置文件完毕，即将执行业务 [%v]\n", *actionFlag)

	var actionInterface action.Action

	// 注意，如果有新的功能类实现，就在这里添加对应的处理
	switch *actionFlag {
	case "list-pod":
		listPod := action.ListPod{}
		actionInterface = &listPod
	case "conflict":
		conflict := action.Confilct{}
		actionInterface = &conflict
	case "controller":
		controllerDemo := action.ControllerDemo{}
		actionInterface = &controllerDemo
	case "label":
		label := action.Lable{}
		actionInterface = &label
	case "query-from-remote":
		queryFromRemote := query.QueryFromRemote{}
		actionInterface = &queryFromRemote
	case "query-from-local-cache":
		queryFromLocalCache := query.QueryFromLocalCache{}
		actionInterface = &queryFromLocalCache
	case "meta-demo":
		metaDemo := action.MetaDemo{}
		actionInterface = &metaDemo
	}

	err = actionInterface.DoAction(clientset)
	if err != nil {
		fmt.Printf("err: %v\n", err)
	} else {
		log.Println("执行完成")
	}
}
