package dao

import (
	"errors"
	"fmt"
	"gorm-demo/entity"
)

type PolymorphicDao struct{}

// 检查表是否存在，如果不存在就立即新建
func (polymorphicDao *PolymorphicDao) CheckTable() error {

	if err := CheckTables([]interface{}{&entity.Xiaofengche{}, &entity.Jiazi{}, &entity.Yujie{}}); err != nil {
		fmt.Println("check table error", err)
		return err
	}

	return nil
}

func (polymorphicDao *PolymorphicDao) CreateJiazi(entity *entity.Jiazi) error {
	dbRes := db.Create(entity)

	if dbRes.Error != nil {
		return dbRes.Error
	} else if dbRes.RowsAffected < 1 {
		return errors.New("未增加有效记录")
	}

	return nil

}
