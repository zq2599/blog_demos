package entity

import (
	"database/sql/driver"
	"encoding/json"
	"errors"

	"gorm.io/gorm"
)

type Info struct {
	Name string
	Age  int
}

// 对象序列化
func (info Info) Value() (driver.Value, error) {
	str, err := json.Marshal(info)
	if err != nil {
		return nil, err
	}

	return string(str), nil
}

func (info *Info) Scan(value interface{}) error {
	str, ok := value.([]byte)

	if !ok {
		return errors.New("invalid raw data")
	}

	json.Unmarshal(str, info)
	return nil
}

type Actions []string

func (actions Actions) Value() (driver.Value, error) {
	str, err := json.Marshal(actions)
	if err != nil {
		return nil, err
	}

	return string(str), nil
}

func (actions *Actions) Scan(value interface{}) error {
	str, ok := value.([]byte)

	if !ok {
		return errors.New("invalid raw data")
	}

	json.Unmarshal(str, actions)
	return nil
}

type Person struct {
	gorm.Model
	PersonInfo    Info
	PersonActions Actions
}
