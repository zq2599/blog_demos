package train

import (
	"fmt"
	"time"
)

func Test008() {
	c0 := make(chan uint)
	c1 := make(chan uint)

	go func() {
		time.Sleep(2 * time.Second)
		fmt.Println("0. send")
		c0 <- 0
	}()

	go func() {
		time.Sleep(time.Second)
		fmt.Println("1. send")
		c1 <- 1
	}()

	// fmt.Printf("0. get [%d]\n", <-c0)
	// fmt.Printf("1. get [%d]\n", <-c1)

	select {
	case r0 := <-c0:
		fmt.Printf("0. received [%d]", r0)
	case r1 := <-c1:
		fmt.Printf("1. received [%d]", r1)
	default:
		fmt.Println("default execute")
	}

}
