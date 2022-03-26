#include "blockchain.h"

#include <stdio.h>
#include <unistd.h>
#include <mysql.h>

int bc_user_revise(Json::Value &input, Json::Value &output)
{
	MYSQL     *mysql;
	char       query[512];
	int        pos = 0;

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

	/* 修改个人信息 */
	// 手机号
	if (!input["phone"].isNull())
		pos = sprintf(query, "update user set user_phone=\"%s\"", input["phone"].asString().c_str());
	// email
	if (!input["email"].isNull())
	{
		if (pos)
			pos += sprintf(query + pos, ",user_email=\"%s\"", input["email"].asString().c_str());
		else
			pos = sprintf(query, "update user set user_email=\"%s\"", input["email"].asString().c_str());
	}
	// 地址
	if (!input["address"].isNull())
	{
		if (pos)
			pos += sprintf(query + pos, ",user_addr=\"%s\"", input["address"].asString().c_str());
		else
			pos = sprintf(query, "update user set user_addr=\"%s\"", input["address"].asString().c_str());
	}
	// 收入
	if (!input["income"].isNull())
	{
		if (pos)
			pos += sprintf(query + pos, ",user_income=\"%s\"", input["income"].asString().c_str());
		else
			pos = sprintf(query, "update user set user_income=\"%s\"", input["income"].asString().c_str());
	}
	
	if (pos) //需要修改
	{
		sprintf(query + pos, " where user_no=\"%s\"", input["userno"].asString().c_str());
		/* 将结果插入数据库 */
		if(mysql_query(mysql, query))//插入失败
		{
			output["result"] = "error";
			output["errtype"] = SERVER_ERROR_DATABASE;
		}
		else
			output["result"] = "success";
	}

	/* 关闭连接 */
	mysql_close(mysql);
	
	return 0;
}