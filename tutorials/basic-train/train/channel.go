package train

import (
	"fmt"
	"time"
)

func Test005() {
	c := make(chan int)

	go func() {

		for value := range c {
			fmt.Printf("1. value: %v\n", value)
			time.Sleep(time.Millisecond * 10)
		}

	}()

	go func() {

		for value := range c {
			fmt.Printf("2. value: %v\n", value)
			time.Sleep(time.Millisecond * 10)
		}

	}()

	for i := 0; i < 10; i++ {
		c <- i
	}

	close(c)

	time.Sleep(time.Second)
	time.Sleep(time.Second)
	time.Sleep(time.Second)

}
