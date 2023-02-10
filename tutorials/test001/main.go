package main

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"log"
	"strconv"
	"test001/swagger"
	"time"

	"github.com/antihax/optional"
)

// 测试用的topic
const TEST_TOPIC = "bridge-quickstart-topic"

const TEST_GROUP = "client-sdk-group"

const CONSUMER_NAME = "client-sdk-consumer-002"

// strimzi bridge地址
const BASE_PATH = "http://42.193.162.141:31331"

var client *swagger.APIClient

func init() {
	configuration := swagger.NewConfiguration()
	configuration.BasePath = BASE_PATH
	client = swagger.NewAPIClient(configuration)
}

func getAllTopics() ([]string, error) {
	array, response, err := client.TopicsApi.ListTopics(context.Background())

	if err != nil {
		log.Printf("getAllTopics err: %v\n", err)
		return nil, err
	}

	log.Printf("response: %v", response)

	return array, nil
}

// 发送消息(异步模式，不会收到offset返回)
func sendAsync(info string) error {
	log.Print("send [" + info + "]")
	_, response, err := client.ProducerApi.Send(context.Background(),
		TEST_TOPIC,
		swagger.ProducerRecordList{
			Records: []swagger.ProducerRecord{
				{Value: "message from go swagger SDK"},
			},
		},
		&swagger.SendOpts{Async: optional.NewBool(true)},
	)

	if err != nil {
		log.Printf("send err: %v\n", err)
		return err
	}

	log.Printf("response: %v", response.StatusCode)

	return nil
}

func timeStr() string {
	return time.Now().Format("20060102150405")
}

func getBodyStr(body io.ReadCloser) string {
	buf := new(bytes.Buffer)
	buf.ReadFrom(body)
	return buf.String()
}

// 取出swagger特有的error类型，从中提取中有效的错误信息
func getErrorMessage(err error) string {
	e := err.(swagger.GenericSwaggerError)
	return string(e.Body())
}

// 创建consumer
func CreateConsumer(group string, consumerName string) (*swagger.CreatedConsumer, error) {

	consumer, response, err := client.ConsumersApi.CreateConsumer(context.Background(),
		group,
		swagger.Consumer{
			Name:                     consumerName,
			AutoOffsetReset:          "latest",
			FetchMinBytes:            16,
			ConsumerRequestTimeoutMs: 300 * 1000,
			EnableAutoCommit:         false,
			Format:                   "json",
		})

	if err != nil {
		log.Printf("CreateConsumer error : %v", getErrorMessage(err))
		return nil, err
	}

	log.Printf("CreateConsumer response : %v, body [%v]", response, getBodyStr(response.Body))
	log.Printf("consumer : %v", consumer)
	return &consumer, nil
}

// 订阅
func Subsciribe(topic string, consumerGroup string, consumerName string) error {

	response, err := client.ConsumersApi.Subscribe(context.Background(),
		swagger.Topics{Topics: []string{topic}},
		consumerGroup,
		consumerName,
	)

	if err != nil {
		log.Printf("Subscribe error : %v", err)
		return err
	}

	log.Printf("Subscribe response : %v", response)
	return nil
}

// 拉取消息
func Poll(consumerGroup string, consumerName string) error {
	// ctx context.Context, groupid string, name string, localVarOptionals *PollOpts
	recordList, response, err := client.ConsumersApi.Poll(context.Background(), consumerGroup, consumerName, nil)
	if err != nil {
		log.Printf("Poll error : %v", err)
		return err
	}

	log.Printf("Poll response : %v", response)
	fmt.Printf("recordList: %v\n", recordList)
	return nil
}

// 提交offset
func Offset(consumerGroup string, consumerName string) error {
	response, err := client.ConsumersApi.Commit(context.Background(),
		consumerGroup,
		consumerName, nil)

	if err != nil {
		log.Printf("Poll error : %v", err)
		return err
	}

	log.Printf("Offset response : %v", response)
	return nil
}

func main() {

	//topics, err := getAllTopics()
	//if err != nil {
	//	return
	//}
	//
	//fmt.Printf("topics: %v\n", topics)

	for i := 0; i < 10; i++ {
		sendAsync("message from go client " + strconv.Itoa(i))
	}

	// 创建consumer
	//CreateConsumer(TEST_GROUP, CONSUMER_NAME)

	//err := Subsciribe(TEST_TOPIC, TEST_GROUP, CONSUMER_NAME)
	//if err != nil {
	//	fmt.Printf("err : %v\n", err)
	//}

	Poll(TEST_GROUP, CONSUMER_NAME)
}
