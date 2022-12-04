package dao

import (
	"errors"
	"fmt"
	"gorm-demo/entity"
)

type CustomizeDao struct{}

var TABLES_CUSTOMIZE = []interface{}{&entity.Person{}}

// 检查表是否存在，如果不存在就立即新建
func (customizeDao *CustomizeDao) Check() error {

	if err := CheckTables(TABLES_CUSTOMIZE); err != nil {
		fmt.Println("check table error", err)
		return err
	}

	return nil
}

// 将表全部删除
func (customizeDao *CustomizeDao) Drop() error {

	if err := DropTables(TABLES_CUSTOMIZE); err != nil {
		fmt.Println("drop table error", err)
		return err
	}

	return nil
}

// t_customize表增加多条记录
func (customizeDao *CustomizeDao) Insert(entities []*entity.Person) error {

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

// 根据id查找记录
func (customizeDao *CustomizeDao) FindById(id uint) (*entity.Person, error) {

	result := &entity.Person{}
	dbRes := db.Where("id=?", id).First(result)

	if dbRes.Error != nil {
		return nil, dbRes.Error
	} else if result.ID < 1 {
		return nil, errors.New(fmt.Sprintf("未找到有效记录[%v]", id))
	}

	return result, nil
}
