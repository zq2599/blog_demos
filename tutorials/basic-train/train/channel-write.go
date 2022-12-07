package train

import (
	"fmt"
	"time"
)

func Test009() {
	// 创建管道
	output1 := make(chan uint, 10)

	go write(output1)

	for s := range output1 {
		fmt.Printf("receive : %v\n", s)
		time.Sleep(time.Second)
	}

}

func write(ch chan uint) {
	for {
		select {
		case ch <- 1:
			fmt.Println("write success")
		default:
			fmt.Println("wirte fail")
		}

		time.Sleep(time.Millisecond * 500)
	}
}
