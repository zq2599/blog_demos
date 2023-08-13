package main

import (
	"context"
	"flag"
	"os"
	"path/filepath"
	"time"

	"github.com/google/uuid"
	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/tools/leaderelection"
	"k8s.io/client-go/tools/leaderelection/resourcelock"
	"k8s.io/client-go/util/homedir"
	"k8s.io/klog/v2"
)

const (
	NAMESPACE = "client-go-tutorials"
)

// 用于表明当前进程身份的全局变量，目前用的是uuid
var processIndentify string

// initOrDie client有关的初始化操作
func initOrDie() *kubernetes.Clientset {
	klog.Infof("[%s]开始初始化kubernetes客户端相关对象", processIndentify)
	var kubeconfig *string
	var master string

	// 试图取到当前账号的家目录
	if home := homedir.HomeDir(); home != "" {
		// 如果能取到，就把家目录下的.kube/config作为默认配置文件
		kubeconfig = flag.String("kubeconfig", filepath.Join(home, ".kube", "config"), "(optional) absolute path to the kubeconfig file")
		master = ""
	} else {
		// 如果取不到，就没有默认配置文件，必须通过kubeconfig参数来指定
		flag.StringVar(kubeconfig, "kubeconfig", "", "absolute path to the kubeconfig file")
		flag.StringVar(&master, "master", "", "master url")
		flag.Parse()
	}

	config, err := clientcmd.BuildConfigFromFlags(master, *kubeconfig)
	if err != nil {
		klog.Fatal(err)
	}

	clientset, err := kubernetes.NewForConfig(config)
	if err != nil {
		klog.Fatal(err)
	}
	klog.Infof("[%s]kubernetes客户端相关对象创建成功", processIndentify)
	return clientset
}

// startLeaderElection 选主的核心逻辑代码
func startLeaderElection(ctx context.Context, clientset *kubernetes.Clientset, stop chan struct{}) {
	klog.Infof("[%s]创建选主所需的锁对象", processIndentify)
	// 创建锁对象
	lock := &resourcelock.LeaseLock{
		LeaseMeta: metav1.ObjectMeta{
			Name:      "leader-tutorials",
			Namespace: NAMESPACE,
		},
		Client: clientset.CoordinationV1(),
		LockConfig: resourcelock.ResourceLockConfig{
			Identity: processIndentify,
		},
	}
	klog.Infof("[%s]开始选主", processIndentify)
	// 启动选主操作
	leaderelection.RunOrDie(ctx, leaderelection.LeaderElectionConfig{
		Lock:            lock,
		ReleaseOnCancel: true,
		LeaseDuration:   10 * time.Second,
		RenewDeadline:   5 * time.Second,
		RetryPeriod:     2 * time.Second,
		Callbacks: leaderelection.LeaderCallbacks{
			OnStartedLeading: func(ctx context.Context) {
				klog.Infof("[%s]当前进程是leader，只有leader才能执行的业务逻辑立即开始", processIndentify)
				// 就像抢分布式锁一样，当前进程选举成功的时候，这的代码就会被执行，
				// 所以，在这里填写抢锁成功的业务逻辑吧，本例中就是监听service变化，然后修改pod的label
				CreateAndStartController(ctx, clientset, &v1.Service{}, "services", NAMESPACE, stop)
			},
			OnStoppedLeading: func() {
				// 失去了leader时的逻辑
				klog.Infof("[%s]失去leader身份，不再是leader了", processIndentify)
				os.Exit(0)
			},
			OnNewLeader: func(identity string) {
				// 收到通知，知道最终的选举结果
				if identity == processIndentify {
					klog.Infof("[%s]选主结果出来了，当前进程就是leader", processIndentify)
					// I just got the lock
					return
				}
				klog.Infof("[%s]选主结果出来了，leader是 : [%s]", processIndentify, identity)
			},
		},
	})
}

func main() {
	// 一次性确定当前进程身份
	processIndentify = uuid.New().String()

	// 准备一个带cancel的context，这样在主程序退出的时候，可以将停止的信号传递给业务
	ctx, cancel := context.WithCancel(context.Background())
	// 这个是用来停止controller的
	stop := make(chan struct{})

	// 主程序结束的时候，下面的操作可以将业务逻辑都停掉
	defer func() {
		close(stop)
		cancel()
	}()

	// 初始化clientSet配置，因为是启动阶段，所以必须初始化成功，否则进程退出
	clientset := initOrDie()

	// 在一个新的协程中执行选主逻辑，以及选主成功的后的逻辑
	go startLeaderElection(ctx, clientset, stop)

	// 这里可以继续做其他事情
	klog.Infof("选主的协程已经在运行，接下来可以执行其他业务 [%s]", processIndentify)

	select {}
}
