package train

import (
	"fmt"
	"sync"
)

var wg1 sync.WaitGroup

func Test011() {

	wg1.Add(1)
	go func() {
		fmt.Println("gorutine execute")
		wg1.Done()
	}()

	fmt.Println("main execute")
	wg1.Wait()
}
