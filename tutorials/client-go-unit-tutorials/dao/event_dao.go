package dao

import (
	"client-go-unit-tutorials/model"
	"log"
	"os"
	"time"

	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

var SQLITE_DB *gorm.DB

func InitDB(db *gorm.DB) {
	// db可以在外部传入，主要用于单元测试场景
	if db != nil {
		SQLITE_DB = db
	} else {
		// 开启日志便于查问题
		newLogger := logger.New(
			log.New(os.Stdout, "\r\n", log.LstdFlags), // io writer
			logger.Config{
				SlowThreshold:             time.Second, // Slow SQL threshold
				LogLevel:                  logger.Info, // Log level
				IgnoreRecordNotFoundError: true,        // Ignore ErrRecordNotFound error for logger
				ParameterizedQueries:      true,        // Don't include params in the SQL log
				Colorful:                  true,        // Disable color
			},
		)

		var err error

		SQLITE_DB, err = gorm.Open(sqlite.Open("controller_event.db"), &gorm.Config{
			Logger: newLogger,
		})
		if err != nil {
			panic("failed to connect database")
		}
	}

	// 迁移 schema
	SQLITE_DB.AutoMigrate(&model.ResourceEvent{})

	SaveEvent("Add", "pod", "pod-001")
	SaveEvent("Add", "pod", "pod-002")
	SaveEvent("Add", "pod", "pod-003")

	log.Printf("总数是[%d]", Count("Add"))

}

// SaveEvent 保存记录
func SaveEvent(eventType, resourceType, resourceName string) {
	SQLITE_DB.Create(&model.ResourceEvent{EventType: "Add",
		ResourceType: resourceType,
		ResourceName: resourceName})
}

// Count 查询记录的数量
func Count(eventType string) int64 {
	var count int64
	SQLITE_DB.Model(&model.ResourceEvent{}).Where("event_type=?", eventType).Count(&count)
	return count
}
