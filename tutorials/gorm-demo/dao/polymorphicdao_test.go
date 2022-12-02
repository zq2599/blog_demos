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
			if err := tt.polymorphicDao.CheckTable(); (err != nil) != tt.wantErr {
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
