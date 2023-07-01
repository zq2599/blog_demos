package handler_test

import (
	"client-go-unit-tutorials/handler"
	initrouter "client-go-unit-tutorials/init_router"
	kubernetesservice "client-go-unit-tutorials/kubernetes_service"
	"client-go-unit-tutorials/unitfake"
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/suite"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/kubernetes/fake"
)

const (
	TEST_NAMESPACE       = "client-go-tutorials"
	TEST_POD_NAME_PREFIX = "nginx-pod-"
	TEST_IMAGE           = "nginx:latest"
	TEST_LABEL_APP       = "nginx-app"
	TEST_POD_NUM         = 3
)

// 用于保存web响应的body
type ResponseNames struct {
	Message string   `json:"message"`
	Names    []string `json:"names"`
}

// 1. 定义suite数据结构
type MySuite struct {
	suite.Suite
	ctx       context.Context
	cancel    context.CancelFunc
	clientSet kubernetes.Interface
	router    *gin.Engine
}

// 2. 定义初始化
func (mySuite *MySuite) SetupTest() {
	client := fake.NewSimpleClientset()
	kubernetesservice.SetClient(client)

	mySuite.ctx, mySuite.cancel = context.WithCancel(context.Background())
	mySuite.clientSet = client
	mySuite.router = initrouter.InitRouter()

	// 初始化数据，创建namespace
	if err := unitfake.CreateNamespace(mySuite.ctx, client, TEST_NAMESPACE); err != nil {
		log.Fatalf("create namespace error, %s", err.Error())
	}

	// 初始化数据，创建pod
	createPod(mySuite.ctx, client, 3)
}

// 3. 定义结束
func (mySuite *MySuite) TearDownTest() {
	mySuite.cancel()
}

// 4. 启动测试
func TestBasicCrud(t *testing.T) {
	suite.Run(t, new(MySuite))
}

// 5. 定义测试集合
func (mySuite *MySuite) TestBasicCrud() {
	// 5.1 若有需要，执行monkey.Patch
	// 5.2 若执行了monkey.Patch，需要执行defer monkey.UnpatchAll()

	// 5.3 执行单个测试
	// 参考 client-go/examples/fake-client/main_test.go/main_test.go
	mySuite.Run("常规查询", func() {
		url := fmt.Sprintf("%s?%s=%s&%s=%s",
			initrouter.PATH_QUERY_PODS_BY_LABEL_APP,
			handler.PARAM_NAMESPACE,
			TEST_NAMESPACE,
			handler.PARAM_APP,
			TEST_LABEL_APP)

		code, body, error := singleTest(mySuite, url)

		if error != nil {
			mySuite.Fail("singleTest error, %v", error)
			return
		}

		// 检查结果
		mySuite.EqualValues(http.StatusOK, code)

		//
		check(&mySuite.Suite, body, TEST_POD_NUM)

		log.Printf("response : %s", body)
	})
}

// 6. 定义单个测试
func singleTest(mySuite *MySuite, url string) (int, string, error) {
	log.Printf("start singleTest, request url : %s", url)
	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, url, nil)
	mySuite.router.ServeHTTP(w, req)
	return w.Code, w.Body.String(), nil
}

// 7. 辅助方法，用于创建多个pod
func createPod(context context.Context, client kubernetes.Interface, num int) {
	for i := 0; i < num; i++ {
		if err := unitfake.CreatePods(context,
			client,
			TEST_NAMESPACE,
			fmt.Sprintf("%s%d", TEST_POD_NAME_PREFIX, i),
			TEST_IMAGE,
			TEST_LABEL_APP); err != nil {
			log.Fatalf("create pod [%d] error, %s", i, err.Error())
		}
	}
}

// 8. 辅助方法，解析web响应，检查结果是否符合预期
func check(suite *suite.Suite, body string, expectNum int) {
	suite.NotNil(body)
	response := &ResponseNames{}

	err := json.Unmarshal([]byte(body), response)

	if err != nil {
		log.Fatalf("unmarshal response error, %s", err.Error())
	}

	suite.EqualValues(expectNum, len(response.Names))
}
