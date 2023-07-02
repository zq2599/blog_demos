package handler_test

import (
	"client-go-unit-tutorials/handler"
	"client-go-unit-tutorials/initor"
	kubernetesservice "client-go-unit-tutorials/kubernetes_service"
	"client-go-unit-tutorials/unittesthelper"
	"context"
	"fmt"
	"log"
	"net/http"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/suite"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/kubernetes/fake"
)

// 1. 定义suite数据结构
type MySuite struct {
	suite.Suite
	ctx       context.Context
	cancel    context.CancelFunc
	clientSet kubernetes.Interface
	router    *gin.Engine
}

// 2. 单元测试的初始化操作
func (mySuite *MySuite) SetupTest() {
	client := fake.NewSimpleClientset()
	kubernetesservice.SetClient(client)

	mySuite.ctx, mySuite.cancel = context.WithCancel(context.Background())
	mySuite.clientSet = client
	mySuite.router = initor.InitRouter()

	// 初始化数据，创建namespace
	if err := kubernetesservice.CreateNamespace(mySuite.ctx, client, unittesthelper.TEST_NAMESPACE); err != nil {
		log.Fatalf("create namespace error, %s", err.Error())
	}

	// 初始化数据，创建pod
	unittesthelper.CreatePod(mySuite.ctx, client, 3)
}

// 3. 定义测试完成后的收尾工作，例如清理一些资源
func (mySuite *MySuite) TearDownTest() {

	// 删除namespace
	if err := kubernetesservice.DeleteNamespace(mySuite.ctx, kubernetesservice.GetClient(), unittesthelper.TEST_NAMESPACE); err != nil {
		log.Fatalf("delete namespace error, %s", err.Error())
	}

	mySuite.cancel()
}

// 4. 启动测试集
func TestBasicCrud(t *testing.T) {
	suite.Run(t, new(MySuite))
}

// 5. 定义测试集
func (mySuite *MySuite) TestBasicCrud() {
	// 5.1 若有需要，执行monkey.Patch
	// 5.2 若执行了monkey.Patch，需要执行defer monkey.UnpatchAll()

	// 5.3 执行单个测试
	// 参考 client-go/examples/fake-client/main_test.go/main_test.go
	mySuite.Run("常规查询", func() {
		url := fmt.Sprintf("%s?%s=%s&%s=%s",
			initor.PATH_QUERY_PODS_BY_LABEL_APP,
			handler.PARAM_NAMESPACE,
			unittesthelper.TEST_NAMESPACE,
			handler.PARAM_APP,
			unittesthelper.TEST_LABEL_APP)

		code, body, error := unittesthelper.SingleTest(mySuite.router, url)

		if error != nil {
			mySuite.Fail("SingleTest error, %v", error)
			return
		}

		// 检查返回码
		mySuite.EqualValues(http.StatusOK, code)

		// 检查结果
		unittesthelper.Check(&mySuite.Suite, body, unittesthelper.TEST_POD_NUM)
	})
}
