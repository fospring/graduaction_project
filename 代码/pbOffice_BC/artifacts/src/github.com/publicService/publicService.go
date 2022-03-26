/*
*本合约的主要功能是发布工位与服务
 */
package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

var logger = shim.NewLogger("Service")

//ServiceChaincode implementation
type ServiceChaincode struct {
}

//==================
//定义工位/服务结构
//==================
type Service struct {
	ID          string    `json:"id"`
	OwnerID     string    `json:"ownerId"`
	Type        string    `json:"type"`
	Name        string    `json:"name"`
	Function    string    `json:"function"`
	Money       float64   `json:"money"`
	FunPoint    int       `json:"funPoint"`
	Description string    `json:"description"`
        Place       string    `json:"palce"`
        Capacity    int       `json:"capacity"`
	State       string    `json:"state"`
	Top         bool      `json:"top"`
	TopPoint    int       `json:"topPoint"`
	UserID      string    `json:"userId"`
	Comments    []Comment `json:"comment"`
}

//==========================
//用户评论
//==========================
type Comment struct {
	CusHash string `json:"cusHash"` //Id hash
	Grade   int    `json:"gevel"`   //bad -> good from 1 to 5
	Comment string `json:"comment"`
}

//==================
//定义用户账户结构
//==================
type User struct {
	ID       string  `json:"id"`
	Password string  `json:"password"`
	Role     string  `json:"role"`
	Balance  float64 `json:"balance"`
	CrdPoint int     `json:"crePoint"`
	FunPoint int     `json:"funcPoint"`
}

//安装Chaincode
func (t *ServiceChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Info("######## Register Init ########")

	return shim.Success(nil)
}

//Invoke interface
func (t *ServiceChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Info("######## Register Invoke ########")

	function, args := stub.GetFunctionAndParameters()

	if function == "addService" {
		//Add an entity to its state
		return t.addService(stub, args)
	}
	if function == "addServiceByFunPoint" {
		//Add an entity to its state
		return t.addServiceByFunPoint(stub, args)
	}
	if function == "appService" {
		//apply to the entity
		return t.appService(stub, args)
	}
	if function == "appServiceByFunPoint" {
		//appService by funPoint of the entity
		return t.appServiceByFunPoint(stub, args)
	}
	if function == "topService" {
		//topService an entities from its state
		return t.topService(stub, args)
	}
	if function == "resetService" {
		//Reset an entities from its state
		return t.resetService(stub, args)
	}
	if function == "addComment" {
		//addComment an entities from its state
		return t.addComment(stub, args)
	}
	if function == "query" {
		//Query an entities from its state
		return t.query(stub, args)
	}
	if function == "queryEnroll" {
		//Query Enroller an entities from its state
		return t.queryEnroll(stub, args)
	}
	if function == "deleteService" {
		//Delete an entities from its state
		return t.deleteService(stub, args)
	}
	if function == "getHistoryForKey" {
		//getHistoryForKey an entities from its state
		return t.getHistoryForKey(stub, args)
	}
	if function == "testRangeQuery" {
		//testRangeQuery entities from its state
		return t.testRangeQuery(stub, args)
	}
	logger.Errorf("Unknown action, check the first argument, must be one of 'addService', 'appService', 'confirmService'"+
		"'resetService', 'queryService' or 'deleteService', But got: %v", args[0])
	return shim.Error(fmt.Sprintf("Unknown action, check the first argument, must be one of 'addService', 'appService', 'confirmService'"+
		" 'resetService', 'queryService' or 'deleteService', But got: %v", args[0]))
}

//==============================================================================================
//创建工位/服务 args:ID | OwnerID | Type | Name | Function | Money | Description | Place | Capacity
//==============================================================================================
func (t *ServiceChaincode) addService(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 8 {
		return shim.Error("Incorrect number of arguments. Expecting 8,function followed by 1 serviceID and 7 value")
	}

	if args[2] != "1" && args[2] != "2" {
		return shim.Error("Incorrect Type. Expecting '1' means workplace,or '2' means a service")
	}

	var serviceID string           //Entities
	var OwnerID, Type, Name string //所有者编号、类型:"1"工位、"2"服务、发布产品的名称
	var Money float64
	var Description, Place string //描述、地点
        var Capacity int //工位容量
	var err error

	serviceID = args[0]
	OwnerID = args[1]
	Type = args[2]
	Name = args[3]
	Money, _ = strconv.ParseFloat(args[4], 64)
	Description = args[5]
        Place = args[6]
        Capacity, _ = strconv.Atoi(args[7])


	var service Service

	service.ID = serviceID
	service.OwnerID = OwnerID
	service.Type = Type
	service.Name = Name
	service.Money = Money
	service.Description = Description
        service.Place = Place
        service.Capacity = Capacity
	service.State = "0" //"0"可使用,"1"待审核,"2"使用中,"3"暂停使用
	service.Top = false //false为未添加特别权限，true为添加特别权限
	service.UserID = ""
	uBytes, _ := json.Marshal(service)
	//Get the state from the ledger
	//TODD:will be nice to have a GetAllState call to ledger
	Avalbytes, err := stub.GetState(serviceID)
	if err != nil {
		return shim.Error("Failed to get state")
	}
	if Avalbytes != nil {
		return shim.Error("this user already exist")
	}

	//Write the state back to the ledger
	err = stub.PutState(serviceID, uBytes)
	if err != nil {
		return shim.Error(err.Error())
	}

	if Type == "1" {
		return shim.Success([]byte(serviceID + "工位创建成功！"))
	} else {
		return shim.Success([]byte(serviceID + "服务创建成功！"))
	}
}

//==============================================================================================
//创建工位/服务 args:ID | OwnerID | Type | Name | Function | FuncPoint | Description | Place | Capacity
//==============================================================================================
func (t *ServiceChaincode) addServiceByFunPoint(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 8 {
		return shim.Error("Incorrect number of arguments. Expecting 8,function followed by 1 serviceID and 7 value")
	}

	if args[2] != "1" && args[2] != "2" {
		return shim.Error("Incorrect Type. Expecting '1' means workplace,or '2' means a service")
	}

	var serviceID string           //Entities
	var OwnerID, Type, Name string //所有者编号、类型:"1"工位、"2"服务、发布产品的名称
	var Description, Place string //描述、地点
        var FunPoint,Capacity int //工位容量
        var err error

	serviceID = args[0]
	OwnerID = args[1]
	Type = args[2]
	Name = args[3]
	FunPoint, _ = strconv.Atoi(args[4])
	Description = args[5]
        Place = args[6]
        Capacity, _ = strconv.Atoi(args[7])

	var service Service

	service.ID = serviceID
	service.OwnerID = OwnerID
	service.Type = Type
	service.Name = Name
	service.FunPoint = FunPoint
	service.Description = Description
        service.Place = Place
        service.Capacity = Capacity
	service.State = "0" //"0"可使用,"1"待审核,"2"使用中,"3"暂停使用
	service.Top = false //false为未添加特别权限，true为添加特别权限
	service.UserID = ""
	uBytes, _ := json.Marshal(service)
	//Get the state from the ledger
	//TODD:will be nice to have a GetAllState call to ledger
	Avalbytes, err := stub.GetState(serviceID)
	if err != nil {
		return shim.Error("Failed to get state")
	}
	if Avalbytes != nil {
		return shim.Error("this user already exist")
	}

	//Write the state back to the ledger
	err = stub.PutState(serviceID, uBytes)
	if err != nil {
		return shim.Error(err.Error())
	}

	if Type == "1" {
		return shim.Success([]byte(serviceID + "工位创建成功！"))
	} else {
		return shim.Success([]byte(serviceID + "服务创建成功！"))
	}
}

//===============================
//申请工位/服务 args:ID | userID
//===============================
func (t *ServiceChaincode) appService(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	serviceID := args[0]
	userID := args[1]

	//query the ledger
	bytes, err := stub.GetState(serviceID)
	if err != nil {
		return shim.Error("Failed to get service: " + err.Error())
	}
	if bytes == nil {
		return shim.Error("This service does not exists: " + serviceID)
	}

	var service Service

	err = json.Unmarshal(bytes, &service)
	if err != nil {
		return shim.Error("Failed to get service: " + err.Error())
	}

	if service.State != "0" {
		return shim.Error("can't be applied.Please choice other service")
	}

	//query the ledger for user
	// query := [][]byte{[]byte("query"), []byte(userID)}
	// response := stub.InvokeChaincode("register", query, "mychannel")
	// if err != nil {
	// 	return shim.Error("Failed to get user: " + err.Error())
	// }
	// if bytes == nil {
	// 	return shim.Error("This user does not exists: " + userID)
	// }

	//fmt.Println(response.Message)
	//return shim.Success([]byte(response.Message))

	var user User
	//query the ledger for user
	query := [][]byte{[]byte("query"), []byte(userID)}
	fmt.Println(query)
	response := stub.InvokeChaincode("register", query, "mychannel")

	//fmt.Println("1111111111111111111", string(response))
	fmt.Println(string(response.Payload))
	bytes = response.GetPayload()
	//使用者
	err = json.Unmarshal(bytes, &user)
	//fmt.Println("3333333333333333333", user.Balance)
	if err != nil {
		return shim.Error("Failed to get user: " + err.Error())
	}
	//enough Credit points and balance

	if user.CrdPoint > 100 && user.Balance > service.Money {
		log.Println("-------------user.CrdPoint >100 and user.balnce >service.money")
		//	balance, err := strconv.Atoi(user.Balance)
		//if err != nil {
		//	return shim.Error("Failed to conver balance to int: " + err.Error())
		//}
		service.State = "1"
		//! LOCK needed
		//update service state
		bytes, _ = json.Marshal(service)
		err = stub.PutState(service.ID, bytes)
		//money, err := strconv.Atoi(service.Money)
		//newBalance = balance - money
		//user.Balance = strconv.Itoa(newBalance)
		user.Balance = user.Balance - service.Money
		user.FunPoint += 5
		bytes, _ = json.Marshal(user)
		//convert type
		//update user stater
		balance := strconv.FormatFloat(user.Balance, 'E', -1, 64)
		crdPoint := strconv.Itoa(user.CrdPoint)
		funPoint := strconv.Itoa(user.FunPoint)
		fmt.Println("xinyongjifen:" + crdPoint + " gongnengjifen:" + funPoint)
		trans := [][]byte{[]byte("update"), []byte(userID), []byte(string(balance)), []byte(crdPoint), []byte(funPoint)}
		response := stub.InvokeChaincode("register", trans, "mychannel")
		//err = stub.PutState(user.ID, bytes)
		if response.Status != int32(200) {
			fmt.Println("11111111111111111111Failed to put state of user: " + userID)
			return shim.Error("11111111111111111111Failed to put state of user: " + userID)
		}
		//query the ledger for Owner
		query = [][]byte{[]byte("query"), []byte(service.OwnerID)}
		fmt.Println(query)
		response = stub.InvokeChaincode("register", query, "mychannel")
		bytes := response.GetPayload()
		if response.Status != int32(200) {
			fmt.Println("2222222222222222222222Failed to put state of Owner: " + service.OwnerID)
			return shim.Error("222222222222222222222Failed to query Owner " + service.OwnerID)
		}
		//bytes, err = stub.GetState(service.OwnerID)
		err = json.Unmarshal(bytes, &user)
		if err != nil {
			return shim.Error(err.Error())
		}
		//	balance, err = strconv.Atoi(user.Balance)
		//	if err != nil {
		//		return shim.Error("Failed to conver balance to int: " + err.Error())
		//	}
		// money, err := strconv.Atoi(service.Money)
		// newBalance = balance + money
		// user.Balance = strconv.Itoa(newBalance)
		user.Balance = user.Balance + service.Money
		user.FunPoint += 5
		bytes, _ = json.Marshal(user)
		//update owner state
		balance = strconv.FormatFloat(user.Balance, 'E', -1, 64)
		crdPoint = strconv.Itoa(user.CrdPoint)
		funPoint = strconv.Itoa(user.FunPoint)
		trans = [][]byte{[]byte("update"), []byte(user.ID), []byte(string(balance)), []byte(crdPoint), []byte(funPoint)}
		response = stub.InvokeChaincode("register", trans, "mychannel")
		//err = stub.PutState(user.ID, bytes)
		if response.Status != int32(200) {
			fmt.Println("333333333333333333Failed to put state of user: " + user.ID)
			return shim.Error("Failed to put state of user: " + string(response.Status))
		}
		// err = stub.PutState(user.ID, bytes)
		// if err != nil {
		// 	return shim.Error("Failed to put state of owner: " + err.Error())
		// }
		// if err != nil {
		// 	return shim.Error("Failed to put state of owner: " + err.Error())
		// }
		//-!LOCK
		return shim.Success([]byte(service.ID + "申请成功！"))
	}
	//enough Credit points and don't have enough balance
	if user.CrdPoint > 100 && user.Balance < service.Money {
		return shim.Error("Don't have enough money")
	}

	return shim.Error("-----------------Don't have enough credit points.Please do something to add your credit points.")
}

//===============================
//申请工位/服务 args:ID | userID
//===============================
func (t *ServiceChaincode) appServiceByFunPoint(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}
	serviceID := args[0]
	userID := args[1]
	//query the ledger
	bytes, err := stub.GetState(serviceID)
	if err != nil {
		return shim.Error("Failed to get service: " + err.Error())
	}
	if bytes == nil {
		return shim.Error("This service does not exists: " + serviceID)
	}
	var service Service
	err = json.Unmarshal(bytes, &service)
	if err != nil {
		return shim.Error("Failed to get service: " + err.Error())
	}

	if service.State != "0" {
		return shim.Error("can't be applied.Please choice other service")
	}
	var user User
	query := [][]byte{[]byte("query"), []byte(userID)}
	fmt.Println(query)
	response := stub.InvokeChaincode("register", query, "mychannel")
	//fmt.Println("1111111111111111111", string(response))
	fmt.Println(string(response.Payload))
	bytes = response.GetPayload()
	//使用者
	err = json.Unmarshal(bytes, &user)
	//fmt.Println("3333333333333333333", user.Balance)
	if err != nil {
		return shim.Error("Failed to get user: " + err.Error())
	}
	//enough Credit points and balance
	if user.CrdPoint > 100 && user.FunPoint > service.FunPoint {
		log.Println("-------------user.CrdPoint >100 and user.balnce >service.money")
		service.State = "1"
		bytes, _ = json.Marshal(service)
		err = stub.PutState(service.ID, bytes)
		user.FunPoint = user.FunPoint - service.FunPoint
		user.FunPoint += 5
		bytes, _ = json.Marshal(user)
		balance := strconv.FormatFloat(user.Balance, 'E', -1, 64)
		crdPoint := strconv.Itoa(user.CrdPoint)
		funPoint := strconv.Itoa(user.FunPoint)
		fmt.Println("xinyongjifen:" + crdPoint + " gongnengjifen:" + funPoint)
		trans := [][]byte{[]byte("update"), []byte(userID), []byte(string(balance)), []byte(crdPoint), []byte(funPoint)}
		response := stub.InvokeChaincode("register", trans, "mychannel")
		if response.Status != int32(200) {
			fmt.Println("11111111111111111111Failed to put state of user: " + userID)
			return shim.Error("11111111111111111111Failed to put state of user: " + userID)
		}
		query = [][]byte{[]byte("query"), []byte(service.OwnerID)}
		fmt.Println(query)
		response = stub.InvokeChaincode("register", query, "mychannel")
		bytes := response.GetPayload()
		if response.Status != int32(200) {
			fmt.Println("2222222222222222222222Failed to put state of Owner: " + service.OwnerID)
			return shim.Error("222222222222222222222Failed to query Owner " + service.OwnerID)
		}
		err = json.Unmarshal(bytes, &user)
		if err != nil {
			return shim.Error(err.Error())
		}
		user.FunPoint = user.FunPoint + service.FunPoint
		user.FunPoint += 5
		bytes, _ = json.Marshal(user)
		balance = strconv.FormatFloat(user.Balance, 'E', -1, 64)
		crdPoint = strconv.Itoa(user.CrdPoint)
		funPoint = strconv.Itoa(user.FunPoint)
		trans = [][]byte{[]byte("update"), []byte(user.ID), []byte(string(balance)), []byte(crdPoint), []byte(funPoint)}
		response = stub.InvokeChaincode("register", trans, "mychannel")
		if response.Status != int32(200) {
			fmt.Println("333333333333333333Failed to put state of user: " + user.ID)
			return shim.Error("Failed to put state of user: " + string(response.Status))
		}
		return shim.Success([]byte(service.ID + "申请成功！"))
	}
	if user.CrdPoint > 100 && user.Balance < service.Money {
		return shim.Error("Don't have enough money")
	}

	return shim.Error("-----------------Don't have enough credit points.Please do something to add your credit points.")
}

//===============================
//工位/服务设置top功能 args:ID | TopPoint
//===============================
func (t *ServiceChaincode) topService(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}
	serviceID := args[0]
	topPoint, _ := strconv.Atoi(args[1])
	//query the ledger
	bytes, err := stub.GetState(serviceID)
	if err != nil {
		return shim.Error("Failed to get service: " + err.Error())
	}
	if bytes == nil {
		return shim.Error("This service does not exists: " + serviceID)
	}
	var service Service
	err = json.Unmarshal(bytes, &service)
	ownerId := service.OwnerID
	if err != nil {
		return shim.Error("Failed to get service: " + err.Error())
	}

	if service.State != "0" {
		return shim.Error("can't be applied.Please choice other service")
	}
	var owner User
	query := [][]byte{[]byte("query"), []byte(ownerId)}
	fmt.Println(query)
	response := stub.InvokeChaincode("register", query, "mychannel")
	//fmt.Println("1111111111111111111", string(response))
	fmt.Println(string(response.Payload))
	bytes = response.GetPayload()
	//使用者
	err = json.Unmarshal(bytes, &owner)
	//fmt.Println("3333333333333333333", user.Balance)
	if err != nil {
		return shim.Error("Failed to get user: " + err.Error())
	}
	//enough Credit points and balance
	if owner.FunPoint >= topPoint {
		log.Println("-------------owner.FunPoint >topPoint--------")
		service.Top = true
		service.TopPoint = topPoint
		bytes, _ = json.Marshal(service)
		err = stub.PutState(service.ID, bytes)
		if err != nil {
			return shim.Error("Fail to putstate service")
		}
		owner.FunPoint = owner.FunPoint - topPoint
		bytes, _ = json.Marshal(owner)
		balance := strconv.FormatFloat(owner.Balance, 'E', -1, 64)
		crdPoint := strconv.Itoa(owner.CrdPoint)
		funPoint := strconv.Itoa(owner.FunPoint)
		fmt.Println("xinyongjifen:" + crdPoint + " gongnengjifen:" + funPoint)
		trans := [][]byte{[]byte("update"), []byte(ownerId), []byte(string(balance)), []byte(crdPoint), []byte(funPoint)}
		response := stub.InvokeChaincode("register", trans, "mychannel")
		if response.Status != int32(200) {
			fmt.Println("11111111111111111111Failed to put state of user: " + ownerId)
			return shim.Error("11111111111111111111Failed to put state of user: " + ownerId)
		}
		return shim.Success([]byte(service.ID + "转换成功！"))
	}
	if owner.FunPoint < topPoint {
		return shim.Error("Don't have enough FunPoint")
	}

	return shim.Error("-----------------Don't have enough credit points.Please do something to add your credit points.")
}

//==============================================
//query from register
//==============================================
func (t *ServiceChaincode) queryEnroll(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	fmt.Println("queryEnroll")

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	//var account Account
	userID := args[0]
	var user User
	//query the ledger
	//query the ledger for user
	query := [][]byte{[]byte("query"), []byte(userID)}
	fmt.Println(query)
	response := stub.InvokeChaincode("register", query, "")
	//fmt.Println("1111111111111111111", string(response))
	fmt.Println("2222222222222222222", string(response.Payload))
	bytes := response.GetPayload()
	json.Unmarshal(bytes, &user)
	fmt.Println("3333333333333333333", user.Balance)
	fmt.Println(user.Balance)
	// if err != nil {
	// 	return shim.Error("Failed to get user: " + err.Error())
	// }
	// if bytes == nil {
	// 	return shim.Error("This user does not exists: " + userID)
	// }
	//fmt.Println(response.Message)
	return response
}

//=============================================
//设置工位/服务状态 args:ID| state
//=============================================
func (t *ServiceChaincode) resetService(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	fmt.Println("resetService")

	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	//var account Account
	serviceID := args[0]
	serviceState := args[1]
	var err error

	//query the ledger
	Bytes, _ := stub.GetState(serviceID)
	if err != nil {
		return shim.Error("Failed to get service: " + err.Error())
	}
	if Bytes == nil {
		return shim.Error("This serviceID does not exists: " + serviceID)
	}
	var service Service

	err = json.Unmarshal(Bytes, &service)
	if err != nil {
		return shim.Error("Failed to get service: " + err.Error())
	}
	//change state
	service.State = serviceState

	bytes, _ := json.Marshal(service)
	err = stub.PutState(serviceID, bytes)
	if err != nil {
		return shim.Error(err.Error())
	}
	if service.Type == "1" {
		return shim.Success([]byte(serviceID + "工位更改成功"))
	}
	return shim.Success([]byte(serviceID + "服务位更改成功"))
}

//================
//删除服务
//================
func (t *ServiceChaincode) deleteService(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("deleteService")

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	serviceID := args[0]
	bytes, err := stub.GetState(serviceID)
	if err != nil {
		return shim.Error("Fail to get service:" + err.Error())
	}
	if bytes == nil {
		return shim.Error("this service is not found")
	}

	var service Service
	err = json.Unmarshal(bytes, &service)
	if err != nil {
		return shim.Error("Fail to get service:" + err.Error())
	}

	service = Service{} //delete the service
	bytes, err = json.Marshal(service)
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState(serviceID, bytes)

	err = stub.DelState(serviceID)
	if err != nil {
		return shim.Error("Failed to delete state:" + err.Error())
	}
	err = stub.DelState(serviceID)
	if err != nil {
		return shim.Success([]byte("delete sucessfully"))
	}

	return shim.Success([]byte("该工位/服务已被删除"))
}

//===================================================
//增加评论 args:userID | serviceID | grade | comment
//===================================================
func (t *ServiceChaincode) addComment(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 4 {
		return shim.Error("Incorrect number of arguments. Expecting 4")
	}

	userID := args[0]
	serviceID := args[1]
	grade, err := strconv.Atoi(args[2])
	if err != nil {
		shim.Error("Cann't convert grade from string to string")
	}
	comment := args[3]

	//query the ledger,需要查询的数据不在此chaincode上
	bytes, err := stub.GetState(serviceID)
	if err != nil {
		return shim.Error("Failed to get service: " + err.Error())
	}

	var commentSt Comment
	commentSt.CusHash = userID
	commentSt.Grade = grade
	commentSt.Comment = comment
	//	commentSt := Comment{
	//                CusHash: userID,
	//		Grade:    grade,
	//		Comment: comment
	//	}
	var service Service
	err = json.Unmarshal(bytes, &service)
	if err != nil {
		shim.Error("Cann't convert to Service")
	}
	service.State = "0"
	service.Comments = append(service.Comments, commentSt)
	bytes, _ = json.Marshal(service)

	err = stub.PutState(serviceID, bytes)
	if err != nil {
		return shim.Error("222222222222222222222222" + err.Error())
	}
	// OwnerID := service.OwnerID
	// var owner, user User
	// bytes, err = stub.GetState(OwnerID)
	// err = json.Unmarshal(bytes, owner)
	// if err != nil {
	// 	shim.Error("Cann't convert to Service")
	// }
	// if grade == 0 {
	// 	owner.CrdPoint -= 5
	// } else {
	// 	owner.CrdPoint += 5
	// }
	// bytes, _ = json.Marshal(owner)
	// err = stub.PutState(OwnerID, bytes)

	// bytes, err = stub.GetState(userID)
	// err = json.Unmarshal(bytes, user)
	// if err != nil {
	// 	shim.Error("Cann't convert to Service")
	// }
	// user.FunPoint += 5
	// bytes, _ = json.Marshal(user)
	// err = stub.PutState(userID, bytes)
	return shim.Success([]byte("sucessfully add comment"))
}

//================================
//查询工位/服务 args:ID
//================================
func (t *ServiceChaincode) query(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting ID of the Service to query")
	}

	var ID string // Entities
	var err error

	ID = args[0]

	// Get the state from the ledger
	Avalbytes, err := stub.GetState(ID)
	if err != nil {
		jsonResp := "{\"Error\":\"Failed to get state for " + ID + "\"}"
		return shim.Error(jsonResp)
	}

	if Avalbytes == nil {
		jsonResp := "{\"Error\":\"Nil count for " + ID + "\"}"
		return shim.Error(jsonResp)
	}

	jsonResp := string(Avalbytes)
	fmt.Printf("Query Response:%s\n", jsonResp)
	return shim.Success(Avalbytes)
}

//==================================
//通过id查询历史记录 args：ID
//==================================
func (t *ServiceChaincode) getHistoryForKey(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 2,function followed by 1 accountID and 1 value")
	}

	var serviceID string //Entities
	var err error
	serviceID = args[0]
	//Get the state from the ledger
	//TODD:will be nice to have a GetAllState call to ledger
	HisInterface, err := stub.GetHistoryForKey(serviceID)
	fmt.Println(HisInterface)
	Avalbytes, err := getHistoryListResult(HisInterface)
	if err != nil {
		return shim.Error("Failed to get history")
	}
	return shim.Success([]byte(Avalbytes))
}

//==================================
//范围查询，参数已经固定，不需要传入
//==================================
func (t *ServiceChaincode) testRangeQuery(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	resultsIterator, err := stub.GetStateByRange("b1001", "b1010")
	if err != nil {
		return shim.Error("Query by Range failed")
	}
	services, err := getListResult(resultsIterator)
	if err != nil {
		return shim.Error("getListResult failed")
	}
	return shim.Success(services)
}

func getHistoryListResult(resultsIterator shim.HistoryQueryIteratorInterface) ([]byte, error) {

	defer resultsIterator.Close()
	// buffer is a JSON array containing QueryRecords
	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}
		// Add a comma before array members, suppress it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		item, _ := json.Marshal(queryResponse)
		buffer.Write(item)
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")
	fmt.Printf("queryResult:\n%s\n", buffer.String())
	return buffer.Bytes(), nil
}

func getListResult(resultsIterator shim.StateQueryIteratorInterface) ([]byte, error) {

	defer resultsIterator.Close()
	// buffer is a JSON array containing QueryRecords
	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}
		// Add a comma before array members, suppress it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		buffer.WriteString("{\"Key\":")
		buffer.WriteString("\"")
		buffer.WriteString(queryResponse.Key)
		buffer.WriteString("\"")

		buffer.WriteString(", \"Record\":")
		// Record is a JSON object, so we write as-is
		buffer.WriteString(string(queryResponse.Value))
		buffer.WriteString("}")
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")
	fmt.Printf("queryResult:\n%s\n", buffer.String())
	return buffer.Bytes(), nil
}

//=================================
//main function
//=================================
func main() {
	err := shim.Start(new(ServiceChaincode))
	if err != nil {
		fmt.Printf("Error starting RegisterChaincode:%s", err)
	}
}
