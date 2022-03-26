#include "blockchain.h"

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <mysql.h>

int bc_trade_query(Json::Value &input, Json::Value &output)
{
	MYSQL     *mysql;
	MYSQL_RES *result;
	MYSQL_ROW  row;
	char       query[512];
	int        pos, i;

	/* ��ʼ�� mysql ������ʧ�ܷ���NULL */
	if ((mysql = mysql_init(NULL))==NULL)
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
		return 0;
	}

	/* �������ݿ⣬ʧ�ܷ���NULL
	   1��mysqldû����
	   2��û��ָ�����Ƶ����ݿ���� */
	while (mysql_real_connect(mysql, HOST, USER, PASSWORD, DB, 0, NULL, 0)==NULL)
		sleep(1);

	/* �����ַ���������������ַ����룬��ʹ/etc/my.cnf������Ҳ���� */
	mysql_set_character_set(mysql, "utf8");

	/* ��ѯ���� */
	sprintf(query, "select * from trade where trade_buyer_no=%lld or trade_seller_no=%lld", input["user_no"].asInt64(), input["user_no"].asInt64());
	if(mysql_query(mysql, query) || (result = mysql_store_result(mysql)) == NULL)//��ѯʧ��
	{
		output["result"] = "error";
		output["errtype"] = SERVER_ERROR_DATABASE;
	}
	else
	{
		output["result"] = "success";
		output["len"] = (int)mysql_num_rows(result);
		i = 1;
		while ((row = mysql_fetch_row(result)) != NULL)
		{
			sprintf(query, "%d", i++);
			output[(const char *)query]["trade_no"] = (Json::Value::Int64)atoll(row[0]);
			output[(const char *)query]["station_no"] = (Json::Value::Int64)atoll(row[1]);
			output[(const char *)query]["time"] = row[6];
			if (input["user_no"].asInt64() == atoll(row[2]))
				output[(const char *)query]["type"] = "buyer";
			else
				output[(const char *)query]["type"] = "seller";
			if (!strcmp(row[4], "finish"))
				output[(const char *)query]["finish"] = true;
			else
				output[(const char *)query]["finish"] = false;
		}
	}
	
	/* �Ͽ����� */
	mysql_close(mysql);
	return 0;
}