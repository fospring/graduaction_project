#include "blockchain.h"
#include "extra.h"

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <mysql.h>

int bc_station_revoke(Json::Value &input, Json::Value &output)
{
	MYSQL     *mysql;
	char       query[512];

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

	/* 撤销工位 */
	sprintf(query, "delete from station where station_no=%d and station_user_no=%d", atoi(input["station_no"].asString().c_str()), atoi(input["user_no"].asString().c_str()));
	if(mysql_query(mysql, query))//插入失败
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
	}
	else
		output["result"] = "success";

	/* 关闭连接 */
	mysql_close(mysql);
	return 0;
}