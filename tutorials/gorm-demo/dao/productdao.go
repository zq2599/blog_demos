package dao

import (
	"fmt"

	"gorm-demo/entity"

	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/schema"
)

var db *gorm.DB

type ProductDao struct{}

func init() {
	fmt.Println("start init")

	var err error
	db, err = gorm.Open(mysql.New(mysql.Config{
		DSN:               "root:888888@tcp(120.24.56.76:3306)/gorm?charset=utf8mb4&parseTime=True&loc=Local",
		DefaultStringSize: 256,
	}), &gorm.Config{
		SkipDefaultTransaction: false,
		NamingStrategy: schema.NamingStrategy{
			TablePrefix:   "t_", // 自动建表的前缀
			SingularTable: true, // 表名是单数
		},
		DisableForeignKeyConstraintWhenMigrating: true, // 禁止外键约束
	})

	if err != nil {
		fmt.Printf("1. err: %v\n", err)
	}
}

// 检查表是否存在，如果不存在就立即新建
func (productDao *ProductDao) CheckTable() error {

	entity := &entity.Product{}

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

// 新增记录
func (productDao *ProductDao) Insert(entity *entity.Product) (tx *gorm.DB) {
	return db.Create(entity)
}

// 查找第一条记录
func (productDao *ProductDao) FindById(id uint) (*gorm.DB, *entity.Product) {
	result := &entity.Product{}
	dbRes := db.Model(&entity.Product{}).First(result, id)
	return dbRes, result
}

// 按照code查找第一条记录
func (productDao *ProductDao) FirstByCode(code string) (*gorm.DB, *entity.Product) {
	result := &entity.Product{}
	dbRes := db.Where("code=?", code).First(result)
	return dbRes, result
}

// 根据code查找，返回所有结果的id字段
func (productDao *ProductDao) FindIdsByCode(code string) (*gorm.DB, []entity.Id) {
	var result []entity.Id
	dbRes := db.Model(&entity.Product{}).Where("code=?", code).Find(&result)
	return dbRes, result
}

// 根据code更新price字段
func (productDao *ProductDao) UpdatePriceByCode(code string, price uint) *gorm.DB {
	dbRes := db.Model(&entity.Product{}).Where("code=?", code).Update("price", price)
	return dbRes
}
