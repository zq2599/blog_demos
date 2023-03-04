package main

import (
	"fmt"
	"strconv"
	"testing"
	"time"
)

func Test_fib(t *testing.T) {
	type args struct {
		n int
	}
	tests := []struct {
		name string
		args args
		want int
	}{
		// TODO: Add test cases.
		{name: "入参为10", args: args{10}, want: 55},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := fib(tt.args.n); got != tt.want {
				t.Errorf("fib() = %v, want %v", got, tt.want)
			}
		})
	}
}

func BenchmarkFib(b *testing.B) {
	for n := 0; n < b.N; n++ {
		fib(30)
	}
}

func BenchmarkParallelFib(b1 *testing.B) {
	b1.RunParallel(func(pb *testing.PB) {
		for pb.Next() {
			fib(30)
		}
	})
}

const (
	SLICE_LENGTH_MILLION         = 1000000   // 往切片中添加数据的长度，百万
	SLICE_LENGTH_TEN_MILLION     = 10000000  // 往切片中添加数据的长度，千万
	SLICE_LENGTH_HUNDRED_MILLION = 100000000 // 往切片中添加数据的长度，亿
)

func BenchmarkNewSlice(b *testing.B) {
	for n := 0; n < b.N; n++ {
		newSlice(SLICE_LENGTH_MILLION)
	}
}

func BenchmarkNewSliceWithCap(b *testing.B) {
	for n := 0; n < b.N; n++ {
		newSliceWithCap(SLICE_LENGTH_MILLION)
	}
}

func testNewSlice(len int, b *testing.B) {
	for n := 0; n < b.N; n++ {
		newSlice(len)
	}
}

func BenchmarkNewSlicMillion(b *testing.B) {
	testNewSlice(SLICE_LENGTH_MILLION, b)
}

func BenchmarkNewSlicTenMillion(b *testing.B) {
	testNewSlice(SLICE_LENGTH_TEN_MILLION, b)
}

func BenchmarkNewSlicHundredMillion(b *testing.B) {
	testNewSlice(SLICE_LENGTH_HUNDRED_MILLION, b)
}

// BenchmarkFibWithPrepare 进入正式测试前需要耗时做准备工作的case
func BenchmarkFibWithPrepare(b *testing.B) {
	// 假设这里有个耗时800毫秒的初始化操作
	<-time.After(800 * time.Millisecond)

	// 这样表示重新正式开始计时，前面的耗时都和本次基准测试无关
	b.ResetTimer()

	// 这下面才是咱们真正想做基准测试的代码
	for n := 0; n < b.N; n++ {
		fib(30)
	}
}

// BenchmarkFibWithClean 假设每次执行完fib方法后，都要做一次清理操作
func BenchmarkFibWithClean(b *testing.B) {
	// 这下面才是咱们真正想做基准测试的代码
	for n := 0; n < b.N; n++ {
		// 继续记录耗时
		b.StartTimer()

		fib(30)

		// 停止记录耗时
		b.StopTimer()

		// 假设这里有个耗时100毫秒的清理操作
		<-time.After(10 * time.Millisecond)
	}
}

// BenchmarkFibWrongA 演示了错误的基准测试代码，这样的测试可能无法结束
func BenchmarkFibWrongA(b *testing.B) {
	for n := 0; n < b.N; n++ {
		fib(n)
	}
}

// BenchmarkFibWrongB 演示了错误的基准测试代码，这样的测试可能无法结束
func BenchmarkFibWrongB(b *testing.B) {
	fmt.Println("n [" + strconv.Itoa(b.N) + "]")
	fib(b.N)
}

var result int

func BenchmarkFibAvoidCompilerOptimisations(b *testing.B) {
	var r int
	for n := 0; n < b.N; n++ {
		r = fib(30)
	}

	result = r
}
