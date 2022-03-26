/*
*本合约的主要功能是发布工位与服务
 */
package main

import (
	"encoding/json"
	"fmt"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

var logger = shim.NewLogger("Service")

//CommentChaincode implementation
type CommentChaincode struct {
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

//安装Chaincode
func (t *CommentChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Info("######## Register Init ########")

	return shim.Success(nil)
}

//Invoke interface
func (t *CommentChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Info("######## Register Invoke ########")

	function, args := stub.GetFunctionAndParameters()

	if function == "addComment" {
		//Add an entity to its state
		return t.addComment(stub, args)
	}

	if function == "query" {
		//Query an entities from its state
		return t.query(stub, args)
	}

	if function == "deleteComment" {
		//Delete an entities from its state
		return t.deleteComment(stub, args)
	}

	logger.Errorf("Unknown action, check the first argument, must be one of 'addService', 'appService', 'confirmService'"+
		"'resetService', 'queryService' or 'deleteService', But got: %v", args[0])
	return shim.Error(fmt.Sprintf("Unknown action, check the first argument, must be one of 'addService', 'appService', 'confirmService'"+
		" 'resetService', 'queryService' or 'deleteService', But got: %v", args[0]))
}

//===================================================
//增加评论 args:userID | serviceID | grade | comment
//===================================================
// func (t *CommentChaincode) addComment(stub shim.ChaincodeStubInterface, args []string) pb.Response {

// 	if len(args) != 4 {
// 		return shim.Error("Incorrect number of arguments. Expecting 4")
// 	}

// 	userID := args[0]
// 	serviceID := args[1]
// 	grade := args[2]
// 	if err != nil {
// 		shim.Error("Cann't convert grade from string to string")
// 	}
// 	comment := args[3]

// 	//query the ledger
// 	//bytes, err := stub.GetState(serviceID)
// 	query := [][]byte{[]byte("query"), []byte(serviceID)}
// 	fmt.Println(query)
// 	response := stub.InvokeChaincode("publicService", query, "mychannel")

// 	if response.Status != int32(200) {
// 		return shim.Error("Failed to get service: " + response)
// 	}
// 	if response.GetPayload() == nil {
// 		return shim.Error("This service does not exists: " + serviceID)
// 	}
// 	// var commentSt Comment
// 	// commentSt.CusHash = userID
// 	// commentSt.Grade = grade
// 	// commentSt.Comment = comment
// 	//	commentSt := Comment{
// 	//                CusHash: userID,
// 	//		Grade:    grade,
// 	//		Comment: comment
// 	//	}
// 	// var service Service
// 	// bytes := response.GetPayload()
// 	// err = json.Unmarshal(bytes, &service)
// 	// if err != nil {
// 	// 	shim.Error("Cann't convert to Service")
// 	// }
// 	// service.Comments = append(service.Comments, commentSt)
// 	// bytes, _ = json.Marshal(service)

// 	//update user state
// 	//balance := strconv.FormatFloat(user.Balance, 'E', -1, 64)
// 	trans := [][]byte{[]byte("addComment"), []byte(userID), []byte(serviceID), []byte(grade), []byte(comment)}
// 	response := stub.InvokeChaincode("publicService", trans, "mychannel")

// 	err = stub.PutState(serviceID, bytes)

// 	OwnerID := service.OwnerID
// 	var owner, user User
// 	bytes, err = stub.GetState(OwnerID)
// 	err = json.Unmarshal(bytes, owner)
// 	if err != nil {
// 		shim.Error("Cann't convert to Service")
// 	}
// 	if grade == 0 {
// 		owner.CrdPoint -= 5
// 	} else {
// 		owner.CrdPoint += 5
// 	}
// 	bytes, _ = json.Marshal(owner)
// 	err = stub.PutState(OwnerID, bytes)

// 	bytes, err = stub.GetState(userID)
// 	err = json.Unmarshal(bytes, user)
// 	if err != nil {
// 		shim.Error("Cann't convert to Service")
// 	}
// 	user.FunPoint += 5
// 	bytes, _ = json.Marshal(user)
// 	err = stub.PutState(userID, bytes)

// 	return shim.Error("Don't have enough credit points.Please do something to add your credit points.")
// }
//===================================================
//增加评论 args:userID | serviceID | grade | comment
//===================================================
func (t *CommentChaincode) addComment(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 4 {
		return shim.Error("Incorrect number of arguments. Expecting 4")
	}

	userID := args[0]
	serviceID := args[1]
	grade, err := strconv.Atoi(args[2])
	//grade := args[2]
	if err != nil {
		return shim.Error("can't convert grade from string to int")
	}
	comment := args[3]

	//query the ledger
	//bytes, err := stub.GetState(serviceID)
	query := [][]byte{[]byte("query"), []byte(serviceID)}
	fmt.Println(query)
	response := stub.InvokeChaincode("publicService", query, "mychannel")

	if fmt.Sprint(response.Status) != "200" {
		return shim.Error("Failed to get service: " + response.GetMessage() + fmt.Sprint(response.Status) + " serviceID:" + serviceID)
	}
	if response.GetPayload() == nil {
		return shim.Error("This service does not exists: " + serviceID)
	}
	var service Service
	json.Unmarshal(response.GetPayload(), &service)

	StrGrade := strconv.Itoa(grade)
	fmt.Printf("+++++++++++++++++++++++grade:%s++++++++++++++++++++++++==\n", StrGrade)
	trans := [][]byte{[]byte("addComment"), []byte(userID), []byte(serviceID), []byte(StrGrade), []byte(comment)}
	response = stub.InvokeChaincode("publicService", trans, "mychannel")
	if response.Status != int32(200) {
		return shim.Error("Failed to get addComment: " + response.GetMessage())
	}
	fmt.Printf("++++++++++ddddddddd %s ddddddd++++++++\n", response.GetPayload())
	OwnerID := service.OwnerID
	var owner, user User
	// bytes, err = stub.GetState(OwnerID)
	// err = json.Unmarshal(bytes, owner)
	trans = [][]byte{[]byte("query"), []byte(OwnerID)}
	response = stub.InvokeChaincode("register", trans, "mychannel")
	if response.Status != int32(200) {
		return shim.Error(string(response.GetPayload()) + "++++aaaaaaaaaaaaaaaFailed to get owner: " + OwnerID + response.GetMessage())
	}
	if response.GetPayload() == nil {
		shim.Error("Cann't get the owner")
	}
	json.Unmarshal(response.GetPayload(), &owner)
	fmt.Printf("++++++++++eeeeeeee %s eeeeeeeee++++++++\n", response.GetPayload())
	//grade, err := strconv.Atoi(args[2])
	if grade == 0 {
		owner.CrdPoint -= 5
	} else {
		owner.CrdPoint += 5
	}
	//bytes, _ = json.Marshal(owner)
	//err = stub.PutState(OwnerID, bytes)
	balance := strconv.FormatFloat(owner.Balance, 'E', -1, 64)
	strGrdPoint := strconv.Itoa(owner.CrdPoint)
	strFunPoint := strconv.Itoa(owner.FunPoint)
	fmt.Printf("owner:%v/n", owner)
	fmt.Printf("\n+++++++++++++++++++++++grade:%s;funPoint:%s;balance:%s;crePoint:%s++++++++++++++++++++++++==\n\n", StrGrade, strFunPoint, balance, strGrdPoint)
	trans = [][]byte{[]byte("update"), []byte(OwnerID), []byte(string(balance)), []byte(strGrdPoint), []byte(strFunPoint)}
	response = stub.InvokeChaincode("register", trans, "mychannel")
	fmt.Println("+++++++++++++++++++++++++++++++++++++aTAG++++++++++++++++++++++++++++++++++++++++++++++==")
	// bytes, err = stub.GetState(userID)
	// err = json.Unmarshal(bytes, user)
	// if err != nil {
	// 	shim.Error("Cann't convert to Service")
	// }
	// user.FunPoint += 5
	// bytes, _ = json.Marshal(user)
	// err = stub.PutState(userID, bytes)
	trans = [][]byte{[]byte("query"), []byte(userID)}
	response = stub.InvokeChaincode("register", trans, "mychannel")
	if response.Status != int32(200) {
		return shim.Error("bbbbbbbbbbbbbbbbbbbbbbbbbFailed to get user: " + userID + response.GetMessage())
	}
	if response.GetPayload() == nil {
		shim.Error("Cann't get the user")
	}
	json.Unmarshal(response.GetPayload(), &user)
	fmt.Printf("++++++++++eeeeeeee %s eeeeeeeee++++++++\n", response.GetPayload())
	user.FunPoint += 5
	//bytes, _ = json.Marshal(owner)
	//err = stub.PutState(OwnerID, bytes)
	balance = strconv.FormatFloat(user.Balance, 'E', -1, 64)
	fmt.Printf("user:%v/n", user)
	strGrdPoint = strconv.Itoa(user.CrdPoint)
	strFunPoint = strconv.Itoa(user.FunPoint)
	fmt.Printf("+++++++++++++++++++++++grade:%s;funPoint:%s;balance:%s++++++++++++++++++++++++==\n", StrGrade, strFunPoint, balance)
	trans = [][]byte{[]byte("update"), []byte(userID), []byte(string(balance)), []byte(strGrdPoint), []byte(strFunPoint)}
	response = stub.InvokeChaincode("register", trans, "mychannel")
	fmt.Println("+++++++++++++++++++++++++++++++++++++aTAG++++++++++++++++++++++++++++++++++++++++++++++==")
	if response.Status != int32(200) {
		return shim.Error("Failed to update user: " + response.GetMessage())
	}
	return shim.Success([]byte("Successfully add comment."))
}

//================================
//删除评论 args userID | serverID
//================================
func (t *CommentChaincode) deleteComment(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("deleteComment")

	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}
	userID := args[0]
	serviceID := args[1]
	//获取服务信息
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

	//service = Service{} //delete the service
	// bytes, err = json.Marshal(service)
	// if err != nil {
	// 	return shim.Error(err.Error())
	// }
	var comments []Comment
	//comments = service.Comments
	for i := range service.Comments {
		if service.Comments[i].CusHash == userID {
			comments = append(service.Comments[:i], service.Comments[i+1:]...)
		}
	}
	service.Comments = comments
	bytes, _ = json.Marshal(service)
	err = stub.PutState(serviceID, bytes)
	if err != nil {
		return shim.Error("Failed to delete state:" + err.Error())
	}

	return shim.Success([]byte("该评论已被删除"))
}

//================================
//查询评论 args:ID | serviceID
//================================
func (t *CommentChaincode) query(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting userID and serviceID of the Service to query")
	}

	var userID, serviceID string // Entities
	var err error

	userID = args[0]
	serviceID = args[2]
	// Get the state from the ledger
	Avalbytes, err := stub.GetState(serviceID)
	if err != nil {
		jsonResp := "{\"Error\":\"Failed to get state for " + userID + "\"}"
		return shim.Error(jsonResp)
	}

	if Avalbytes == nil {
		jsonResp := "{\"Error\":\"Nil service for " + userID + "\"}"
		return shim.Error(jsonResp)
	}
	var service Service
	err = json.Unmarshal(Avalbytes, &service)
	if err != nil {
		return shim.Error("Failed to convert []bytes to Service")
	}
	//comments = service.Comments
	for i := range service.Comments {
		if service.Comments[i].CusHash == userID {
			Avalbytes, _ = json.Marshal(service.Comments[i])
			return shim.Success([]byte("{\"userID\":" + userID + ",\"comment\":" +
				string(Avalbytes) + "\"}"))
			//       return shim.Success(Avalbytes)
		}
	}
	return shim.Error("Failed to query the comment")
}

//=================================
//main function
//=================================
func main() {
	err := shim.Start(new(CommentChaincode))
	if err != nil {
		fmt.Printf("Error starting CommentChaincode:%s", err)
	}
}
