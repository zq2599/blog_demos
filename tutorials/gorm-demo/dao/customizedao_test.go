package dao

import (
	"fmt"
	"gorm-demo/entity"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestCustomizeDao_Check(t *testing.T) {
	tests := []struct {
		name         string
		customizeDao *CustomizeDao
		wantErr      bool
	}{
		// TODO: Add test cases.
		{
			name:         "检查t_customize表",
			customizeDao: &CustomizeDao{},
			wantErr:      false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			customizeDao := &CustomizeDao{}
			if err := customizeDao.Check(); (err != nil) != tt.wantErr {
				t.Errorf("CustomizeDao.Check() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}

func TestCustomizeDao_Drop(t *testing.T) {
	tests := []struct {
		name         string
		customizeDao *CustomizeDao
		wantErr      bool
	}{
		// TODO: Add test cases.
		{
			name:         "删除t_customize表",
			customizeDao: &CustomizeDao{},
			wantErr:      false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			customizeDao := &CustomizeDao{}
			if err := customizeDao.Drop(); (err != nil) != tt.wantErr {
				t.Errorf("CustomizeDao.Drop() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}

func TestCustomizeDao_Insert(t *testing.T) {
	type args struct {
		entities []*entity.Person
	}
	tests := []struct {
		name         string
		customizeDao *CustomizeDao
		args         args
		wantErr      bool
	}{
		// TODO: Add test cases.
		{
			name:         "往t_customize表新增记录",
			customizeDao: &CustomizeDao{},
			args: args{
				entities: []*entity.Person{
					{PersonInfo: entity.Info{Name: "a1", Age: 11}, PersonActions: entity.Actions{"aaa", "bbb", "ccc"}},
					{PersonInfo: entity.Info{Name: "a2", Age: 12}, PersonActions: entity.Actions{"ddd", "eee", "fff"}},
				},
			},
			wantErr: false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			customizeDao := &CustomizeDao{}
			if err := customizeDao.Insert(tt.args.entities); (err != nil) != tt.wantErr {
				t.Errorf("CustomizeDao.Insert() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}

func TestCustomizeDao_FindById(t *testing.T) {
	type args struct {
		id uint
	}
	tests := []struct {
		name         string
		customizeDao *CustomizeDao
		args         args
		wantErr      bool
	}{
		// TODO: Add test cases.
		{
			name:         "验证根据id查找记录",
			customizeDao: &CustomizeDao{},
			args:         args{id: 1},
			wantErr:      false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			customizeDao := &CustomizeDao{}
			got, err := customizeDao.FindById(tt.args.id)
			if (err != nil) != tt.wantErr {
				t.Errorf("CustomizeDao.FindById() error = %v, wantErr %v", err, tt.wantErr)
				return
			}

			fmt.Printf("got: %v\n", got)
			a := assert.New(t)

			// gorm返回的对象必须非空
			a.NotNil(got)

			// code字段必须相等
			a.Equal(got.ID, tt.args.id)

		})
	}
}
