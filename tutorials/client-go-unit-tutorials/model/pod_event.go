package model

import (
	"gorm.io/gorm"
)

type ResourceEvent struct {
	gorm.Model
	EventType    string
	ResourceType string
	ResourceName string
}
