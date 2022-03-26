package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"runtime"
	"strings"
	"time"
)

// 声明两个共享数据通道，用来统计发送请求和请求成功执行次数，有100缓存空间
var quit chan int = make(chan int, 100)
var send chan int = make(chan int, 100)

type response struct {
	Success bool   `json:"success"`
	Message string `json:"message"`
}

var res response

func Http_Post() error {
	// 执行HTTP_POST()函数时往通道发送消息
	send <- 0
	reqbody := `
        {
       "username":"Jim",
	   "orgname":"org1",
	   "channelName":"mychannel",
	   "peers":["localhost:7051"],
	   "chaincodeName":"register",
	   "functionName":"update",
	   "args":["a1001","1000","1000","1000"]
        }
        `
	//创建请求
	postReq, err := http.NewRequest("POST",
		"http://localhost:4000/channels/chaincodes/invoke", //post链接
		strings.NewReader(reqbody))                         //post内容

	if err != nil {
		fmt.Println("POST请求:创建请求失败", err)
		return err
	}

	//增加header
	postReq.Header.Set("authorization", "Bearer")
	postReq.Header.Set("content-type", "application/json")

	//执行请求
	client := &http.Client{}
	resp, err := client.Do(postReq)
	if err != nil {
		fmt.Println("POST请求:创建请求失败", err)
		return err
	} else {
		//读取响应
		body, err := ioutil.ReadAll(resp.Body)
		if err != nil {
			fmt.Println("POST请求:读取body失败", err)
			return err
		}

		fmt.Println("POST请求:创建成功", string(body))
		//	quit <- 0
		json.Unmarshal(body, &res)
		if res.Success == true {
			quit <- 0
		}
	}
	defer resp.Body.Close()

	return nil
}

func main() {
	var i, j int
	i = 0
	j = 0
	runtime.GOMAXPROCS(4)
	go func() {
		for {
			<-send
			j++
			fmt.Printf("发送交易次数：%d 次\n", j)
		}
	}()
	go func() {
		for {
			<-quit
			i++
			fmt.Printf("交易成功执行次数：%d 次\n", i)
		}
	}()
	go func() {
		for {
			select {
			// 每1百万纳秒执行一次，即1毫秒发送一次请求
			case <-time.After(2000000000):
				go Http_Post()
			}
		}
	}()
	time.Sleep(100 * time.Second)
}
