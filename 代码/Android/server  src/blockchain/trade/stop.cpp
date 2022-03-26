#include "blockchain.h"
#include "extra.h"

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <mysql.h>
#include <arpa/inet.h>

int bc_trade_stop(long long user_no, long long trade_no, bool cancel)
{
	MYSQL     *mysql;
	MYSQL_RES *result;
	MYSQL_ROW  row;
	char       query[512];
	int        pos, balance, crePoint, funcPoint;
	time_t     timep;

	Json::Value args, res;

	/* 初始化 mysql 变量，失败返回NULL */
	if ((mysql = mysql_init(NULL))==NULL)
		return 0;

	/* 连接数据库，失败返回NULL
	   1、mysqld没运行
	   2、没有指定名称的数据库存在 */
	while (mysql_real_connect(mysql, HOST, USER, PASSWORD, DB, 0, NULL, 0)==NULL)
		sleep(1);

	/* 设置字符集，否则读出的字符乱码，即使/etc/my.cnf中设置也不行 */
	mysql_set_character_set(mysql, "utf8");

	//查询现有数据
	time(&timep); //获取当前时间
	pos = strftime(query, sizeof(query), "update trade set trade_status='finish',trade_stop=\"%Y-%m-%d %H:%M:%S\"", localtime(&timep));
	sprintf(query + pos, " where trade_no=%lld", trade_no);
	mysql_query(mysql, query);
	/* 查找工位编号 */
	sprintf(query, "select trade_station_no,trade_buyer_no,trade_seller_no from trade where trade_no=%lld", trade_no);
	mysql_query(mysql, query);
	result = mysql_store_result(mysql);
	row = mysql_fetch_row(result);
	/* 工位状态设为空闲 */
	sprintf(query, "update station set station_status='free' where station_no=%s", row[0]);
	mysql_query(mysql, query);
	
	if (cancel)//如果违约
	{
		//先查找用户的信息
		sprintf(query, "%lld", user_no);
		args.append(query);
		bc_post(QUERY_URL, USER_CODE, USER_QUERY, &args, &res);
		printf("%s\n", res.toStyledString().c_str());
		balance = res["message"]["balance"].asInt();
		crePoint = res["message"]["crePoint"].asInt() - 10;
		funcPoint = res["message"]["funcPoint"].asInt();
		sprintf(query, "%d", balance);
		args.append(query);
		sprintf(query, "%d", crePoint);
		args.append(query);
		sprintf(query, "%d", funcPoint);
		args.append(query);
		res.clear();
		bc_post(INVOKE_URL, USER_CODE, USER_UPDATE, &args, &res);
		printf("%s\n", res.toStyledString().c_str());
		if (res["success"].asBool())
		{
			sprintf(query, "insert into message values(\"%s\",%lld,%d)", res["message"].asString().c_str(), user_no, crePoint);
			mysql_query(mysql, query);
		}
	}
	else //时间到结束交易
	{
		pos = 0;
		//买家
		args.append(row[1]);
		bc_post(QUERY_URL, USER_CODE, USER_QUERY, &args, &res);
		balance = res["message"]["balance"].asInt();
		crePoint = res["message"]["crePoint"].asInt() + 3;
		funcPoint = res["message"]["funcPoint"].asInt();
		sprintf(query, "%d", balance);
		args.append(query);
		sprintf(query, "%d", crePoint);
		args.append(query);
		sprintf(query, "%d", funcPoint);
		args.append(query);
		res.clear();
		bc_post(INVOKE_URL, USER_CODE, USER_UPDATE, &args, &res);
		printf("%s\n", res.toStyledString().c_str());
		if (res["success"].asBool())
		{
			pos = sprintf(query, "insert into message values(\"%s\",%s,%d,", res["message"].asString().c_str(), row[1], crePoint);
			time(&timep); //获取当前时间
			pos += strftime(query + pos, sizeof(query) - pos, "\"%Y-%m-%d %H:%M:%S\"),", localtime(&timep));
		}
		
		//卖家
		args.clear();
		args.append(row[2]);
		bc_post(QUERY_URL, USER_CODE, USER_QUERY, &args, &res);
		balance = res["message"]["balance"].asInt();
		crePoint = res["message"]["crePoint"].asInt() + 3;
		funcPoint = res["message"]["funcPoint"].asInt();
		sprintf(query, "%d", balance);
		args.append(query);
		sprintf(query, "%d", crePoint);
		args.append(query);
		sprintf(query, "%d", funcPoint);
		args.append(query);
		res.clear();
		bc_post(INVOKE_URL, USER_CODE, USER_UPDATE, &args, &res);
		printf("%s\n", res.toStyledString().c_str());
		if (res["success"].asBool())
		{
			if (!pos)
				pos = sprintf(query, "insert into message values(\"%s\",%s,%d,", res["message"].asString().c_str(), row[2], crePoint);
			else
				pos += sprintf(query + pos, "(\"%s\",%s,%d,", res["message"].asString().c_str(), row[2], crePoint);
			time(&timep); //获取当前时间
			strftime(query + pos, sizeof(query) - pos, "\"%Y-%m-%d %H:%M:%S\");", localtime(&timep));
			mysql_query(mysql, query);
		}
	}
	
	/* 断开连接 */
	mysql_close(mysql);
	
	return 0;
}