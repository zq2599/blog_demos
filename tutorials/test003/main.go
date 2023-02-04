package main

import (
	"fmt"

	"github.com/gin-gonic/gin"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
)

type Student struct {
	gorm.Model
	Name string
	age  uint
}

// 全局数据库 db
var db *gorm.DB

// 包初始化函数，可以用来初始化 gorm
func init() {
	// 配置 dsn
	// 账号
	username := "root"
	// 密码
	password := "123456"
	// mysql 服务地址
	host := "127.0.0.1"
	// 端口
	port := 3306
	// 数据库名
	Dbname := "demo"

	// 拼接 mysql dsn，即拼接数据源，下方 {} 中的替换参数即可
	// {username}:{password}@tcp({host}:{port})/{Dbname}?charset=utf8&parseTime=True&loc=Local&timeout=10s&readTimeout=30s&writeTimeout=60s
	// timeout 是连接超时时间，readTimeout 是读超时时间，writeTimeout 是写超时时间，可以不填
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?charset=utf8&parseTime=True&loc=Local", username, password, host, port, Dbname)

	// err
	var err error
	// 连接 mysql 获取 db 实例
	db, err = gorm.Open(mysql.Open(dsn), &gorm.Config{})
	if err != nil {
		panic("连接数据库失败, error=" + err.Error())
	}

	// 设置数据库连接池参数
	sqlDB, _ := db.DB()
	// 设置数据库连接池最大连接数
	sqlDB.SetMaxOpenConns(10)
	// 连接池最大允许的空闲连接数，如果没有sql任务需要执行的连接数大于2，超过的连接会被连接池关闭
	sqlDB.SetMaxIdleConns(2)

	// 建表
	db.AutoMigrate(&Student{})
}

// 获取 gorm db，其他包调用此方法即可拿到 db
// 无需担心不同协程并发时使用这个 db 对象会公用一个连接，因为 db 在调用其方法时候会从数据库连接池获取新的连接
func GetDB() *gorm.DB {
	return db
}

func main() {

	router := gin.Default()
	router.GET("/", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"message": "hello world",
		})
	})

	router.GET("/create", func(c *gin.Context) {
		name := c.Query("name")
		fmt.Printf("name [%v\n]", name)
		/*
			name := c.DefaltQuery("name", "小王子")
			age := strconv.Atoi(c.DefaltQuery("age", "10"))

			fmt.Println("name [%v], age [%v]", name, age)

			student := &Student{
				Name: name,
				Age:  age,
			}

			if err := db.Create(student).Error; err != nil {
				c.JSON(200, gin.H{
					"code":    0,
					"message": "insert db error",
				})

				return
			}

			c.JSON(200, gin.H{
				"code":    0,
				"message": fmt.Sprintf("insert into db success [%v]", student.Name),
			})
		*/

		c.JSON(200, gin.H{
			"message": "Hello world 123!",
		})
	})
	router.Run()
}
