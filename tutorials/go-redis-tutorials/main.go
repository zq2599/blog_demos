/*
package main

import (
	"fmt"

	"github.com/gomodule/redigo/redis"
)

var c redis.Conn

var pool *redis.Pool

func init() {
	fmt.Println("连接成功")

	pool = &redis.Pool{MaxIdle: 16,
		MaxActive:   0,
		IdleTimeout: 300,
		Dial: func() (redis.Conn, error) {
			return redis.Dial("tcp", "120.24.56.76:16379")
		}}
}

func test001() {
	reply, err := c.Do("Set", "aaa", 101)
	if err != nil {
		fmt.Printf("2. err: %v\n", err)
		return
	}

	fmt.Printf("reply: %v\n", reply)
}

func test002() {
	reply, err := c.Do("Get", "aaa")
	if err != nil {
		fmt.Printf("3. err: %v\n", err)
		return
	}

	fmt.Printf("reply: %v\n", reply)

	value, err := redis.Int(reply, nil)
	if err != nil {
		fmt.Printf("5. err: %v\n", err)
		return
	}

	fmt.Printf("value: %v\n", value)

}

func test003() {
	reply, err := c.Do("MSet", "a0", 0, "a1", 1)
	if err != nil {
		fmt.Printf("6. err: %v\n", err)
		return
	}

	fmt.Printf("reply: %v\n", reply)
}

func test004() {
	reply, err := c.Do("MGet", "a0", "a1")
	if err != nil {
		fmt.Printf("7. err: %v\n", err)
		return
	}

	fmt.Printf("reply: %v\n", reply)

	rlt, err := redis.Ints(reply, nil)

	if err != nil {
		fmt.Printf("test004. err: %v\n", err)
		return
	}

	fmt.Printf("rlt: %v\n", rlt)

	for _, v := range rlt {
		fmt.Printf("v: %v\n", v)
	}
}

func test005() {
	reply, err := c.Do("expire", "a0", 10)
	if err != nil {
		fmt.Printf("7. err: %v\n", err)
		return
	}

	fmt.Printf("reply: %v\n", reply)
}

func test006() {
	reply, err := c.Do("lpush", "list0", 101, 102, 103)
	if err != nil {
		fmt.Printf("test006. err: %v\n", err)
		return
	}

	fmt.Printf("reply: %v\n", reply)
}

func test007() {
	reply, err := c.Do("lpop", "list0")
	if err != nil {
		fmt.Printf("test007. err: %v\n", err)
		return
	}

	fmt.Printf("reply: %v\n", reply)

	rlt, err := redis.Int(reply, nil)

	if err != nil {
		fmt.Printf("test007. err: %v\n", err)
		return
	}

	fmt.Printf("rlt: %v\n", rlt)
}

func test008() {
	reply, err := c.Do("HSet", "set0", "aaa", 103)
	if err != nil {
		fmt.Printf("test008. err: %v\n", err)
		return
	}

	fmt.Printf("reply: %v\n", reply)
}

func test009() {

	reply, err := c.Do("HGet", "set0", "aaa")
	if err != nil {
		fmt.Printf("test009. err: %v\n", err)
		return
	}

	fmt.Printf("reply: %v\n", reply)

	rlt, err := redis.Int(reply, nil)

	if err != nil {
		fmt.Printf("test009. err: %v\n", err)
		return
	}

	fmt.Printf("rlt: %v\n", rlt)
}

func main() {
	defer pool.Close()
	defer c.Close()

	c = pool.Get()
	test009()
}
*/

package main

import (
	"context"
	"fmt"
	"github.com/olivere/elastic/v7"
	"log"
	"os"
	"reflect"
)

/*
func main() {
	config := sarama.NewConfig()
	config.Producer.RequiredAcks = sarama.WaitForAll          // 发送完数据需要leader和follow都确认
	config.Producer.Partitioner = sarama.NewRandomPartitioner // 新选出一个partition
	config.Producer.Return.Successes = true                   // 成功交付的消息将在success channel返回

	// 构造一个消息
	msg := &sarama.ProducerMessage{}
	msg.Topic = "web_log"
	msg.Value = sarama.StringEncoder("this is a test log")
	// 连接kafka
	client, err := sarama.NewSyncProducer([]string{"120.24.56.76:9093"}, config)
	if err != nil {
		fmt.Println("producer closed, err:", err)
		return
	}
	defer client.Close()
	// 发送消息
	pid, offset, err := client.SendMessage(msg)
	if err != nil {
		fmt.Println("send msg failed, err:", err)
		return
	}
	fmt.Printf("pid:%v offset:%v\n", pid, offset)
}
*/
/*
func main() {
	config := sarama.NewConfig()
	config.Producer.RequiredAcks = sarama.WaitForAll
	config.Producer.Partitioner = sarama.NewRandomPartitioner
	config.Producer.Return.Successes = true

	msg := &sarama.ProducerMessage{}
	msg.Topic = "web_log"
	msg.Value = sarama.StringEncoder("this is a test log")

	// 连接kafka
	client, err := sarama.NewSyncProducer([]string{"120.24.56.76:9093"}, config)

	if err != nil {
		fmt.Printf("1. err: %v\n", err)
		return
	}

	defer client.Close()

	pid, offset, err := client.SendMessage(msg)
	if err != nil {
		fmt.Printf("2. err: %v\n", err)
		return
	}

	fmt.Printf("pid: %v\n", pid)
	fmt.Printf("offset: %v\n", offset)
}
*/

/* func main() {
	consumer, err := sarama.NewConsumer([]string{"120.24.56.76:9093"}, nil)
	if err != nil {
		fmt.Printf("1. err: %v\n", err)
		return
	}

	partitionList, err := consumer.Partitions("web_log")
	if err != nil {
		fmt.Printf("2. err: %v\n", err)
		return
	}

	fmt.Printf("partitionList: %v\n", partitionList)

	for partition := range partitionList {
		fmt.Println("检查partition")
		// 处理每一个分区
		pc, err := consumer.ConsumePartition("web_log", int32(partition), sarama.OffsetOldest)

		if err != nil {
			fmt.Printf("3. err: %v\n", err)
			return
		}

		defer pc.AsyncClose()

		// 异步消费
		go func(p sarama.PartitionConsumer) {
			for msg := range p.Messages() {
				fmt.Printf("msg, patition: %v, offset: %v, key: %v, value: %v\n", msg.Partition, msg.Offset, string(msg.Key), string(msg.Value))
			}
		}(pc)
	}

	time.Sleep(60 * time.Second)

}
*/

/* type Person struct {
	Name    string `json:"name"`
	Age     int    `json:"age"`
	Married bool   `json:"married"`
}

func main() {
	client, err := elastic.NewClient(elastic.SetURL("http://120.24.56.76:9200"))
	if err != nil {
		// Handle error
		panic(err)
	}

	fmt.Println("connect to es success")
	p1 := Person{Name: "lmh", Age: 18, Married: false}
	put1, err := client.Index().
		Index("user").
		BodyJson(p1).
		Do(context.Background())
	if err != nil {
		// Handle error
		panic(err)
	}
	fmt.Printf("Indexed user %s to index %s, type %s\n", put1.Id, put1.Index, put1.Type)
}
*/

// 定义数据结构，即索引结构
/* type Person struct {
	Name    string `json:"name"`
	Age     int    `json:"age"`
	Married bool   `json:"married"`
}

func main() {
	esClient, err := elastic.NewClient(elastic.SetURL("http://120.24.56.76:9200"))
	if err != nil {
		fmt.Printf("1. err: %v\n", err)
		// 直接抛出
		panic(err)
	}

	fmt.Printf("1. esClient: %v\n", esClient)

	p1 := Person{Name: "Tome", Age: 11, Married: false}
	put1, err := esClient.Index().Index("person").BodyJson(p1).Do(context.Background())

	if err != nil {
		fmt.Printf("2. err: %v\n", err)
		// 直接抛出
		panic(err)
	}

	fmt.Printf("put1: %v\n", put1)
}
*/
var INDEX_NAME = "employee"
var client *elastic.Client
var host = "http://120.24.56.76:9200"

type Employee struct {
	FirstName string   `json:"first_name"`
	LastName  string   `json:"last_name"`
	Age       int      `json:"age"`
	About     string   `json:"about"`
	Interests []string `json:"interests"`
}

func init() {
	errorlog := log.New(os.Stdout, "APP", log.LstdFlags)

	var err error

	client, err = elastic.NewClient(elastic.SetErrorLog(errorlog), elastic.SetURL(host))
	if err != nil {
		fmt.Printf("3. err: %v\n", err)
		return
	}

	info, code, err := client.Ping(host).Do(context.Background())
	if err != nil {
		fmt.Printf("4. err: %v\n", err)
		return
	}

	fmt.Printf("code: %v\n", code)
	fmt.Printf("info: %v\n", info)

	esversion, err := client.ElasticsearchVersion(host)
	if err != nil {
		fmt.Printf("5. err: %v\n", err)
		return
	}
	fmt.Printf("esversion: %v\n", esversion)

	nodeInfo, err := client.NodesInfo().Do(context.Background())
	if err != nil {
		fmt.Printf("6. err: %v\n", err)
		return
	}

	fmt.Printf("cluster name: %v\n", nodeInfo.ClusterName)

	for key, nodesInfoNode := range nodeInfo.Nodes {
		fmt.Printf("key: %v\n", string(key))
		fmt.Printf("TransportAddress: %v\n", nodesInfoNode.TransportAddress)
		fmt.Printf("Host: %v\n", nodesInfoNode.Host)
		fmt.Printf("IP: %v\n", nodesInfoNode.IP)
		fmt.Printf("OS: %v\n", *nodesInfoNode.OS)
	}

}

// 创建
func create() {
	// 使用结构体
	employee := Employee{"Tom-2", "Smith", 18, "I like to collect rock albums", []string{"sports", "music"}}
	put1, err := client.Index().Index(INDEX_NAME).Id("3").BodyJson(employee).Do(context.Background())
	if err != nil {
		fmt.Printf("7. err: %v\n", err)
		return
	}
	fmt.Printf("put1: %v\n", put1)

	// 使用字符串
	e2 := `{"first_name":"John-2","last_name":"Smith","age":25,"about":"I love sports","interests":["football","drinking"]}`
	put2, err := client.Index().Index(INDEX_NAME).Id("4").BodyJson(e2).Do(context.Background())
	if err != nil {
		fmt.Printf("7. err: %v\n", err)
		return
	}
	fmt.Printf("put2: %v\n", put2)
}

// 删除
func delete() {
	resp, err := client.Delete().Index(INDEX_NAME).Id("1").Do(context.Background())
	if err != nil {
		fmt.Printf("8. err: %v\n", err)
		return
	}

	fmt.Printf("resp: %v\n", resp)
}

// 修改
func update() {
	resp, err := client.Update().Index(INDEX_NAME).Id("2").Doc(map[string]interface{}{"first_name": "HelloWorld"}).Do(context.Background())

	if err != nil {
		fmt.Printf("9. err: %v\n", err)
		return
	}

	fmt.Printf("resp: %v\n", resp)
}

// 查找
func search() {
	// 通过id查找
	resp, err := client.Get().Index("employee").Id("2").Do(context.Background())
	if err != nil {
		fmt.Printf("10. err: %v\n", err)
		return
	}

	fmt.Printf("found: %v\n", resp.Found)
	fmt.Printf("resp.Source: %v\n", string(resp.Source))
	fmt.Printf("resp.Fields: %v\n", resp.Fields)

	if resp.Found {
		for key, value := range resp.Fields {
			fmt.Printf("key: %v\n", key)
			fmt.Printf("value: %v\n", value)
		}
	}

}

// 从结果中取出每一个转为对象
func printEmployee(res *elastic.SearchResult, err error) {
	if err != nil {
		fmt.Printf("x. err: %v\n", err)
		return
	}

	var typ Employee

	for _, item := range res.Each(reflect.TypeOf(typ)) {
		t := item.(Employee)
		fmt.Printf("t: %v\n", t)
	}
}

func query() {
	var res *elastic.SearchResult
	var err error

	fmt.Println("查全部")
	// 全部
	res, err = client.Search(INDEX_NAME).Do(context.Background())
	printEmployee(res, err)

	fmt.Println("字段匹配")
	// 字段匹配
	q := elastic.NewQueryStringQuery("first_name:HelloWorld")
	res, err = client.Search(INDEX_NAME).Query(q).Do(context.Background())
	printEmployee(res, err)

}

func main() {
	/* fmt.Println("开始新增操作")
	create()
	fmt.Println("开始删除操作")
	delete()*/

	fmt.Println("开始查找操作")
	query()
}
