#include "blockchain.h"

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <mysql.h>
#include <time.h>

int bc_station_rent(Json::Value &input, Json::Value &output)
{
	MYSQL     *mysql;
	MYSQL_RES *result;
	MYSQL_ROW  row;
	char       query[512], select[512];
	int        pos;
	time_t     timep;

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

	/* 查询工位状态 */
	sprintf(query, "select station_user_no,station_status from station where station_no=%lld", input["station_no"].asInt64());
	if(mysql_query(mysql, query) || (result = mysql_store_result(mysql)) == NULL)//查询失败
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
	}
	else if (!mysql_num_rows(result)) //结果为空
	{
		output["result"] = "error";
		output["errtype"] = CLIENT_ERROR_NOSTATION;
	}
	else
	{
		row = mysql_fetch_row(result);
		if (input["user_no"].asInt64() == atoll(row[0]))
		{
			output["result"] = "error";
			output["errtype"] = CLIENT_ERROR_SELF;
		}
		else if (!strcmp(row[1], "busy"))//工位繁忙
		{
			output["result"] = "error";
			output["errtype"] = CLIENT_ERROR_BUSY;
		}
		else
		{
			/* 查询这笔交易是否已经存在 */
			sprintf(select, "select trade_no from trade where trade_station_no=%lld and trade_buyer_no=%lld and trade_status='work'", input["station_no"].asInt64(), input["user_no"].asInt64());
			if(mysql_query(mysql, select) || (result = mysql_store_result(mysql)) == NULL)//查询失败
			{
				output["result"] = "error";
				output["errtype"] = SERVER_ERROR_DATABASE;
			}
			else if (mysql_num_rows(result))//交易已经存在
			{
				row = mysql_fetch_row(result);
				output["result"] = "success";
				output["trade_no"] = (Json::Value::Int64)atoll(row[0]);
			}
			else
			{
				/* 产生交易 */
				pos = sprintf(query, "insert into trade values(0,%lld,%lld,%s,'work',", input["station_no"].asInt64(), input["user_no"].asInt64(), row[0]);
				time(&timep); //获取当前时间
				pos += strftime(query + pos, sizeof(query) - pos, "\"%Y-%m-%d %H:%M:%S\",", localtime(&timep));//开始时间
				timep += input["time"].asInt() * 24 * 3600;
				strftime(query + pos, sizeof(query) - pos, "\"%Y-%m-%d %H:%M:%S\")", localtime(&timep));//开始时间
				if(mysql_query(mysql, query))//插入失败
				{
					output["result"] = "error";
					output["errtype"] = SERVER_ERROR_DATABASE;
				}
				else if (mysql_query(mysql, select) || (result = mysql_store_result(mysql)) == NULL)//查询这笔正在等待的交易的编号
				{
					output["result"] = "error";
					output["errtype"] = SERVER_ERROR_DATABASE;
				}
				else
				{
					row = mysql_fetch_row(result);
					output["result"] = "success";
					output["trade_no"] = (Json::Value::Int64)atoll(row[0]);
				}
				/* 工位状态设为繁忙 */
				sprintf(query, "update station set station_status='busy' where station_no=%lld", input["station_no"].asInt64());
				mysql_query(mysql, query);
			}
		}
	}
	
	/* 断开连接 */
	mysql_close(mysql);
	return 0;
}