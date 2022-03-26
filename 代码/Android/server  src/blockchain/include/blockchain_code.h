#ifndef _BLOCKCHAIN_CODE_H
#define _BLOCKCHAIN_CODE_H

/* ������ */
#define CLIENT_USER_SIGNUP 1//ע��
#define CLIENT_USER_SIGNIN 2//��¼
#define CLIENT_USER_CHGPWD 3//�޸�����
#define CLIENT_USER_REVISE 4//�޸ĸ�����Ϣ
#define CLIENT_USER_QUERY  5//��ѯ������Ϣ

#define CLIENT_STATION_APPLY  6//���빤λ
#define CLIENT_STATION_REVOKE 7//������λ
#define CLIENT_STATION_RENT   8//���ù�λ
#define CLIENT_STATION_RETURN 9//������λ
#define CLIENT_STATION_QUERY  10//��ѯ��λ

#define CLINET_TRADE_QUERY 11//��ѯ��λ

/* ������ */
#define CLIENT_ERROR_SAME       9001//ͬ��
#define CLIENT_ERROR_NOUSER     9002//�û������������
#define CLIENT_ERROR_ERRPWD     9003//ԭ�������
#define CLIENT_ERROR_NOSTATION  9004//��λ������
#define CLIENT_ERROR_SELF       9005//�Լ��Ĺ�λ
#define CLIENT_ERROR_WAIT       9006//�Դ˹�λ�������Ѿ�����
#define CLIENT_ERROR_BUSY       9007//��λ�Ѿ���ʹ��
#define CLIENT_ERROR_NOTRADE    9008//��ʽ��ײ�����
#define CLIENT_ERROR_FINISHED   9009//�����Ѿ�����
#define CLIENT_ERROR_END        9010//��ѯ�ѵ���β

/* ��������ͻ��˷��͵����� */
#define SERVER_TRADE_WAIT   1001//�ͱ��˺���ص����еȴ��еĽ���
#define SERVER_TRADE_BUYER  1002//���׳�����һ������ҷ�����������ҽ���
#define SERVER_TRADE_SELLER 1003//���׳����ڶ�������ҷ����ظ�������ҽ���
#define SERVER_TRADE_OK     1004//���׳��������������ȷ�ϣ�����ҽ��գ����˽��׳���
#define SERVER_TRADE_CANCEL 1005//����������ͬһ��λ������һ�����ȷ�Ϻ��������ȡ��

/* ������ */
#define SERVER_ERROR_DATABASE   9901 //���ݿ����
#define SERVER_ERROR_BLOCKCHAIN 9902 //�޷�����������

#endif