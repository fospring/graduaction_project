#include "extra.h"
#include <time.h>
#include <errno.h>
#include <sys/socket.h>
#include <sys/select.h>

int bc_recv(int sock, void *buf, int size)
{
	fd_set         rds;
	struct timeval timeout;
	int            ret, recvnum;

	recvnum = 0;
	while (1)
	{
		FD_ZERO(&rds); //清空读集
		FD_SET(sock, &rds);//设置读集
		timeout.tv_sec  = 5;//设置超时时间为5s
		timeout.tv_usec = 0;

		ret = select(sock + 1, &rds, NULL, NULL, &timeout);

		if (ret < 0 && errno != EINTR && errno != EAGAIN && errno != EWOULDBLOCK) //select失败
			return -1;
		else if (ret > 0 && FD_ISSET(sock, &rds))//成功
		{
			ret = recv(sock, (char *)buf + recvnum, size - recvnum, 0);//接收数据

			if (ret <= 0 && errno != EINTR && errno != EAGAIN && errno != EWOULDBLOCK) //断线
				return -1;
			else //接收成功
			{
				recvnum += ret;
				if (recvnum >= size)
					break;
			}
		}
	}

	return size;
}