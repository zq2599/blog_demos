package dao

import (
	"testing"
)

func TestTmgDao_Check(t *testing.T) {
	tests := []struct {
		name    string
		tmgDao  *TmgDao
		wantErr bool
	}{
		// TODO: Add test cases.
		{
			name:    "创建t_tmg表",
			tmgDao:  &TmgDao{},
			wantErr: false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			tmgDao := &TmgDao{}
			if err := tmgDao.Check(); (err != nil) != tt.wantErr {
				t.Errorf("TmgDao.Check() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}

func TestTmgDao_Drop(t *testing.T) {
	tests := []struct {
		name    string
		tmgDao  *TmgDao
		wantErr bool
	}{
		// TODO: Add test cases.
		{
			name:    "删除t_tmg表",
			tmgDao:  &TmgDao{},
			wantErr: false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			tmgDao := &TmgDao{}
			if err := tmgDao.Drop(); (err != nil) != tt.wantErr {
				t.Errorf("TmgDao.Drop() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}

func TestTmgDao_InsertWithTransaction(t *testing.T) {
	type args struct {
		needInterrupt bool
	}
	tests := []struct {
		name    string
		tmgDao  *TmgDao
		args    args
		wantErr bool
	}{
		// TODO: Add test cases.
		{
			name:    "验证事务，正常提交",
			tmgDao:  &TmgDao{},
			args:    args{needInterrupt: false},
			wantErr: false,
		},
		{
			name:    "验证事务，事务中断",
			tmgDao:  &TmgDao{},
			args:    args{needInterrupt: true},
			wantErr: false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			tmgDao := &TmgDao{}
			if err := tmgDao.InsertWithTransaction(tt.args.needInterrupt); (err != nil) != tt.wantErr {
				t.Errorf("TmgDao.InsertWithTransaction() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}
