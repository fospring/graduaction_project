#include "blockchain.h"

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <mysql.h>

int bc_user_query(Json::Value &input, Json::Value &output)
{
	MYSQL     *mysql;
	MYSQL_RES *result;
	MYSQL_ROW  row;
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

	/* 查询个人信息 */
	sprintf(query, "select user_name,user_phone,user_email,user_addr,user_type,user_company,user_represent,user_income from user where user_no=\"%s\"", input["user_no"].asString().c_str());
	if (mysql_query(mysql, query) || (result = mysql_store_result(mysql)) == NULL)//查询失败
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
	}
	else
	{
		row = mysql_fetch_row(result);
		output["result"] = "sucess";
		if (row[0])
			output["user"] = row[0];
		if (row[1])
			output["phone"] = row[1];
		if (row[2])
			output["email"] = row[2];
		if (row[3])
			output["address"] = row[3];
		if (row[4])
			output["type"] = row[4];
		if (row[5])
			output["company"] = row[5];
		if (row[6])
			output["represent"] = row[6];
		if (row[7])
			output["income"] = atof(row[7]);
	}

	/* 关闭连接 */
	mysql_close(mysql);
	
	return 0;
}