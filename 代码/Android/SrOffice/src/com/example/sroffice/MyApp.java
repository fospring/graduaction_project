package com.example.sroffice;

import android.app.Application;

public class MyApp extends Application{
	
	private boolean isSigned;
	private long userNo;
	private String userName;
	private static int bufSize = 255;
	
	public boolean getIsSigned() {
		return isSigned;
	}
	
	public void setIsSigned(boolean value) {
		isSigned = value;
	}
	
	public int getbufSize() {
		return bufSize;
	}
	
	public long getUserNo() {
		return userNo;
	}
	
	public void setUserNo(long value) {
		userNo = value;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String value) {
		userName = value;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		/*全局变量初始化*/
		isSigned = false;
	}
}