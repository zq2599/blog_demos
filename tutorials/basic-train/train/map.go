package train

import (
	"fmt"
	"strconv"
	"sync"
)

var m = sync.Map{}

func get(key string) any {
	_, value := m.Load(key)
	return value
}

func set(key string, value int) {
	m.Store(key, value)
}

func Test013() {
	wg := sync.WaitGroup{}

	for i := 0; i < 1000; i++ {
		wg.Add(1)

		go func(n int) {
			key := strconv.Itoa(n)
			set(key, n)
			fmt.Printf("key: %v, value: %v\n", key, get(key))
			wg.Done()
		}(i)
	}

	wg.Wait()

}
