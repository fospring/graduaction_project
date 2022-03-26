#include "blockchain.h"
#include "extra.h"

int bc_user_chgpwd(Json::Value &input, Json::Value &output)
{
	Json::Value args, res;

	args.append(input["userno"]);   //用户名
	args.append(input["oldpasswd"]);//旧密码
	args.append(input["newpasswd"]);//新密码
	bc_post(INVOKE_URL, USER_CODE, USER_CHGPWD, &args, &res);

	if (res["success"].asBool())
		output["result"] = "success";
	else
	{
		output["result"] = "error";
		if (res["errorcode"].isNull())
			output["errtype"] = CLIENT_ERROR_ERRPWD;
		else
			output["errtype"] = res["errorcode"];
	}
	return 0;
}