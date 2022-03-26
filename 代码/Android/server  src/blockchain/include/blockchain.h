#ifndef _BLOCKCHAIN_H
#define _BLOCKCHAIN_H

#include <json/json.h>
#include <pthread.h>
#include <signal.h>
#include "blockchain_code.h"

extern pthread_mutex_t user_mutex;
extern pthread_mutex_t station_mutex;

#define HOST     "localhost"     //主机
#define USER     "blockchain"    //用户名
#define PASSWORD "blockchain123" //密码
#define DB       "blockchain"    //数据库名

#define IP "192.168.100.230"

#define TRADE_THREAD_SIG SIGRTMIN
#define TRADE_MAIN_SIG   SIGRTMAX

#define INIT_ASSET 1000//初始用户积分

#define BC_SUCCESS "success"
#define BC_ERROR   "error"

/* 用户操作 */
int bc_user_signup(Json::Value &input, Json::Value &output);//注册
int bc_user_signin(Json::Value &input, Json::Value &output);//登录
int bc_user_chgpwd(Json::Value &input, Json::Value &output);//修改密码
int bc_user_revise(Json::Value &input, Json::Value &output);//修改个人信息
int bc_user_query(Json::Value &input, Json::Value &output);//修改个人信息

/* 工位操作 */
int bc_station_apply(Json::Value &input, Json::Value &output);//申请工位
int bc_station_revoke(Json::Value &input, Json::Value &output);//撤销工位
int bc_station_rent(Json::Value &input, Json::Value &output);//租用工位
int bc_station_return(Json::Value &input, Json::Value &output);//返还工位
int bc_station_query(Json::Value &input, Json::Value &output);//查询工位

/* 服务操作 */
int bc_trade_stop(long long user_no, long long trade_no, bool cancel); //服务结束,cancel=true 非正常终止,cancel=false 正常终止
int bc_trade_query(Json::Value &input, Json::Value &output);//服务查询

#endif