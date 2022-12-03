package dao

import (
	"fmt"
	"reflect"
)

func CheckTables(tables []interface{}) error {

	m := db.Migrator()

	for _, v := range tables {
		if m.HasTable(v) {
			fmt.Printf("表已存在，%v\n", reflect.TypeOf(v))
		} else {
			fmt.Printf("表不存在，立即创建，%v\n", reflect.TypeOf(v))
			if err := m.CreateTable(v); err != nil {
				fmt.Printf("3. err: %v\n", err)
				return err
			}
		}
	}

	return nil
}

func DropTables(tables []interface{}) error {
	m := db.Migrator()

	for _, v := range tables {
		if m.HasTable(v) {
			fmt.Printf("表已存在，立即删除，%v\n", reflect.TypeOf(v))
			if err := m.DropTable(v); err != nil {
				fmt.Printf("4. err: %v\n", err)
				return err
			}
		} else {
			fmt.Printf("表不存在，跳过，，%v\n", reflect.TypeOf(v))
		}
	}

	return nil
}
