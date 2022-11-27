package dao

import "testing"

func TestUserDao_CheckTable(t *testing.T) {
	tests := []struct {
		name    string
		userDao *UserDao
		wantErr bool
	}{
		// TODO: Add test cases.
		{
			name:    "建表测试",
			userDao: &UserDao{},
			wantErr: false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			userDao := &UserDao{}
			if err := userDao.CheckTable(); (err != nil) != tt.wantErr {
				t.Errorf("UserDao.CheckTable() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}
