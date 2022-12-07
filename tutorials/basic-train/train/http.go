package train

import (
	"bytes"
	"fmt"
	"io"
	"net/http"
)

const URL_KAFKA_TOPIC_LIST string = "http://42.193.162.141:31331/topics"

// 发起一次远程请求
func HttpRequest(response *string) error {
	resp, err := http.Get(URL_KAFKA_TOPIC_LIST)

	if err != nil {
		fmt.Printf("1. err: %v\n", err)
		return err
	}

	defer resp.Body.Close()

	fmt.Printf("resp.Status: %v\n", resp.Status)
	fmt.Printf("resp.Header: %v\n", resp.Header)

	buf := make([]byte, 16)

	var bt bytes.Buffer
	var tempStr string

	for {
		n, err := resp.Body.Read(buf)

		fmt.Println("读取了一组数据")

		if err != nil {

			// 真正的错误
			if err != io.EOF {
				fmt.Printf("2. err: %v\n", err)
				return err
			}

			tempStr = string(buf[:n])
			fmt.Printf("tempStr: %v\n", tempStr)
			// 读取结束
			bt.WriteString(tempStr)

			*response = bt.String()

			return nil
		} else {
			tempStr = string(buf[:n])
			fmt.Printf("tempStr: %v\n", tempStr)
			bt.WriteString(tempStr)
		}
	}
}
