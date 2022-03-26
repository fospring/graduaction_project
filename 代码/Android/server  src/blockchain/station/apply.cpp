#include "blockchain.h"
#include "extra.h"

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <mysql.h>

int bc_station_apply(Json::Value &input, Json::Value &output)
{
	MYSQL     *mysql;
	MYSQL_RES *result;
	MYSQL_ROW  row;
	char       query[512], temp[30];

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

	/* 工位 */
	sprintf(query, "insert into station values(0,%d,\"free\")", input["user_no"].asInt64());

	/* 进入临界区 */
	pthread_mutex_lock(&station_mutex);
	
	/* 将结果插入数据库 */
	if(mysql_query(mysql, query))//插入失败
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
		mysql_close(mysql);
		pthread_mutex_unlock(&station_mutex);
		return 0; 
	}

	/* 插入成功，查询用户编号 */
	sprintf(query, "select station_no from station where station_user_no=%d order by station_no desc", input["user_no"].asInt64());
	if (mysql_query(mysql, query) || (result = mysql_store_result(mysql)) == NULL)//查询失败
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
		mysql_close(mysql);
		pthread_mutex_unlock(&station_mutex);
		return 0; 
	}
	row = mysql_fetch_row(result);
	
	/* 离开临界区 */
	pthread_mutex_unlock(&station_mutex);

	/* 数据上链 */
	sprintf(temp, "%lld", input["user_no"].asInt64());
	args.append(row[0]);//服务/工位ID
	args.append(temp);  //发布者ID
	args.append("1");   //类型（1为工位，2为其他服务）
	//类型2（服务名称）
	if (!input["name"].isNull())
		args.append(input["name"]);
	else
		args.append("");
	//价格（float64）
	if (!input["price"].isNull())
	{
		sprintf(temp, "%.2f", input["price"].asFloat());
		args.append(temp);
	}
	else
		args.append("");
	//描述字段
	if (!input["introduce"].isNull())
		args.append(input["introduce"]);
	else
		args.append("");
	//地点
	if (!input["address"].isNull())
		args.append(input["address"]);
	else
		args.append("");
	//工位容量
	if (!input["size"].isNull())
	{
		sprintf(temp, "%d", input["size"].asInt());
		args.append(temp);
	}
	else
		args.append("");
	bc_post(INVOKE_URL, STATION_CODE, STATION_APPLY, &args, &res);

	if (res["success"].asBool())
	{
		output["result"] = "success";
		output["station_no"] = (Json::Value::Int64)atoll(row[0]);
	}
	else
	{
		output["result"] = "error";
		output["errtype"] = res["errorcode"];
	}

	/* 关闭连接 */
	mysql_close(mysql);
	return 0;
}