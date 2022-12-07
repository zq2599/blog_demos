package train

import (
	"fmt"
	"sync"
)

var x uint
var wg sync.WaitGroup
var lock sync.Mutex

func Test010() {
	wg.Add(2)

	go func() {
		for i := 0; i < 100000; i++ {
			lock.Lock()
			x = x + 1
			lock.Unlock()
		}
		wg.Done()
	}()

	go func() {
		for i := 0; i < 100000; i++ {
			lock.Lock()
			x = x + 1
			lock.Unlock()
		}
		wg.Done()
	}()

	wg.Wait()
	fmt.Printf("x: %v\n", x)

}
