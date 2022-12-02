package entity

type Jiazi struct {
	ID          uint
	Name        string
	Xiaofengche Xiaofengche `gorm:"polymorphic:Owner;"`
}

type Yujie struct {
	ID          uint
	Name        string
	Xiaofengche Xiaofengche `gorm:"polymorphic:Owner;"`
}

type Xiaofengche struct {
	ID        uint
	Name      string
	OwnerType string
	OwnerID   uint
}
