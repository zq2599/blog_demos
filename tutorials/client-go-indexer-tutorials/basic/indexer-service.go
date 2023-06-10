package basic

import (
	"log"

	"github.com/gin-gonic/gin"
)

func init() {
	log.Println("Hello world")
}

// getObjKeysByLanguageName 查询指定语言的所有对象的key(演示2. IndexKeys方法)
func GetObjKeysByLanguageName(c *gin.Context) {

	m := make(map[string]string)
	m["name"] = "Tom"
	c.JSON(200, m)
}

// getObjByLanguageName 查询指定语言的所有对象(演示4. ByIndex方法)
func GetObjByLanguageName(c *gin.Context) {

}

// getAllObjByOneName 根据某个对象的key，获取同语言类型的所有对象(演示1. Index方法)
func GetAllObjByOneName(c *gin.Context) {

}

// getAllClassType 返回所有语言类型(演示3. ListIndexFuncValues方法)
func GetAllLanguange(c *gin.Context) {

}

// getAllClassType 返回所有分类方式，这里应该是按服务类型和按语言类型两种(演示5. GetIndexers方法)
func GetAllClassType(c *gin.Context) {

}
