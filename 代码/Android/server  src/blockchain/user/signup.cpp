#include "blockchain.h"
#include "extra.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <mysql.h>

int bc_user_signup(Json::Value &input, Json::Value &output)
{
	MYSQL     *mysql;
	MYSQL_RES *result;
	MYSQL_ROW  row;
	char       query[512];
	int        pos;
	time_t     timep;

	Json::Value args, res;

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

	/* 个人信息 */
	// 用户名、手机号
	pos = sprintf(query, "insert into user values(0,\"%s\",\"%s\",", input["user"].asString().c_str(), input["phone"].asString().c_str());
	// email
	if (!input["email"].isNull())
		pos += sprintf(query + pos, "\"%s\",", input["email"].asString().c_str());
	else
		pos += sprintf(query + pos, "NULL,");
	// 地址
	if (!input["address"].isNull())
		pos += sprintf(query + pos, "\"%s\",", input["address"].asString().c_str());
	else
		pos += sprintf(query + pos, "NULL,");
	// 注册时间
	time(&timep); //获取当前时间
	pos += strftime(query + pos, sizeof(query) - pos, "\"%Y-%m-%d %H:%M:%S\",", localtime(&timep));//注册时间
	// 用户类型
	pos += sprintf(query + pos, "\"%s\",", input["type"].asString().c_str());
	// 公司名
	if (input["type"].asString() == "enterprise" && !input["company"].isNull())
		pos += sprintf(query + pos, "\"%s\",", input["company"].asString().c_str());
	else
		pos += sprintf(query + pos, "NULL,");
	// 初始信用积分、个人姓名
	pos += sprintf(query + pos, "%d,\"%s\",", INIT_ASSET, input["represent"].asString().c_str());
	// 收入
	if (!input["income"].isNull())
		sprintf(query + pos, "%.2f,\"signout\",0,0)", input["income"].asFloat());
	else
		sprintf(query + pos, "NULL,\"signout\",0,0)");

	/* 进入临界区 */
	pthread_mutex_lock(&user_mutex);

	/* 将结果插入数据库 */
	if(mysql_query(mysql, query))//插入失败
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
		mysql_close(mysql);
		pthread_mutex_unlock(&user_mutex);
		return 0; 
	}

	/* 插入成功，查询用户编号 */
	sprintf(query, "select user_no from user where user_name=\"%s\" order by user_no desc", input["user"].asString().c_str());
	if (mysql_query(mysql, query) || (result = mysql_store_result(mysql)) == NULL)//查询失败
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
		mysql_close(mysql);
		pthread_mutex_unlock(&user_mutex);
		return 0; 
	}
	row = mysql_fetch_row(result);

	/* 离开临界区 */
	pthread_mutex_unlock(&user_mutex);

	args.append(row[0]);  //用户名
	args.append(input["passwd"]);//用户密码
	args.append("2");            //1代表孵化器、众创空间，可发布工位与服务；2代表需要工位的公司或个人
	args.append("1000");         //账户余额（float64型字符串）
	args.append("1000");         //信用积分（整型字符串）,初始积分为1000
	args.append("1000");         //功能积分（整型字符串）
	bc_post(INVOKE_URL, USER_CODE, USER_SIGNUP, &args, &res);

	if (res["success"].asBool())
	{
		output["result"] = "success";
		output["user_no"] = (Json::Value::Int64)atoll(row[0]);
	}
	else
	{
		output["result"] = "error";
		if (res["errorcode"].isNull())
			output["errtype"] = CLIENT_ERROR_SAME;
		else
			output["errtype"] = res["errorcode"];
	}

	/* 关闭连接 */
	mysql_close(mysql);
	
	return 0;
}