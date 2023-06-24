package handler_test

import (
	"client-go-unit-tutorials/handler"
	initrouter "client-go-unit-tutorials/init_router"
	kubernetesservice "client-go-unit-tutorials/kubernetes_service"
	"fmt"
	"log"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
	"k8s.io/client-go/kubernetes/fake"
)

// 参考 client-go/examples/fake-client/main_test.go/main_test.go
func TestQueryPodsByDeploymentName(t *testing.T) {
	client := fake.NewSimpleClientset()
	kubernetesservice.SetClient(client)

	router := initrouter.InitRouter()
	w := httptest.NewRecorder()

	url := fmt.Sprintf("%s?%s=%s&%s=%s",
		initrouter.PATH_QUERY_PODS_BY_LABEL_APP,
		handler.PARAM_NAMESPACE,
		"client-go-tutorial",
		handler.PARAM_APP,
		"nginx-app")

	log.Printf("request url : %s", url)

	req, _ := http.NewRequest(http.MethodGet, url, nil)
	router.ServeHTTP(w, req)
	assert.Equal(t, http.StatusOK, w.Code)
}
