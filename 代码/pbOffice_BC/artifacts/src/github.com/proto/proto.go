package proto

//==================
//定义工位/服务结构
//==================
type Service struct {
	//工位/服务id
	ID string `json:"id"`
	//发布者id
	OwnerID string `json:"ownerId"`
	//类型，1为工位，2为其他服务
	Type string `json:"type"`
	//工位名称
	Name string `json:"name"`
	//工位名称
	Function string `json:"function"`
	//价格
	Money float64 `json:"money"`
	//功能积分（与价格二选一支付）
	FunPoint int `json:"funPoint"`
	//描述
	Description string `json:"description"`
	//状态，0为可租用，1为不可租用，被占用
	State string `json:"state"`
	//是否设置特权，false无，true有
	Top bool `json:"top"`
	//设置特权是支付的积分，至少为1
	TopPoint int `json:"topPoint"`
	//租用者id
	UserID string `json:"userId"`
	//租用者评论集合
	Comments []Comment `json:"comment"`
}

//==========================
//用户评论
//==========================
type Comment struct {
	CusHash string `json:"cusHash"` //Id hash
	//评分，1~5
	Grade int `json:"gevel"` //bad -> good from 1 to 5
	//具体评论
	Comment string `json:"comment"`
}

//==================
//定义用户账户结构
//==================
type User struct {
	//用户Id
	ID string `json:"id"`
	//密码
	Password string `json:"password"`
	//角色，1为孵化器、众创空间，2为其他公司与个人
	Role string `json:"role"`
	//账户金额
	Balance float64 `json:"balance"`
	//功能积分
	CrdPoint int `json:"crePoint"`
	//诚信积分（预留）
	FunPoint int `json:"funcPoint"`
}
