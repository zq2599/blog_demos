package main

import (
  "flag"
  "fmt"
)

//声明变量用于接收命令行传入的参数值
var (
  name string
  age int
  address *string
  id *int
)

func init() {
  //通过传入变量地址的方式，绑定命令行参数到string变量
  flag.StringVar(&name,   //第一个参数：存放值的参数地址
                 "name",  //第二个参数：命令行参数的名称
                "匿名",    //第三个参数：命令行不输入时的默认值
                "您的姓名") //第四个参数：该参数的描述信息，help命令时会显示
  
  //通过传入变量地址的方式，绑定命令行参数到int变量
  flag.IntVar(&age,        //第一个参数：存放值的参数地址
              "age",       //第二个参数：命令行参数的名称
              -1,          //第三个参数：命令行不输入时的默认值
              "您的年龄")   //第四个参数：该参数的描述信息，help命令时会显示
  
  //和前面两个变量的获取方式不同，这个api没有传入变量地址，而是把命令行参数值的地址返回了
  address = flag.String("address", //第一个参数：命令行参数的名称
                           "未知",     //第二个参数：命令行不输入时的默认值
                          "您的住址")   //第三个参数：该参数的描述信息，help命令时会显示

  id = flag.Int("id", //第一个参数：命令行参数的名称
                           -1,     //第二个参数：命令行不输入时的默认值
                          "身份ID")   //第三个参数：该参数的描述信息，help命令时会显示
}

func main() {
  //处理入参
  flag.Parse()

  //入参已经被赋值给各个变量，可以使用了
  fmt.Printf("%s您好, 您的年龄:%d, 您的住址:%s, 您的ID:%d\n\n", name, age, *address, *id)

  fmt.Println("---遍历有输入的参数（开始）---")

  //Visit方法会遍历有输入的参数，flag.Flag可以将参数的名称、值、默认值、描述等内容取到
  flag.Visit(func(f *flag.Flag){
    fmt.Printf("参数名[%s], 参数值[%s], 默认值[%s], 描述信息[%s]\n", f.Name, f.Value, f.DefValue, f.Usage)
  })
  fmt.Println("---遍历有输入的参数（结束）---\n")

  fmt.Println("---遍历所有的参数（开始）---")
  //VisitAll方法会遍历所有定义的参数(包括没有在命令行输入的)，flag.Flag可以将参数的名称、值、默认值、描述等内容取到
  flag.VisitAll(func(f *flag.Flag){
    fmt.Printf("参数名[%s], 参数值[%s], 默认值[%s], 描述信息[%s]\n", f.Name, f.Value, f.DefValue, f.Usage)
  })
  fmt.Println("---遍历所有的参数（结束）---\n")
}

