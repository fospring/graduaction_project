#ifndef _EXTRA_H
#define _EXTRA_H

#include <json/json.h>

#define INVOKE_URL "http://202.120.167.93:4000/channels/chaincodes/invoke"
#define QUERY_URL  "http://202.120.167.93:4000/channels/chaincodes/query"

/* 用户操作 */
#define USER_CODE   "register"
#define USER_SIGNUP "regist" //注册
#define USER_SIGNIN "login"  //登录
#define USER_CHGPWD "changePwd" //修改密码
#define USER_UPDATE "update"
#define USER_QUERY  "query"

/* 工位操作 */
#define STATION_CODE  "publicService"
#define STATION_APPLY "addService"//申请工位
#define STATION_QUERY "query"//查询工位

void bc_post(const char *url, const char *chaincode, const char *function, const Json::Value *args, Json::Value *res);
int bc_send(int sock, const void *buf, int size);
int bc_recv(int sock, void *buf, int size);

#endif