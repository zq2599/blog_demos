package dao

import (
	"errors"
	"fmt"
	"gorm-demo/entity"

	"gorm.io/gorm"
)

type TmgDao struct {
}

// 检查表是否存在，如果不存在就立即新建
func (tmgDao *TmgDao) Check() error {

	if err := CheckTables([]interface{}{&entity.Tmg{}}); err != nil {
		fmt.Println("check table error", err)
		return err
	}

	return nil
}

func (tmgDao *TmgDao) Drop() error {

	if err := DropTables([]interface{}{&entity.Tmg{}}); err != nil {
		fmt.Println("drop table error", err)
		return err
	}

	return nil
}

func (tmgDao *TmgDao) InsertWithTransaction(needInterrupt bool) error {

	db.Transaction(func(tx *gorm.DB) error {
		tx.Create(&entity.Tmg{Code: "001"})
		tx.Create(&entity.Tmg{Code: "002"})

		// 表示事务回滚
		if needInterrupt {
			return errors.New("interrupt here")
		}

		tx.Create(&entity.Tmg{Code: "003"})

		return nil
	})

	return nil
}
