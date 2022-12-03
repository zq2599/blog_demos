package entity

type Jiazi struct {
	ID          uint
	Name        string
	Xiaofengche Xiaofengche `gorm:"polymorphic:Owner;polymorphicValue:huhu"`
}

type Yujie struct {
	ID          uint
	Name        string
	Xiaofengche []Xiaofengche `gorm:"polymorphic:Owner;polymorphicValue:abaaba"`
}

type Xiaofengche struct {
	ID        uint
	Name      string
	OwnerType string
	OwnerID   uint
}
