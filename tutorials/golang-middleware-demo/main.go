package main

import (
	"fmt"
	"golang-middleware-demo/yaml"

	_ "github.com/go-sql-driver/mysql"
	"github.com/jmoiron/sqlx"
)

type Person struct {
	UserId   int    `db:"user_id"`
	UserName string `db:"user_name"`
	Sex      string `db:"sex"`
	Email    string `db:"email"`
}

type Place struct {
	Country string `db:"country"`
	City    string `db:"city"`
	TelCode string `db:"telcode"`
}

var Db *sqlx.DB

func init() {
	fmt.Println("start init")
	database, err := sqlx.Open("mysql", "root:888888@tcp(120.24.56.76:3306)/test")

	if err != nil {
		fmt.Printf("err: %v\n", err)
		return
	}

	Db = database

	fmt.Printf("database: %v\n", *database)
	fmt.Println("end init")
}

func test001() {
	r, err := Db.Exec("insert into person(user_name, sex, email)values(?, ?, ?)", "Tom", "male", "Tom@qq.com")

	if err != nil {
		fmt.Printf("1. err: %v\n", err)
		return
	}

	fmt.Printf("r: %v\n", r)

	id, err := r.LastInsertId()

	if err != nil {
		fmt.Printf("2. err: %v\n", err)
		return
	}

	fmt.Printf("id: %v\n", id)
}

func test002() {
	var person []Person

	err := Db.Select(&person, "select user_id, user_name, sex, email from person where user_id>?", 1)

	if err != nil {
		fmt.Printf("3. err: %v\n", err)
		return
	}

	for _, v := range person {
		fmt.Printf("v: %v\n", v)
	}

}

func test003() {
	conf := &yaml.Yaml2Go{}

	if err := yaml.ReadConf(conf); err != nil {
		fmt.Printf("err: %v\n", err)
		return
	}

	fmt.Printf("conf: %v\n", *conf)
	fmt.Printf("config: %v\n", conf.Config)
}

func main() {
	defer Db.Close()
	// test001()
	// test002()
	test003()
}
