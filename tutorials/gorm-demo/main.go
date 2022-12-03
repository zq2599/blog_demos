package main

import (
	"fmt"
)

func main() {
	test001()
}

type Person struct {
	name string
}

func test001() {
	p0 := &Person{"p0"}
	p1 := &Person{"p1"}

	var array = []*Person{p0, p1}

	for _, v := range array {
		fmt.Printf("*v: %v\n", *v)
	}

	fmt.Println("start test002")
	test002(array)
	fmt.Println("finish test002")

	for _, v := range array {
		fmt.Printf("*v: %v\n", *v)
	}
}

func test002(array []*Person) {
	for _, v := range array {
		fmt.Printf("*v: %v\n", *v)
		v.name = fmt.Sprintf("%s-change", v.name)
		fmt.Printf("*v: %v\n", *v)
	}
}
