package util

import (
	"bytes"
	"fmt"
	"webhook/biz/model"
)

// TransformToLarkRequest 根据alertmanager的对象，创建出飞书消息的对象
func TransformToLarkRequest(notification model.Notification) (larkRequest *model.LarkRequest, err error) {
	var buffer bytes.Buffer

	// 先拿到分组情况
	buffer.WriteString(fmt.Sprintf("通知组%s，状态[%s]\n告警项\n\n", notification.GroupKey, notification.Status))

	// 每条告警逐个获取，拼接到一起
	for _, alert := range notification.Alerts {
		buffer.WriteString(fmt.Sprintf("摘要：%s\n详情：%s\n", alert.Annotations["summary"], alert.Annotations["description"]))
		buffer.WriteString(fmt.Sprintf("开始时间: %s\n\n", alert.StartsAt.Format("15:04:05")))
	}

	// 构造出飞书机器人所需的数据结构
	larkRequest = &model.LarkRequest{
		MsgType: "text",
		Content: model.Content{
			Text: buffer.String(),
		},
	}

	return larkRequest, nil
}
