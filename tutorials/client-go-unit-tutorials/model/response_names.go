package model

// 用于保存web响应的body
type ResponseNames struct {
	Message string   `json:"message"`
	Names   []string `json:"names"`
}
