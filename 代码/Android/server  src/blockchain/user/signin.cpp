#include "blockchain.h"
#include "extra.h"

#include <stdio.h>
#include <mysql.h>
#include <unistd.h>

int bc_user_signin(Json::Value &input, Json::Value &output)
{
	MYSQL     *mysql;
	MYSQL_RES *result;
	MYSQL_ROW  row;
	char       query[512];
	Json::Value args, res;
	char user_no[30];

	sprintf(user_no, "%lld", input["user_no"].asInt64());
	args.append(user_no);  //用户名
	args.append(input["passwd"]);//用户密码
	bc_post(INVOKE_URL, USER_CODE, USER_SIGNIN, &args, &res);

	if (!res["success"].asBool())
	{
		output["result"] = "error";
		if (res["errorcode"].isNull())
			output["errtype"] = CLIENT_ERROR_NOUSER;
		else
			output["errtype"] = res["errorcode"];

		return 0;
	}

	/* 初始化 mysql 变量，失败返回NULL */
	if ((mysql = mysql_init(NULL))==NULL)
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
		return 0;
	}

	/* 连接数据库，失败返回NULL
	   1、mysqld没运行
	   2、没有指定名称的数据库存在 */
	while (mysql_real_connect(mysql, HOST, USER, PASSWORD, DB, 0, NULL, 0)==NULL)
		sleep(1);

	/* 设置字符集，否则读出的字符乱码，即使/etc/my.cnf中设置也不行 */
	mysql_set_character_set(mysql, "utf8");

	/* 查询信息信息 */
	sprintf(query, "select user_name from user where user_no=%lld", input["user_no"].asInt64());
	if(mysql_query(mysql, query) || (result = mysql_store_result(mysql)) == NULL || !mysql_num_rows(result))//查询失败
	{
		output["result"] = "error";
		output["errtype"] = CLIENT_ERROR_NOUSER;
	}
	else 
	{
		row = mysql_fetch_row(result);
		output["result"] = "success";
		output["user_no"] = input["user_no"];
		output["user"] = row[0];
	}

	/* 关闭连接 */
	mysql_close(mysql);
	
	return 0;
}