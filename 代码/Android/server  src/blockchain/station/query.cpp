#include "blockchain.h"
#include "extra.h"

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <mysql.h>

int bc_station_query(Json::Value &input, Json::Value &output)
{
	MYSQL     *mysql;
	MYSQL_RES *result;
	MYSQL_ROW  row;
	char       query[512];
	int        i;
	
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

	/* 查询工位，最多5条 */
	sprintf(query, "select station_no,station_user_no from station where station_no>%lld limit 5", input["start_no"].asInt());
	if(mysql_query(mysql, query) || (result = mysql_store_result(mysql)) == NULL)//查询失败
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
	}
	else if (!mysql_num_rows(result)) //结果为空
	{
		output["result"] = "error";
		output["errtype"] = CLIENT_ERROR_END;
	}
	else
	{
		output["result"] = "success";
		output["len"] = (int)mysql_num_rows(result);
		i = 1;
		while ((row = mysql_fetch_row(result)) != NULL)
		{
			sprintf(query, "%d", i++);
			args.clear();
			args.append(row[0]);
			bc_post(QUERY_URL, STATION_CODE, STATION_QUERY, &args, &res);
			if (!res["success"].isBool())
			{
				output.clear();
				output["result"] = "error";
				output["errtype"] = res["errorcode"];
				break;
			}
			output[(const char *)query]["station_no"] = (Json::Value::Int64)atoll(row[0]);
			output[(const char *)query]["user_no"] = (Json::Value::Int64)atoll(row[1]);
			output[(const char *)query]["name"] = res["message"]["name"];
			output[(const char *)query]["type"] = res["message"]["type"];
			output[(const char *)query]["address"] = res["message"]["palce"];
			output[(const char *)query]["stationPrice"] = res["message"]["money"];
			output[(const char *)query]["stationCap"] = res["message"]["capacity"];
			output[(const char *)query]["introduce"] = res["message"]["description"];
		}
	}
	
	/* 断开连接 */
	mysql_close(mysql);
	return 0;
}