package entity

import "gorm.io/gorm"

type Tmg struct {
	gorm.Model
	Code string
}
