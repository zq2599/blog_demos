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

	// CreateAndStartController(clientset.CoreV1().RESTClient(), &v1.Pod{}, "pods", NAMESPACE, stop)

	lock := &resourcelock.LeaseLock{
		LeaseMeta: metav1.ObjectMeta{
			Name:      "leader-tutorials",
			Namespace: NAMESPACE,
		},
		Client: clientset.CoordinationV1(),
		LockConfig: resourcelock.ResourceLockConfig{
			Identity: uuid.New().String(),
		},
	}

	leaderelection.RunOrDie(context.TODO(), leaderelection.LeaderElectionConfig{
		Lock: lock,
		// IMPORTANT: you MUST ensure that any code you have that
		// is protected by the lease must terminate **before**
		// you call cancel. Otherwise, you could have a background
		// loop still running and another process could
		// get elected before your background loop finished, violating
		// the stated goal of the lease.
		ReleaseOnCancel: true,
		LeaseDuration:   60 * time.Second,
		RenewDeadline:   15 * time.Second,
		RetryPeriod:     5 * time.Second,
		Callbacks: leaderelection.LeaderCallbacks{
			OnStartedLeading: func(ctx context.Context) {
				// we're notified when we start - this is where you would
				// usually put your code
				run(ctx)
			},
			OnStoppedLeading: func() {
				// we can do cleanup here
				klog.Infof("leader lost: %s", id)
				os.Exit(0)
			},
			OnNewLeader: func(identity string) {
				// we're notified when new leader elected
				if identity == id {
					// I just got the lock
					return
				}
				klog.Infof("new leader elected: %s", identity)
			},
		},
	})

	CreateAndStartController(clientset.CoreV1().RESTClient(), &v1.Service{}, "services", NAMESPACE, stop)

	select {}
}
