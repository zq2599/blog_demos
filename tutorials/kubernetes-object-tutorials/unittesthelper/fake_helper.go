package unittesthelper

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"net/http/httptest"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/suite"
	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
)

const (
	TEST_NAMESPACE       = "client-go-tutorials"
	TEST_POD_NAME_PREFIX = "nginx-pod-"
	TEST_IMAGE           = "nginx:latest"
	TEST_LABEL_APP       = "nginx-app"
	TEST_POD_NUM         = 3
)

// 数据结构，用于保存web响应的body
type ResponseTypeMeta struct {
	Message    string `json:"message"`
	Kind       string `json:"kind"`
	ApiVersion string `json:"api_version"`
}

// SingleTest 辅助方法，发请求，返回响应
func SingleTest(router *gin.Engine, url string) (int, string, error) {
	log.Printf("start SingleTest, request url : %s", url)
	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, url, nil)
	router.ServeHTTP(w, req)
	return w.Code, w.Body.String(), nil
}

// 9. 辅助方法，解析web响应，检查结果是否符合预期
func Check(suite *suite.Suite, body string, expectNum int) {
	suite.NotNil(body)
	response := &ResponseTypeMeta{}

	err := json.Unmarshal([]byte(body), response)

	if err != nil {
		log.Fatalf("unmarshal response error, %s", err.Error())
	}

	// suite.EqualValues(expectNum, len(response.Names))
}

// CreatePodObj 辅助方法，用于创建pod对象
func CreatePodObj(namespace, name, app, image string) *v1.Pod {
	return &v1.Pod{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Deployment",
			APIVersion: "apps/v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      name,
			Namespace: namespace,
			Labels: map[string]string{
				"app": app,
			},
		},
		Spec: v1.PodSpec{
			Containers: []v1.Container{
				{
					Image: image,
				},
			},
		},
	}
}

// CreateDeployment 单元测试的辅助工具，用于创建namespace
func CreatePods(context context.Context, client kubernetes.Interface, namespace, name, image, app string) error {
	_, err := client.CoreV1().Pods(namespace).Create(context, CreatePodObj(namespace, name, app, image), metav1.CreateOptions{})
	return err
}

// CreatePod 辅助方法，用于创建多个pod
func CreatePod(context context.Context, client kubernetes.Interface, num int) {
	for i := 0; i < num; i++ {
		if err := CreatePods(context,
			client,
			TEST_NAMESPACE,
			fmt.Sprintf("%s%d", TEST_POD_NAME_PREFIX, i),
			TEST_IMAGE,
			TEST_LABEL_APP); err != nil {
			log.Fatalf("create pod [%d] error, %s", i, err.Error())
		}
	}
}
