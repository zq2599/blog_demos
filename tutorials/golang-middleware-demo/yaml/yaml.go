package yaml

import (
	"fmt"
	"io/ioutil"

	"gopkg.in/yaml.v2"
)

// Yaml2Go
type Yaml2Go struct {
	Config Config `yaml:"config"`
}

// Config
type Config struct {
	Models []Models `yaml:"models"`
	Acls   []Acls   `yaml:"acls"`
}

// Models
type Models struct {
	Name   string      `yaml:"name"`
	Schema string      `yaml:"schema"`
	Model1 interface{} `yaml:"model1"`
	Model2 interface{} `yaml:"model2"`
}

// Acls
type Acls struct {
	Acl1      interface{} `yaml:"acl1"`
	Model     string      `yaml:"model"`
	Role      string      `yaml:"role"`
	Operation string      `yaml:"operation"`
	Action    string      `yaml:"action"`
	Acl2      interface{} `yaml:"acl2"`
}

// 从配置文件读取配置，生成对应的数据结构对象
func ReadConf(conf *Yaml2Go) error {
	// 读取文件
	yamlFile, err := ioutil.ReadFile("config/config.yaml")
	if err != nil {
		fmt.Printf("read yaml file err: %v\n", err)
		return err
	}

	// 反序列化
	if err := yaml.Unmarshal(yamlFile, conf); err != nil {
		fmt.Printf("unmarshal yaml err: %v\n", err)
		return err
	}

	return nil
}
