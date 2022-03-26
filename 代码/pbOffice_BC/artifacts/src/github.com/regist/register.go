package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

var logger = shim.NewLogger("register")

//RegisterChaincode implementation
type RegisterChaincode struct {
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
func (t *RegisterChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Info("######## Register Init ########")

	return shim.Success(nil)
}

//Invoke interface
func (t *RegisterChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Info("######## Register Invoke ########")

	function, args := stub.GetFunctionAndParameters()

	if function == "regist" {
		//Register an entity to its state
		return t.regist(stub, args)
	}
	if function == "login" {
		//login to the entity
		return t.login(stub, args)
	}
	if function == "changePwd" {
		//change Password of the entity
		return t.changePwd(stub, args)
	}
	if function == "update" {
		//update the entity
		return t.update(stub, args)
	}
	if function == "delete" {
		//Delete an entities from its state
		return t.delete(stub, args)
	}
	if function == "query" {
		//Delete an entities from its state
		return t.query(stub, args)
	}
	if function == "getHistoryForKey" {
		//getHistoryForKey an entities from its state
		return t.getHistoryForKey(stub, args)
	}
	if function == "transfer" {
		//transfer an entities from its state
		return t.transfer(stub, args)
	}
	logger.Errorf("Unknown action, check the first argument, must be one of 'delete', 'query', or 'move'."+
		" But got: %v", args[0])
	return shim.Error(fmt.Sprintf("Unknown action, check the first argument, must be one of 'regist',or 'delete'."+
		" But got: %v", args[0]))
}

//==================
//创建账号 args:ID | Password | Role | Balance | CrdPoint | Funpoint
//==================
func (t *RegisterChaincode) regist(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("######## Register regist ########")
	if len(args) != 6 {
		return shim.Error("Incorrect number of arguments. Expecting 2,function followed by 1 accountID and 1 value")
	}

	var accountID string      //Entities
	var Password, Role string //Password,Roel
	//	var Balance, CrdPoint, FunPoint string //账户金额、信用积分、功能积分
	var err error

	accountID = args[0]
	Password = args[1]
	Role = args[2]
	//	Balance = args[3]
	//	CrdPoint = args[4]
	//	FunPoint = args[5]

	var user User

	user.ID = accountID
	user.Password = Password
	user.Role = Role
	Balance, _ := strconv.ParseFloat(args[3], 64)
	user.Balance = Balance
	CrdPoint, _ := strconv.Atoi(args[4])
	user.CrdPoint = CrdPoint
	FunPoint, _ := strconv.Atoi(args[5])
	user.FunPoint = FunPoint
	uBytes, _ := json.Marshal(user)
	//Get the state from the ledger
	//TODD:will be nice to have a GetAllState call to ledger
	Avalbytes, err := stub.GetState(accountID)
	fmt.Println(string(Avalbytes))
	if err != nil {
		return shim.Error("Failed to get state")
	}
	if Avalbytes != nil {
		return shim.Error("this user already exist")
	}

	//Write the state back to the ledger
	err = stub.PutState(accountID, uBytes)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success([]byte(accountID + "账号创建成功！"))

}

//================
//验证账号密码是否匹配 args:ID|Password
//================
func (t *RegisterChaincode) login(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	accountID := args[0]
	password := args[1]

	//query the ledger
	bytes, err := stub.GetState(accountID)
	if err != nil {
		return shim.Error("Failed to get account: " + err.Error())
	}
	if bytes == nil {
		return shim.Error("This account does not exists: " + accountID)
	}

	var user User
	err = json.Unmarshal(bytes, &user)
	if err != nil {
		return shim.Error("Failed to get account: " + err.Error())
	}

	if user.Password == password {
		return shim.Success([]byte("correct password"))
	} else {
		return shim.Error("wrong password ")
	}
}

//================================
//查询账号 args:ID
//================================
func (t *RegisterChaincode) query(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("######## Register query ########")
	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting name of the person to query")
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

//==============================
//更改用户密码 args:ID| OldPassword |newPassword
//==============================
func (t *RegisterChaincode) changePwd(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	fmt.Println("ChangePassword")

	if len(args) != 3 {
		return shim.Error("Incorrect number of arguments. Expecting 3")
	}

	//var account Account
	userID := args[0]
	userPassword := args[1]
	newPassword := args[2]
	var err error

	//query the ledger
	Bytes, _ := stub.GetState(userID)
	if err != nil {
		return shim.Error("Failed to get account: " + err.Error())
	}
	if Bytes == nil {
		return shim.Error("This accountt does not exists: " + userID)
	}
	var user User

	err = json.Unmarshal(Bytes, &user)
	if err != nil {
		return shim.Error("Failed to get account: " + err.Error())
	}
	//change password
	if user.Password == userPassword {
		user.Password = newPassword
	} else {
		return shim.Error("wrong password")
	}

	bytes, _ := json.Marshal(user)
	err = stub.PutState(userID, bytes)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success([]byte("密码更改成功"))

}

//==============================
//更新账户信息 args:ID| Balance |CrdPoint | FunPoint
//==============================
func (t *RegisterChaincode) update(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	fmt.Println("update")
	fmt.Println(args)
	if len(args) != 4 {
		return shim.Error("Incorrect number of arguments. Expecting 4")
	}

	//var account Account
	userID := args[0]
	Balance, err := strconv.ParseFloat(args[1], 64)
	if err != nil {
		fmt.Println("err:111111111111111111111111")
		return shim.Error("Failed to conver balance from string to float64: " + err.Error())
	}
	fmt.Println(Balance)
	CrdPoint, err := strconv.Atoi(args[2])
	fmt.Println("")
	fmt.Println("")
	if err != nil {
		fmt.Printf("err:222222222222222222222222:%s:2222:%d:\n", args[2], CrdPoint)
		return shim.Error("Failed to conver CrdPoint from string to int: " + err.Error())
	}
	FunPoint, err := strconv.Atoi(args[3])
	if err != nil {
		fmt.Println("err:3333333333333333333333333")
		return shim.Error("Failed to conver FunPoint from string to int: " + err.Error())
	}
	//query the ledger
	Bytes, _ := stub.GetState(userID)
	if err != nil {
		fmt.Println("err:444444444444444444444444")
		return shim.Error("Failed to get account: " + err.Error())
	}
	if Bytes == nil {
		fmt.Println("err:55555555555555555555555")
		return shim.Error("This accountt does not exists: " + userID)
	}
	var user User

	err = json.Unmarshal(Bytes, &user)
	if err != nil {
		fmt.Println("err:66666666666666666666666")
		return shim.Error("Failed to get account: " + err.Error())
	}
	user.Balance = Balance
	user.CrdPoint = CrdPoint
	user.FunPoint = FunPoint
	bytes, _ := json.Marshal(user)
	err = stub.PutState(userID, bytes)
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success([]byte("修改账户成功"))

}

//================
//删除账号
//================
func (t *RegisterChaincode) delete(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("Deleteusr")

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	accountID := args[0]
	uBytes, err := stub.GetState(accountID)
	if err != nil {
		return shim.Error("Fail to get user:" + err.Error())
	}
	if uBytes == nil {
		return shim.Error("this user is not found")
	}

	var user User
	err = json.Unmarshal(uBytes, &user)
	if err != nil {
		return shim.Error("Fail to get account:" + err.Error())
	}

	user = User{} //delete the user
	uBytes, err = json.Marshal(user)
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState(accountID, uBytes)

	err = stub.DelState(accountID)
	if err != nil {
		return shim.Error("Failed to delete state:" + err.Error())
	}
	err = stub.DelState(accountID)
	if err != nil {
		return shim.Success([]byte("delete sucessfully"))
	}

	return shim.Success([]byte("该账号已被删除"))
}

func (t *RegisterChaincode) getHistoryForKey(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("######## getHistoryForKey ########")
	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 2,function followed by 1 accountID and 1 value")
	}

	var accountID string //Entities
	var err error
	accountID = args[0]
	//Get the state from the ledger
	//TODD:will be nice to have a GetAllState call to ledger
	HisInterface, err := stub.GetHistoryForKey(accountID)
	fmt.Println(HisInterface)
	Avalbytes, err := getHistoryListResult(HisInterface)
	if err != nil {
		return shim.Error("Failed to get history")
	}
	return shim.Success([]byte(Avalbytes))
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

//=================================
// 积分转账
//=================================
func (t *RegisterChaincode) transfer(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("######## transfer ########")
	if len(args) != 3 {
		return shim.Error("Incorrect number of arguments. Expecting 3,function followed by 2 accountID and 1 value")
	}
	fmt.Println(args)
	var accountID1, accountID2 string //Entities
	var err error
	var user1, user2 User
	accountID1 = args[0]
	accountID2 = args[1]
	funPoint, err := strconv.Atoi(args[2])
	fmt.Printf("funPoint:%d\n", funPoint)
	if err != nil {
		return shim.Error("Failed to convert " + args[2])
	}
	//Get the state from the ledger
	//TODD:will be nice to have a GetAllState call to ledger
	userBytes, err := stub.GetState(accountID1)
	fmt.Printf("user1:%s\n", userBytes)
	if err != nil {
		return shim.Error("Failed to get" + accountID1)
	}
	err = json.Unmarshal(userBytes, &user1)
	fmt.Printf("user1 v:%v\n", user1)
	if err != nil {
		return shim.Error("Failed to Unmashal user1" + string(userBytes))
	}
	userBytes, err = stub.GetState(accountID2)
	fmt.Printf("user2:%s\n", userBytes)
	if err != nil {
		return shim.Error("Failed to get" + accountID2)
	}
	err = json.Unmarshal(userBytes, &user2)
	fmt.Printf("user2 v:%v\n", user2)
	if err != nil {
		return shim.Error("Failed to Unmashal user2" + string(userBytes))
	}
	user1.FunPoint = user1.FunPoint - funPoint
	user2.FunPoint = user2.FunPoint + funPoint
	userBytes, err = json.Marshal(user1)
	if err != nil {
		return shim.Error("Failed to Mashal user1")
	}
	err = stub.PutState(accountID1, userBytes)
	if err != nil {
		shim.Error("Fail to putstate user1 bytes")
	}
	userBytes, _ = json.Marshal(user2)
	err = stub.PutState(accountID2, userBytes)
	if err != nil {
		shim.Error("Fail to putstate user2 bytes")
	}
	return shim.Success([]byte([]byte{}))
}

//=================================
//main function
//=================================
func main() {
	err := shim.Start(new(RegisterChaincode))
	if err != nil {
		fmt.Printf("Error starting RegisterChaincode:%s", err)
	}
}
