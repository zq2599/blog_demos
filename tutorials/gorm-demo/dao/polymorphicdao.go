package dao

import (
	"errors"
	"fmt"
	"gorm-demo/entity"
)

type PolymorphicDao struct{}

var ALL_TABLES = []interface{}{&entity.Xiaofengche{}, &entity.Jiazi{}, &entity.Yujie{}}

// 检查表是否存在，如果不存在就立即新建
func (polymorphicDao *PolymorphicDao) Check() error {

	if err := CheckTables(ALL_TABLES); err != nil {
		fmt.Println("check table error", err)
		return err
	}

	return nil
}

// 将表全部删除
func (polymorphicDao *PolymorphicDao) Drop() error {

	if err := DropTables(ALL_TABLES); err != nil {
		fmt.Println("drop table error", err)
		return err
	}

	return nil
}

// 增加一条记录
func (polymorphicDao *PolymorphicDao) CreateJiazi(entity *entity.Jiazi) error {
	dbRes := db.Create(entity)

	if dbRes.Error != nil {
		return dbRes.Error
	} else if dbRes.RowsAffected < 1 {
		return errors.New("未增加有效记录")
	}

	return nil
}

// t_yujie表增加多条记录
func (polymorphicDao *PolymorphicDao) CreateYujies(entities []*entity.Yujie) error {

	for _, v := range entities {
		dbRes := db.Create(v)

		if dbRes.Error != nil {
			return dbRes.Error
		} else if dbRes.RowsAffected < 1 {
			errStr := fmt.Sprintf("新增记录失败，%v", *v)
			return errors.New(errStr)
		}
	}

	return nil
}
