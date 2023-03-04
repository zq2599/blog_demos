package main

import (
	"fmt"

	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
)

type Product struct {
	gorm.Model
	Code  string
	Price uint
}

const (
	FIRST_PRODUCT_CODE = "D101"
)

func main() {
	fmt.Println("Hello world")

	db, err := gorm.Open(sqlite.Open("test.db"), &gorm.Config{})

	if err != nil {
		panic("failed to connect database")
	}

	db.AutoMigrate(&Product{})

	db.Create(&Product{Code: FIRST_PRODUCT_CODE, Price: 1000})

	var product Product

	db.Debug().First(&product, "code = ?", FIRST_PRODUCT_CODE)

	fmt.Printf("product: %+v\n", product)

	db.Delete(&product, product.ID)
}
