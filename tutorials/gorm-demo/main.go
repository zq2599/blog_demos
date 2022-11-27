package main

import (
	"fmt"
	"time"

	"gorm-demo/dao"
	"gorm-demo/entity"
)

func main() {
	// 初始化
	productDao := &dao.ProductDao{}

	fmt.Println("start check table")

	// 确保表存在
	if err := productDao.CheckTable(); err != nil {
		fmt.Printf("1. err: %v\n", err)
		return
	}

	fmt.Println("start insert")

	// 新增一条记录
	productDao.Insert(&entity.Product{
		Code:   "D66",
		Price:  100,
		Remark: fmt.Sprintf("1. %v", time.Now().Format("2006-01-02 15:04:05")),
	})

	fmt.Println("finish insert")
}
