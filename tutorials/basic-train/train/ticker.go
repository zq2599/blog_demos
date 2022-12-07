package train

import (
	"fmt"
	"time"
)

func Test007() {
	ticker1 := time.NewTicker(1 * time.Second)

	go func() {

		for {
			t := <-ticker1.C
			fmt.Printf("ticker : %v\n", t.Local().UTC())
		}

	}()

	time.Sleep(10 * time.Second)
}
