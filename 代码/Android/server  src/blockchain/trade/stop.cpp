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

	/* ��ʼ�� mysql ������ʧ�ܷ���NULL */
	if ((mysql = mysql_init(NULL))==NULL)
		return 0;

	/* �������ݿ⣬ʧ�ܷ���NULL
	   1��mysqldû����
	   2��û��ָ�����Ƶ����ݿ���� */
	while (mysql_real_connect(mysql, HOST, USER, PASSWORD, DB, 0, NULL, 0)==NULL)
		sleep(1);

	/* �����ַ���������������ַ����룬��ʹ/etc/my.cnf������Ҳ���� */
	mysql_set_character_set(mysql, "utf8");

	//��ѯ��������
	time(&timep); //��ȡ��ǰʱ��
	pos = strftime(query, sizeof(query), "update trade set trade_status='finish',trade_stop=\"%Y-%m-%d %H:%M:%S\"", localtime(&timep));
	sprintf(query + pos, " where trade_no=%lld", trade_no);
	mysql_query(mysql, query);
	/* ���ҹ�λ��� */
	sprintf(query, "select trade_station_no,trade_buyer_no,trade_seller_no from trade where trade_no=%lld", trade_no);
	mysql_query(mysql, query);
	result = mysql_store_result(mysql);
	row = mysql_fetch_row(result);
	/* ��λ״̬��Ϊ���� */
	sprintf(query, "update station set station_status='free' where station_no=%s", row[0]);
	mysql_query(mysql, query);
	
	if (cancel)//���ΥԼ
	{
		//�Ȳ����û�����Ϣ
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
	else //ʱ�䵽��������
	{
		pos = 0;
		//���
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
			time(&timep); //��ȡ��ǰʱ��
			pos += strftime(query + pos, sizeof(query) - pos, "\"%Y-%m-%d %H:%M:%S\"),", localtime(&timep));
		}
		
		//����
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
			time(&timep); //��ȡ��ǰʱ��
			strftime(query + pos, sizeof(query) - pos, "\"%Y-%m-%d %H:%M:%S\");", localtime(&timep));
			mysql_query(mysql, query);
		}
	}
	
	/* �Ͽ����� */
	mysql_close(mysql);
	
	return 0;
}