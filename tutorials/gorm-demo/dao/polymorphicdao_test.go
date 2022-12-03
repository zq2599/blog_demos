package dao

import (
	"gorm-demo/entity"
	"testing"
)

func TestPolymorphicDao_CheckTable(t *testing.T) {
	tests := []struct {
		name           string
		polymorphicDao *PolymorphicDao
		wantErr        bool
	}{
		// TODO: Add test cases.
		{
			name:           "3. 检查三个关联表是否存在",
			polymorphicDao: &PolymorphicDao{},
			wantErr:        false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if err := tt.polymorphicDao.Check(); (err != nil) != tt.wantErr {
				t.Errorf("PolymorphicDao.CheckTable() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}

func TestPolymorphicDao_CreateJiazi(t *testing.T) {
	type args struct {
		entity *entity.Jiazi
	}
	tests := []struct {
		name           string
		polymorphicDao *PolymorphicDao
		args           args
		wantErr        bool
	}{
		// TODO: Add test cases.
		{
			name:           "新增一条Jiazi记录",
			polymorphicDao: &PolymorphicDao{},
			args:           args{entity: &entity.Jiazi{Name: "夹子001", Xiaofengche: entity.Xiaofengche{Name: "小风车001"}}},
			wantErr:        false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if err := tt.polymorphicDao.CreateJiazi(tt.args.entity); (err != nil) != tt.wantErr {
				t.Errorf("PolymorphicDao.CreateJiazi() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}

func TestPolymorphicDao_Drop(t *testing.T) {
	tests := []struct {
		name           string
		polymorphicDao *PolymorphicDao
		wantErr        bool
	}{
		// TODO: Add test cases.
		{
			name:           "删除三个表",
			polymorphicDao: &PolymorphicDao{},
			wantErr:        false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			polymorphicDao := &PolymorphicDao{}
			if err := polymorphicDao.Drop(); (err != nil) != tt.wantErr {
				t.Errorf("PolymorphicDao.Drop() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}

func TestPolymorphicDao_CreateYujies(t *testing.T) {
	type args struct {
		entities []*entity.Yujie
	}
	tests := []struct {
		name           string
		polymorphicDao *PolymorphicDao
		args           args
		wantErr        bool
	}{
		// TODO: Add test cases.
		{
			name:           "增加多条yujie记录",
			polymorphicDao: &PolymorphicDao{},
			args: args{entities: []*entity.Yujie{
				{Name: "y0", Xiaofengche: []entity.Xiaofengche{{Name: "y0-xfc0"}, {Name: "y0-xfc1"}}},
				{Name: "y1", Xiaofengche: []entity.Xiaofengche{{Name: "y1-xfc0"}}},
			}},
			wantErr: false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			polymorphicDao := &PolymorphicDao{}
			if err := polymorphicDao.CreateYujies(tt.args.entities); (err != nil) != tt.wantErr {
				t.Errorf("PolymorphicDao.CreateYujies() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}
