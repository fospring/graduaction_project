#include "extra.h"
#include <time.h>
#include <errno.h>
#include <sys/socket.h>
#include <sys/select.h>

int bc_send(int sock, const void *buf, int size)
{
	fd_set         wds;
	struct timeval timeout;
	int            ret, sendnum;

	sendnum = 0;
	while (1)
	{
		FD_ZERO(&wds); //清空写集
		FD_SET(sock, &wds);//设置写集
		timeout.tv_sec  = 5;//设置超时时间为5s
		timeout.tv_usec = 0;

		ret = select(sock + 1, NULL, &wds, NULL, &timeout);

		if (ret < 0 && errno != EINTR && errno != EAGAIN && errno != EWOULDBLOCK) //select失败
			return -1;
		else if (ret > 0 && FD_ISSET(sock, &wds))//成功
		{
			ret = send(sock, (char *)buf + sendnum, size - sendnum, 0);//发送数据

			if (ret <= 0 && errno != EINTR && errno != EAGAIN && errno != EWOULDBLOCK) //断线
				return -1;
			else //发送成功
			{
				sendnum += ret;
				if (sendnum >= size)
					break;
			}
		}
	}

	return size;
}