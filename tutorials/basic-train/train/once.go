package train

import (
	"fmt"
	"sync"
	"time"
)

var once sync.Once

func onceFunc() {
	fmt.Println("onceFunc execute")
}

func biz() {
	once.Do(onceFunc)
	fmt.Println("biz execute")
}

func Test012() {

	go biz()
	go biz()
	time.Sleep(time.Second)
}
