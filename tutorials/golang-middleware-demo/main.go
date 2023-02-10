package main

import (
	"context"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"reflect"
	"strings"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
)

/*
import (
	"fmt"
	"golang-middleware-demo/yaml"

	_ "github.com/go-sql-driver/mysql"
	"github.com/jmoiron/sqlx"
)

type Person struct {
	UserId   int    `db:"user_id"`
	UserName string `db:"user_name"`
	Sex      string `db:"sex"`
	Email    string `db:"email"`
}

type Place struct {
	Country string `db:"country"`
	City    string `db:"city"`
	TelCode string `db:"telcode"`
}

var Db *sqlx.DB

func init() {
	fmt.Println("start init")
	database, err := sqlx.Open("mysql", "root:888888@tcp(120.24.56.76:3306)/test")

	if err != nil {
		fmt.Printf("err: %v\n", err)
		return
	}

	Db = database

	fmt.Printf("database: %v\n", *database)
	fmt.Println("end init")
}

func test001() {
	r, err := Db.Exec("insert into person(user_name, sex, email)values(?, ?, ?)", "Tom", "male", "Tom@qq.com")

	if err != nil {
		fmt.Printf("1. err: %v\n", err)
		return
	}

	fmt.Printf("r: %v\n", r)

	id, err := r.LastInsertId()

	if err != nil {
		fmt.Printf("2. err: %v\n", err)
		return
	}

	fmt.Printf("id: %v\n", id)
}

func test002() {
	var person []Person

	err := Db.Select(&person, "select user_id, user_name, sex, email from person where user_id>?", 1)

	if err != nil {
		fmt.Printf("3. err: %v\n", err)
		return
	}

	for _, v := range person {
		fmt.Printf("v: %v\n", v)
	}

}

func test003() {
	conf := &yaml.Yaml2Go{}

	if err := yaml.ReadConf(conf); err != nil {
		fmt.Printf("err: %v\n", err)
		return
	}

	fmt.Printf("conf: %v\n", *conf)
	fmt.Printf("config: %v\n", conf.Config)
}

func main() {
	defer Db.Close()
	test003()
}
*/

// zookeeper部分
/*
import (
	"fmt"
	"time"

	"github.com/samuel/go-zookeeper/zk"
)

var conn *zk.Conn

func init() {
	var err error
	conn, _, err = zk.Connect([]string{"120.24.56.76:12181"}, 10*time.Second)
	if err != nil {
		fmt.Printf("getConnection err: %v\n", err)
		return
	}

	fmt.Println("connecting success")
}

func test001() {
	var flags int32 = 0
	//(path string, data []byte, flags int32, acl []ACL)
	conn.Create("/aaa/bbb", []byte("abc"), flags, zk.WorldACL(zk.PermAll))
}

func test002() {
	children, _, err := conn.Children("/aaa")
	if err != nil {
		fmt.Printf("test002 1 err: %v\n", err)
	}

	fmt.Printf("children: %v\n", children)
}

func test003() {
	children, state, err := conn.Get("/aaa/bbb")
	if err != nil {
		fmt.Printf("test002 1 err: %v\n", err)
	}

	fmt.Printf("state: %v\n", state)

	fmt.Printf("children: %v\n", string(children))
}

func main() {
	defer conn.Close()
	test003()
}

*/

/* import (
	"fmt"
	"github.com/Shopify/sarama"
)

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
	client, err := sarama.NewSyncProducer([]string{"120.24.56.76:9092"}, config)
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
type Person struct {
	Name   string
	Gender string
	Age    int
}

func sayHello(w http.ResponseWriter, r *http.Request) {
	// 解析指定文件生成模板对象
	tmpl, err := template.ParseFiles("./hello.html")
	if err != nil {
		fmt.Printf("1. err: %v\n", err)
		return
	}

	person := &Person{"Tom", "Male", 11}

	tmpl.Execute(w, person)
}

func main() {
	http.HandleFunc("/", sayHello)
	err := http.ListenAndServe(":9090", nil)
	if err != nil {
		fmt.Printf("2. err: %v\n", err)
	}
}
*/

func test001() {
	resp, err := http.Get("http://42.193.162.141:31331/topics")
	if err != nil {
		fmt.Printf("test001 1 err: %v\n", err)
		return
	}

	fmt.Printf("resp.Header: %v\n", resp.Header)

	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Printf("test001 2 err: %v\n", err)
		return
	}

	fmt.Printf("body: %v\n", string(body))
}

var wg sync.WaitGroup

func worker(ctx context.Context) {
LOOP:
	for {
		fmt.Println("execute worker")
		time.Sleep(time.Second)
		select {
		case <-ctx.Done():
			break LOOP
		default:
			fmt.Println("go to default case")
		}
	}

	wg.Done()
}

func test002() {
	ctx, cancel := context.WithCancel(context.Background())
	wg.Add(1)
	go worker(ctx)
	time.Sleep(3 * time.Second)
	fmt.Println("通知协程结束")
	cancel()
	wg.Wait()
	fmt.Println("完全结束")

}

func test003() {
	d := time.Now().Add(1 * time.Second)
	ctx, cancel := context.WithDeadline(context.Background(), d)

	defer cancel()
LOOP:
	for {
		select {
		case <-ctx.Done():
			fmt.Println("收到Done信号")
			break LOOP
		default:
			fmt.Println("走的是default")
		}
	}

	fmt.Println("结束")
}

type TraceCode string

func worker01(ctx context.Context) {
	key := TraceCode("TRACE")
	traceCodeValue, ok := ctx.Value(key).(string)
	if !ok {
		fmt.Println("无效的tracecode")
	}

LOOP:
	for {
		fmt.Printf("协程的一次select循环，traceCodeValue: %v\n", traceCodeValue)
		time.Sleep(1 * time.Second)
		select {
		case <-ctx.Done():
			fmt.Println("协程收到结束的消息，跳出循环")
			break LOOP
		default:
		}
	}

	wg.Done()
	fmt.Println("协程的wg信号已经发出，协程结束")
}

func test005() {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	ctx = context.WithValue(ctx, TraceCode("TRACE"), "1234567890")
	wg.Add(1)

	go worker01(ctx)

	fmt.Println("主线程用wg等待")
	wg.Wait()
	cancel()
	fmt.Println("主线程结束")
}

type Person struct {
	Name string `json:"name"`
	Age  int    `json:"age"`
}

func test006() {
	p := Person{"Tom", 11}
	fmt.Printf("p: %v\n", p)
	s, err := json.Marshal(p)
	if err != nil {
		fmt.Printf("err: %v\n", err)
		return
	}
	fmt.Printf("s: \n%v\n", string(s))
}

func test007() {
	student := make(map[string]interface{})
	student["name"] = "Jerry"
	student["age"] = 13
	b, err := json.Marshal(student)
	if err != nil {
		fmt.Printf("err: %v\n", err)
		return
	}
	fmt.Printf("b: %v\n", string(b))
}

// {"name":"Tom","age":11}
func test008() {
	b := []byte(`{"name":"Tom1","age":11}`)
	var person Person
	err := json.Unmarshal(b, &person)
	if err != nil {
		fmt.Printf("err: %v\n", err)
		return
	}

	fmt.Printf("person: %v\n", person)

}

func test009() {
	b := []byte(`{"name":"Tom1","age":11}`)
	var i interface{}
	err := json.Unmarshal(b, &i)
	if err != nil {
		fmt.Printf("err: %v\n", err)
		return
	}

	fmt.Printf("i: %v\n", i)

	m := i.(map[string]interface{})
	fmt.Printf("m: %v\n", m)

	for _, v := range m {
		switch vv := v.(type) {
		case float64:
			fmt.Printf("float64: %v\n", vv)
		case string:
			fmt.Printf("string: %v\n", vv)
		default:
			fmt.Printf("其他: %v\n", vv)
		}

	}

}

func reflect_type(a interface{}) {
	t := reflect.TypeOf(a)
	fmt.Printf("t: %v\n", t)

	k := t.Kind()
	fmt.Printf("k: %v\n", k)

	switch k {
	case reflect.Chan:
		fmt.Println("chan类型")
	case reflect.String:
		fmt.Println("string类型")
	default:
		fmt.Printf("其他类型[%v]\n", k)
	}
}

func test010() {
	reflect_type(1.1)
}

func current() string {
	return time.Now().Format("[2006-01-02 15:04:05]")
}

func test011() {
	// 创建路由
	r := gin.Default()

	r.GET("/hello", func(ctx *gin.Context) {
		ctx.String(http.StatusOK, fmt.Sprintf("hello world %s", current()))
	})

	r.GET("/test", func(ctx *gin.Context) {
		name := ctx.DefaultQuery("name", "yy")
		ctx.String(http.StatusOK, fmt.Sprintf("hello [%s], from test %s", name, current()))
	})

	r.Run(":8080")
}

func test012() {
	r := gin.Default()
	// 限制上传最大尺寸
	r.MaxMultipartMemory = 8 << 20

	r.POST("/upload", func(ctx *gin.Context) {
		file, err := ctx.FormFile("file")
		if err != nil {
			fmt.Printf("1. err: %v\n", err)
			ctx.String(http.StatusBadRequest, fmt.Sprintf("get file from form error %s", current()))
			return
		}

		ctx.SaveUploadedFile(file, file.Filename)
		ctx.String(http.StatusOK, "文件["+file.Filename+"]上传成功")

	})

	r.Run(":8080")

}

func test013() {
	r := gin.Default()
	// 限制上传最大尺寸
	r.MaxMultipartMemory = 8 << 20

	r.POST("/uploads", func(ctx *gin.Context) {

		form, err := ctx.MultipartForm()
		if err != nil {
			fmt.Printf("2. err: %v\n", err)
			ctx.String(http.StatusBadRequest, fmt.Sprintf("get files from form error %s", current()))
			return
		}

		files := form.File["files"]

		fileNames := []string{}

		for _, file := range files {
			// 逐个处理
			err := ctx.SaveUploadedFile(file, file.Filename)
			if err != nil {
				fmt.Printf("3. err: %v\n", err)
				ctx.String(http.StatusBadRequest, fmt.Sprintf("seave files error %s", current()))
				return
			}

			fileNames = append(fileNames, file.Filename)
		}

		ctx.String(http.StatusOK, "文件["+strings.Join(fileNames, ",")+"]上传成功")

	})

	r.Run(":8080")

}

func test015() {
	r := gin.Default()

	// 路由组1是专门处理get请求的
	v1 := r.Group("/get")
	{
		v1.GET("/action1", func(ctx *gin.Context) { ctx.String(http.StatusOK, "get-action1 响应"+current()) })
		v1.GET("/action2", func(ctx *gin.Context) { ctx.String(http.StatusOK, "get-action2 响应"+current()) })
	}

	v2 := r.Group("/post")
	{
		v2.POST("/action1", func(ctx *gin.Context) { ctx.String(http.StatusOK, "post-action1 响应"+current()) })
		v2.POST("/action2", func(ctx *gin.Context) { ctx.String(http.StatusOK, "post-action2 响应"+current()) })
	}
	r.Run(":8080")
}

func main() {
	test015()
}
