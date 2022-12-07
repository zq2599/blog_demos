package train

import (
	"fmt"
	"runtime"
)

func Test003() {
	go func(s string) {
		for i := 0; i < 2; i++ {
			fmt.Println(s)
		}
	}("Hello")

	runtime.Gosched()
	fmt.Println("123")
}
