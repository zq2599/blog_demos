package train

import (
	"fmt"
	"time"
)

func Test006() {
	start := time.Now()
	timer1 := time.NewTimer(1 * time.Second)

	go func() {
		fmt.Printf("t : %v\n", <-timer1.C)
		fmt.Printf("span : %v\n", time.Now().Unix()-start.Unix())
	}()

	timer1.Reset(2 * time.Second)
	time.Sleep(3 * time.Second)
	fmt.Println("结束")
}
