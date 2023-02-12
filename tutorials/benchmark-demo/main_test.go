package main

import (
	"testing"
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
