package main

import (
	"fmt"
	"math/rand"
	"time"
)

// 斐波拉契数列
func fib(n int) int {
	if n == 0 || n == 1 {
		return n
	}

	return fib(n-2) + fib(n-1)
}

// 往切片中放入指定数量的随机数，这个切片没有提前设置容量
func newSlice(n int) []int {
	rand.Seed(time.Now().UnixNano())

	// 注意，这里在生成切片的时候并没有指定容量
	nums := make([]int, 0)

	for i := 0; i < n; i++ {
		nums = append(nums, rand.Int())
	}

	return nums
}

// 往切片中放入指定数量的随机数，这个切片提前设置了容量
func newSliceWithCap(n int) []int {
	rand.Seed(time.Now().UnixNano())

	// 注意，这里在生成切片的时候指定了容量
	nums := make([]int, 0, n)

	for i := 0; i < n; i++ {
		nums = append(nums, rand.Int())
	}

	return nums
}

func main() {
	start := time.Now()
	fmt.Printf("fib(50): %v, use [%v]ms\n", fib(50), time.Since(start).Milliseconds())
}
