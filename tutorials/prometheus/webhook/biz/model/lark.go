package model

// 飞书机器人支持的POST数据结构
// 请求体相关
type LarkRequest struct {
	MsgType string  `json:"msg_type"`
	Content Content `json:"content"`
}
type Content struct {
	Text string `json:"text"`
}

// 响应体相关
type LarkResponse struct {
	Code int    `json:"code"`
	Msg  string `json:"msg"`
	Data Data   `json:"data"`
}
type Data struct {
}
