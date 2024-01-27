package main

import (
	"ffmpeg-go-tutorials/image"
	"time"
)

const (
	BasePath = "/Users/will/temp/202401/27"
)

func main() {
	// 视频的位置(请按照您的实际情况修改)
	inputPath := BasePath + "/input/test.mp4"
	// 输出文件的位置(请按照您的实际情况修改)
	outputPath := BasePath + "/output/" + time.Now().Format("20060102150405") + ".jpg"

	// 从指定视频提取第一桢图片到缓冲区
	reader, err := image.ExtractFrameFromVideo(inputPath, 1)
	if err != nil {
		panic(err)
	}

	// 将缓冲区中的图片数据持久化到文件
	err = image.SaveImageFromPipeToFile(reader, outputPath)
	if err != nil {
		panic(err)
	}
}
