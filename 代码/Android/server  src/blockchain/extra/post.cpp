#include "extra.h"
#include "blockchain_code.h"
#include <curl/curl.h>
#include <string.h>
size_t func(void *ptr, size_t size, size_t nmemb, void *stream)
{
	Json::CharReaderBuilder builder;
	Json::CharReader* reader = builder.newCharReader();
	Json::Value *v = (Json::Value *)stream;
	JSONCPP_STRING errs;
	if (!reader->parse((const char *)ptr, (const char *)ptr + size * nmemb, v, &errs)) //从jsonStr中读取数据到jsonRoot
	{
		v->clear();
		(*v)["success"] = false;
		(*v)["errorcode"] = SERVER_ERROR_BLOCKCHAIN;
	}
	return size * nmemb;
}

void bc_post(const char *url, const char *chaincode, const char *function, const Json::Value *args, Json::Value *res)
{
	//easy handler的句柄
	CURL* curl = NULL;
	CURLcode curl_res = CURLE_OK;

	//HTTP报文头
	struct curl_slist* headers = NULL;
	headers = curl_slist_append(headers, "authorization: Bearer");
	headers = curl_slist_append(headers, "content-type: application/json; charset=utf-8");

	//HTTP数据
	Json::Value JsonValue;
	std::string data;
	JsonValue["username"]    = "Jim";
	JsonValue["orgname"]     = "org1";
	JsonValue["channelName"] = "mychannel";
	JsonValue["peers"].append("localhost:7051");
	JsonValue["chaincodeName"] = chaincode;
	JsonValue["functionName"] = function;
	JsonValue["args"] = *args;
	data = JsonValue.toStyledString();
	
	printf("post:\n%s\n", data.c_str());

	curl = curl_easy_init();  
	if (curl) {
		curl_easy_setopt(curl, CURLOPT_URL, url);                //设置url地址
		curl_easy_setopt(curl, CURLOPT_POST, 1);                 // post请求
		curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);     //设置HTTP头
		curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data.c_str());//设置post内容
		curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, func);
		curl_easy_setopt(curl, CURLOPT_WRITEDATA, res);
		curl_easy_setopt(curl, CURLOPT_TIMEOUT, 300);

		curl_res = curl_easy_perform(curl);
		if (curl_res != CURLE_OK)
		{
			(*res)["success"] = false;
			(*res)["errorcode"] = SERVER_ERROR_BLOCKCHAIN;
		}

		curl_easy_cleanup(curl);
	}

	curl_slist_free_all(headers);
}