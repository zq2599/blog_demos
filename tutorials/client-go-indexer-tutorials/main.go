package main

import (
	"client-go-indexer-tutorials/basic"

	"github.com/gin-gonic/gin"
)

func main() {
	r := gin.Default()
	basic.DoInit()

	// 用于提供基本功能的路由组
	basicGroup := r.Group("/basic")

	// 查询指定语言的所有对象的key(演示2. IndexKeys方法)
	basicGroup.GET("get_obj_keys_by_language_name", basic.GetObjKeysByLanguageName)

	// 查询指定语言的所有对象(演示4. ByIndex方法)
	basicGroup.GET("get_obj_by_language_name", basic.GetObjByLanguageName)

	// 根据某个对象的key，获取同语言类型的所有对象(演示1. Index方法)
	basicGroup.GET("get_all_obj_by_one_name", basic.GetAllObjByOneName)

	// 返回所有语言类型(演示3. ListIndexFuncValues方法)
	basicGroup.GET("get_all_languange", basic.GetAllLanguange)

	// 返回所有分类方式，这里应该是按服务类型和按语言类型两种(演示5. GetIndexers方法)
	basicGroup.GET("get_all_class_type", basic.GetAllClassType)

	r.Run(":18080")
}
