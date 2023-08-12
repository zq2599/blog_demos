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

var identity string

func startLeaderElecting(clientset *kubernetes.Clientset) {
	lock := &resourcelock.LeaseLock{
		LeaseMeta: metav1.ObjectMeta{
			Name:      "leader-tutorials",
			Namespace: NAMESPACE,
		},
		Client: clientset.CoordinationV1(),
		LockConfig: resourcelock.ResourceLockConfig{
			Identity: identity,
		},
	}
}

func main() {
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

	stop := make(chan struct{})
	defer close(stop)

	id := uuid.New().String()

	go func() {

		lock := &resourcelock.LeaseLock{
			LeaseMeta: metav1.ObjectMeta{
				Name:      "leader-tutorials",
				Namespace: NAMESPACE,
			},
			Client: clientset.CoordinationV1(),
			LockConfig: resourcelock.ResourceLockConfig{
				Identity: id,
			},
		}

		leaderelection.RunOrDie(context.TODO(), leaderelection.LeaderElectionConfig{
			Lock:            lock,
			ReleaseOnCancel: true,
			LeaseDuration:   10 * time.Second,
			RenewDeadline:   5 * time.Second,
			RetryPeriod:     2 * time.Second,
			Callbacks: leaderelection.LeaderCallbacks{
				OnStartedLeading: func(ctx context.Context) {
					klog.Infof("Leader election success [%s]", id)
					// 就像抢分布式锁一样，当前进程选举成功的时候，这的代码就会被执行，
					// 所以，在这里填写抢锁成功的业务逻辑吧，本例中就是监听service变化，然后修改pod的label
					CreateAndStartController(clientset.CoreV1().RESTClient(), &v1.Service{}, "services", NAMESPACE, stop)
				},
				OnStoppedLeading: func() {
					// 失去了leader时的逻辑
					klog.Infof("leader lost: %s", id)
					os.Exit(0)
				},
				OnNewLeader: func(identity string) {
					// 收到通知，知道最终的选举结果
					if identity == id {
						// I just got the lock
						return
					}
					klog.Infof("new leader elected: %s", identity)
				},
			},
		})

	}()

	// 这里可以继续做其他事情
	klog.Infof("other business will be execute here [%s]", id)

	select {}
}
