package com.example.sroffice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SocketThread extends Thread {
	private String serverIP = "172.16.0.12";
	private int serverPort = 50000;
	private int timeOut = 3000;
	private Socket sock;
	private String msgStr;
	private Handler myHandler;
	private static String TAG = "SOCKThread"; 
	
	public SocketThread(String str, Handler handler) {
		msgStr = str;
		myHandler = handler;
	}
	
	@Override
	public void run() {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.clear();
		try {
			//连接服务器，并设置连接超时为5秒
			sock = new Socket();
			sock.connect(new InetSocketAddress(serverIP, serverPort), timeOut);
			//向服务器发送消息
			OutputStream os = sock.getOutputStream();
			byte[] buf = msgStr.getBytes();
			//os.write(buf.length);
			Log.i(TAG, buf.length+"");
			Log.i(TAG, msgStr);
			byte[] b = new byte[4];
			b[0] =  (byte) ((buf.length>>24) & 0xFF);  
		    b[1] =  (byte) ((buf.length>>16) & 0xFF);  
		    b[2] =  (byte) ((buf.length>>8) & 0xFF);    
		    b[3] =  (byte) (buf.length & 0xFF); 
		    //System.arraycopy(data1,0,data3,0,data1.length);
		    os.write(b);
			os.write(buf);
			os.flush();
			//读取服务器的返回结果
			BufferedReader bff = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			/*数据长度*/
			b[0] = (byte)bff.read();
			b[1] = (byte)bff.read();
			b[2] = (byte)bff.read();
			b[3] = (byte)bff.read();
			int dataLen = (b[0] & 0x000000ff) << 24 | (b[1] & 0x000000ff) << 16 | (b[2] & 0x000000ff) << 8 | (b[3] & 0x000000ff);
			Log.i(TAG, "datalen:" + dataLen+" "+b[0]+" "+b[1]+" "+b[2]+" "+b[3]);
			/*数据*/
			char[] bffer = new char[2048];
			int len = 0;
			while (len < dataLen) {
			    len += bff.read(bffer, len, dataLen - len);
			}
			Log.i(TAG, new String(bffer));
			//发送消息，UI进程处理
			bundle.putString("msg", new String(bffer));
			msg.what = 0x00;
			msg.setData(bundle);
			myHandler.sendMessage(msg);
			//关闭各种输入输出流
			bff.close();
			os.close();
			sock.close();
		}catch (SocketTimeoutException aa) {  
            //连接超时 在UI界面显示消息  
            bundle.putString("msg", "服务器连接失败！请检查网络是否打开"); 
            msg.what = 0x01;
            msg.setData(bundle);  
            //发送消息 修改UI线程中的组件  
            myHandler.sendMessage(msg);  
        } catch (IOException e) {  
        	Log.i(TAG, "IO");
            e.printStackTrace();  
        }  
	}
}

