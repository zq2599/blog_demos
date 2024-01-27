package image

import (
	"bytes"
	"fmt"
	"io"
	"os"

	"github.com/disintegration/imaging"
	"github.com/pkg/errors"
	"github.com/sirupsen/logrus"
	ffmpeg "github.com/u2takey/ffmpeg-go"
)

// ExtractFrameFromVideo 从指定视频文件提取指定的一帧
func ExtractFrameFromVideo(fullPath string, frameIndex int) (io.Reader, error) {
	buf := bytes.NewBuffer(nil)

	err := ffmpeg.
		Input(fullPath).                                                                     // 指定视频文件作为输入
		Filter("select", ffmpeg.Args{fmt.Sprintf("gte(n,%d)", frameIndex)}).                 // 通过select过滤器指定抽取第几帧
		Output("pipe:", ffmpeg.KwArgs{"vframes": 1, "format": "image2", "vcodec": "mjpeg"}). // 指定输出为图片，再指定图片格式以及解码器，还有桢数
		WithOutput(buf, os.Stdout).Run()                                                     // 输出是缓冲区

	if err != nil {
		logrus.Errorf("save frame from video[%s] to pipe error : %v", fullPath, err)
		return nil, errors.WithMessagef(err, "save frame from video[%s] to pipe fail", fullPath)
	}

	return buf, nil
}

// SaveImageFromPipeToFile 将缓冲区中的图片数据持久化到文件
func SaveImageFromPipeToFile(reader io.Reader, fullPath string) error {
	// 从缓冲区中读取图片数据
	image, err := imaging.Decode(reader)
	if err != nil {
		logrus.Errorf("decode to [%s] error : %v", fullPath, err)
		return errors.WithMessagef(err, "decode to [%s] fail", fullPath)
	}

	// 将图片数据持久化到文件
	err = imaging.Save(image, fullPath)
	if err != nil {
		logrus.Errorf("save to [%s] error : %v", fullPath, err)
		return errors.WithMessagef(err, "save to [%s] fail", fullPath)
	}

	return nil
}
