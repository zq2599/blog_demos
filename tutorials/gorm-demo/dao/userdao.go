package dao

import (
	"fmt"

	"gorm-demo/entity"
)

type UserDao struct{}

// 检查表是否存在，如果不存在就立即新建
func (userDao *UserDao) CheckTable() error {

	entity := &entity.User{}

	m := db.Migrator()

	if m.HasTable(entity) {
		fmt.Println("表已存在")
	} else {
		fmt.Println("表不存在，立即创建")
		if err := m.CreateTable(entity); err != nil {
			fmt.Printf("2. err: %v\n", err)
			return err
		}
	}

	return nil
}

/* func (productDao *ProductDao) Insert(entity *entity.Product) {
	db.Create(entity)
} */
