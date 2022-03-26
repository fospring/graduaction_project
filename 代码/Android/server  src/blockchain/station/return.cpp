#include "blockchain.h"
#include "extra.h"

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <mysql.h>

int bc_station_return(Json::Value &input, Json::Value &output)
{
	MYSQL     *mysql;
	MYSQL_RES *result;
	MYSQL_ROW  row;
	char       query[512];
	int        pos;

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

	/* 查询交易是否存在 */
	sprintf(query, "select trade_status from trade where trade_no=%lld", input["trade_no"].asInt64());
	if(mysql_query(mysql, query) || (result = mysql_store_result(mysql)) == NULL)//查询失败
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
	}
	else if (!mysql_num_rows(result)) //结果为空
	{
		output["result"] = "error";
		output["errtype"] = CLIENT_ERROR_NOTRADE;
	}
	else
	{
		row = mysql_fetch_row(result);
		if (!strcmp(row[0], "finish"))//交易已经结束
		{
			output["result"] = "error";
			output["errtype"] = CLIENT_ERROR_FINISHED;
		}
		else
			output["result"] = "success";
	}
	
	/* 断开连接 */
	mysql_close(mysql);
	return 0;
}