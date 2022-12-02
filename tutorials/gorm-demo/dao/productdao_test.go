package dao

import (
	"fmt"
	"gorm-demo/entity"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"gorm.io/gorm"
)

func TestProductDao_CheckTable(t *testing.T) {
	tests := []struct {
		name       string
		productDao *ProductDao
		wantErr    bool
	}{
		// TODO: Add test cases.
		{
			name:       "测试CheckTable方法",
			productDao: &ProductDao{},
			wantErr:    false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			productDao := &ProductDao{}
			if err := productDao.CheckTable(); (err != nil) != tt.wantErr {
				t.Errorf("ProductDao.CheckTable() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}

func TestProductDao_Insert(t *testing.T) {

	productDao := &ProductDao{}

	type args struct {
		entity *entity.Product
	}

	tests := []struct {
		name       string
		productDao *ProductDao
		args       args
	}{
		// TODO: Add test cases.
		{
			name:       "新增第一条记录",
			productDao: productDao,
			args: args{
				entity: &entity.Product{
					Code:   "a101",
					Price:  101,
					Remark: fmt.Sprintf("1. %v", time.Now().Format("2006-01-02 15:04:05")),
				},
			},
		}, {
			name:       "新增第二条记录",
			productDao: productDao,
			args: args{
				entity: &entity.Product{
					Code:   "a102",
					Price:  102,
					Remark: fmt.Sprintf("2. %v", time.Now().Format("2006-01-02 15:04:05")),
				},
			},
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			dbRes := productDao.Insert(tt.args.entity)

			a := assert.New(t)

			// gorm返回的对象必须非空
			a.NotNil(dbRes)

			// error必须为空
			a.Nil(dbRes.Error)

			// 影响行数必须是1
			a.Equal(int64(1), dbRes.RowsAffected)

			// 对象主键必须被重新设置
			a.Greater(tt.args.entity.ID, uint(0))
		})
	}
}

func TestProductDao_FindById(t *testing.T) {
	type args struct {
		id uint
	}
	tests := []struct {
		name       string
		productDao *ProductDao
		args       args
	}{
		// TODO: Add test cases.
		{
			name:       "根据主键查询t_product表",
			productDao: &ProductDao{},
			args:       args{id: 1},
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {

			dbRes, got := tt.productDao.FindById(tt.args.id)

			a := assert.New(t)

			// gorm返回的对象必须非空
			a.NotNil(dbRes)

			// error必须为空
			a.Nil(dbRes.Error)

			// gorm返回的对象必须非空
			a.NotNil(got)

			// 对象主键必须被重新设置
			a.Equal(got.ID, tt.args.id)

			fmt.Printf("got: %v\n", got)
		})
	}
}

func TestProductDao_FirstByCode(t *testing.T) {
	type args struct {
		code string
	}
	tests := []struct {
		name       string
		productDao *ProductDao
		args       args
		want       *gorm.DB
		want1      *entity.Product
	}{
		// TODO: Add test cases.
		{
			name:       "根据code查询t_product表",
			productDao: &ProductDao{},
			args:       args{code: "a102"},
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {

			dbRes, got := tt.productDao.FirstByCode(tt.args.code)

			a := assert.New(t)

			// gorm返回的对象必须非空
			a.NotNil(dbRes)

			// error必须为空
			a.Nil(dbRes.Error)

			// gorm返回的对象必须非空
			a.NotNil(got)

			// code字段必须相等
			a.Equal(got.Code, tt.args.code)

			a.Greater(got.ID, uint(0))

			fmt.Printf("got: %v\n", got)
		})
	}
}

func TestProductDao_FindIdsByCode(t *testing.T) {
	type args struct {
		code string
	}
	tests := []struct {
		name       string
		productDao *ProductDao
		args       args
	}{
		// TODO: Add test cases.
		{
			name:       "根据code查询t_product表的记录的id",
			productDao: &ProductDao{},
			args:       args{code: "a102"},
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {

			dbRes, got := tt.productDao.FindIdsByCode(tt.args.code)

			a := assert.New(t)

			// gorm返回的对象必须非空
			a.NotNil(dbRes)

			// error必须为空
			a.Nil(dbRes.Error)

			// gorm返回的对象必须非空
			a.NotNil(got)

			// 记录数大于0
			a.Greater(len(got), 0)

			// 每一个id都应该大于0
			for _, v := range got {
				a.Greater(v.Id, uint(0))
			}

			fmt.Printf("got: %v\n", got)
		})
	}
}

func TestProductDao_UpdatePriceByCode(t *testing.T) {
	type args struct {
		code  string
		price uint
	}
	tests := []struct {
		name       string
		productDao *ProductDao
		args       args
	}{
		// TODO: Add test cases.
		{
			name:       "根据code更新t_product表的记录的price字段",
			productDao: &ProductDao{},
			args:       args{code: "a102", price: 666},
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			dbRes := tt.productDao.UpdatePriceByCode(tt.args.code, tt.args.price)

			a := assert.New(t)

			// gorm返回的对象必须非空
			a.NotNil(dbRes)

			// error必须为空
			a.Nil(dbRes.Error)

			// 影响的记录数大于0
			a.Greater(dbRes.RowsAffected, int64(0))

		})
	}
}
