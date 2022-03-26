#include "blockchain.h"
#include "extra.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>

#include <errno.h>
#include <arpa/inet.h>
#include <sys/stat.h>
#include <sys/signal.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>

pthread_mutex_t user_mutex;
pthread_mutex_t station_mutex;

void init_daemon(void);//成为守护进程

int main(int argc, char **argv)
{
	int                 listenfd, connfd, ret, pid;
	struct sockaddr_in  servaddr;
	fd_set              readfds;
	struct timeval      timeout;
	pthread_mutexattr_t mutexattr;//互斥锁属性
	
	int keepAlive = 1; 
	int keepIdle  = 15; //如该连接在该时间内没有任何数据往来,则进行探测,单位为s
	int keepIntvl = 3;  //探测时发包的时间间隔,单位为s
	int keepCnt   = 5;  //探测尝试的次数

//	init_daemon();
	signal(SIGCHLD,SIG_IGN); //防止子进程成为僵尸进程

	/* 初始化锁 */
	pthread_mutexattr_init(&mutexattr);//初始化互斥锁属性    
	pthread_mutexattr_setpshared(&mutexattr, PTHREAD_PROCESS_SHARED);//设置互斥锁可以在多个进程访问
	pthread_mutex_init(&user_mutex, &mutexattr);
	pthread_mutex_init(&station_mutex, &mutexattr);

	/* 创建套接字 */
	if ((listenfd = (AF_INET, SOCK_STREAM, 0)) == -1)
		return -1;
	
	memset(&servaddr, 0, sizeof(servaddr));//初始化servaddr
	servaddr.sin_family      = AF_INET;          //协议族
	servaddr.sin_addr.s_addr = htonl(INADDR_ANY);//INADDR_ANY为0.0.0.0，即所有地址
	servaddr.sin_port        = htons(50000);     //绑定TCP端口

	// 置端口重用
	ret = 1;
	setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, (void *)&ret, sizeof(ret));

	// 打开keepalive机制
	setsockopt(listenfd, SOL_SOCKET, SO_KEEPALIVE, (void *)&keepAlive, sizeof(keepAlive));
	setsockopt(listenfd, SOL_TCP, TCP_KEEPIDLE,  (void *)&keepIdle,  sizeof(keepIdle));
	setsockopt(listenfd, SOL_TCP, TCP_KEEPINTVL, (void *)&keepIntvl, sizeof(keepIntvl));
	setsockopt(listenfd, SOL_TCP, TCP_KEEPCNT,   (void *)&keepCnt,   sizeof(keepCnt));

	if (bind(listenfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) == -1)// 将套接字与本地地址绑定
		return -1;
	if ((ret = fcntl(listenfd, F_GETFL)) < 0)// 取套接字属性
		return -1;
	if (fcntl(listenfd, F_SETFL, ret|O_NONBLOCK) < 0)// 设置套接字为非阻塞
		return -1;
	if (listen(listenfd, 10) == -1)// 监听端口
		return -1;

	socklen_t len = sizeof(servaddr);
	while (1)
	{
		FD_ZERO(&readfds); //将文件描述符清空
		FD_SET(listenfd, &readfds); //设置文件描述符
		timeout.tv_sec  = 5;//超时时间设为5s
		timeout.tv_usec = 0;
		
		ret = select(listenfd+1, &readfds, NULL, NULL, &timeout);
		
		if (ret < 0 && errno != EINTR && errno != EWOULDBLOCK && errno != EAGAIN) //select出错
			break;
		else if (ret > 0 && FD_ISSET(listenfd, &readfds)) //可读
		{
			if ((connfd = accept(listenfd, (struct sockaddr *)&servaddr, &len)) < 0) //连接客户端
				continue;
printf("accept connect: %s:%d\n", inet_ntoa(servaddr.sin_addr), ntohs(servaddr.sin_port));
			if ((pid = fork()) > 0) //父进程
				close(connfd); //关闭接收的连接
			else if (!pid) //子进程
			{
				int         len;
				char        buf[1024];
				Json::Value input, output;
				std::string data;

				Json::CharReaderBuilder builder;
				Json::CharReader* reader = builder.newCharReader();
				JSONCPP_STRING errs;

				close(listenfd); //关闭监听的socket*/

				while (1)
				{
					FD_ZERO(&readfds); //清空读集
					FD_SET(connfd, &readfds);//设置读集
					timeout.tv_sec  = 5;//设置超时时间为5s
					timeout.tv_usec = 0;

					ret = select(connfd + 1, &readfds, NULL, NULL, &timeout);

					if (ret < 0 && errno != EINTR && errno != EAGAIN && errno != EWOULDBLOCK) //select失败
						return -1;
					else if (ret > 0 && FD_ISSET(connfd, &readfds))//成功
					{
						ret = recv(connfd, (char *)&len, sizeof(len), 0);//接收数据

						if (ret <= 0 && errno != EINTR && errno != EAGAIN && errno != EWOULDBLOCK) //断线
							return -1;

						len = ntohl(len);
						bc_recv(connfd, buf, len);

						input.clear();
						if (!reader->parse(buf, buf + len, &input, &errs))
							continue;
						printf("input:\n%s\n", input.toStyledString().c_str());

						output.clear();
						switch(input["operate"].asInt())
						{
							/* 用户操作 */
							case CLIENT_USER_SIGNUP: //注册
								bc_user_signup(input, output);
								break;

							case CLIENT_USER_SIGNIN:
								bc_user_signin(input, output); //登录
								break;

							case CLIENT_USER_CHGPWD: //修改密码
								bc_user_chgpwd(input, output);
								break;

							case CLIENT_USER_REVISE: //修改个人信息
								bc_user_revise(input, output);
								break;

							case CLIENT_USER_QUERY: //查询个人信息
								bc_user_query(input, output);
								break;

							/* 工位操作 */
							case CLIENT_STATION_APPLY://申请工位
								bc_station_apply(input, output);
								break;

							case CLIENT_STATION_REVOKE://撤销工位
								bc_station_revoke(input, output);
								break;

							case CLIENT_STATION_RENT://租用工位
								bc_station_rent(input, output);
								break;

							case CLIENT_STATION_RETURN://返还工位
								bc_station_return(input, output);
								if (!strcmp(output["result"].asString().c_str(), "success")) //成功返还工位
									bc_trade_stop(input["user_no"].asInt64(), input["trade_no"].asInt64(), true);
								break;

							case CLIENT_STATION_QUERY:
								bc_station_query(input, output);
								break;//查询工位

							case CLINET_TRADE_QUERY://查询交易
								bc_trade_query(input, output);
								break;

							default: break;
						}
						printf("output\n%s\n", output.toStyledString().c_str());

						if (!output.isNull())
						{
							data = output.toStyledString();
							len = htonl(data.length());
							bc_send(connfd, &len, sizeof(len));
							bc_send(connfd, data.c_str(), ntohl(len));
						}
					}
				}
				close(connfd);

				return 0;//子进程退出
			}
		}
	}

	close(listenfd);
	pthread_mutexattr_destroy(&mutexattr);//销毁互斥锁属性
	pthread_mutex_destroy(&user_mutex);
	pthread_mutex_destroy(&station_mutex);
	return 0;
}

void init_daemon(void) 
{ 
	int pid, i; 

	if ((pid = fork()) > 0) 
		exit(0);//是父进程，结束父进程 
	else if (pid < 0) 
		exit(-1);//fork失败，退出 
	//是第一子进程，后台继续执行 

	setsid();//第一子进程成为新的会话组长和进程组长 

	//与控制终端分离 
	if ((pid = fork()) > 0) 
		exit(0);//是第一子进程，结束第一子进程 
	else if (pid < 0) 
		exit(-1);//fork失败，退出 
	//是第二子进程，继续 
	//第二子进程不再是会话组长 

	for (i = 0;i < getdtablesize(); i++)//关闭打开的文件描述符 
		close(i);  
	umask(0);//重设文件创建掩模 
//	signal(SIGCHLD,SIG_IGN); //防止子进程成为僵尸进程
	return; 
}