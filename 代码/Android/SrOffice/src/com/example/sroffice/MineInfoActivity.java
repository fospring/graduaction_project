package com.example.sroffice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MineInfoActivity extends Activity {
	/*布局变量*/
	private TextView TVUserName;
	private MyApp myApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mine_info);
		/*获取布局变量*/
		TVUserName = (TextView)findViewById(R.id.TVUserName);
		/*初始化*/
		myApp = (MyApp)getApplication();
		if(!myApp.getIsSigned()) {		//用户未登录
			new AlertDialog.Builder(this).setMessage("请登录").setPositiveButton("确定", new loginListener()).show();
		}
		TVUserName.setText(myApp.getUserName());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mine_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public class loginListener implements OnClickListener{
		public void onClick(DialogInterface dialog, int which) {
			Intent loginIntent = new Intent(MineInfoActivity.this, LoginActivity.class);
			startActivity(loginIntent);
		}
	}
	
	/*用户信息按钮点击事件响应*/
	public void userInfoButtonClick(View v) {
		if(myApp.getIsSigned()) {				//已登录
			Intent userInfoIntent = new Intent(MineInfoActivity.this, UserInfoActivity.class);
			startActivity(userInfoIntent);
		}else {
			new AlertDialog.Builder(this).setMessage("请登录").setPositiveButton("确定", new loginListener()).show();
		}
	}
	
	/*租赁信息按钮点击事件响应*/
	public void rentInfoButtonClick(View v) {
		if(myApp.getIsSigned()) {				//已登录
			Intent rentInfoIntent = new Intent(MineInfoActivity.this, RentInfoActivity.class);
			startActivity(rentInfoIntent);
		}else {		//未登录
			new AlertDialog.Builder(this).setMessage("请登录").setPositiveButton("确定", new loginListener()).show();
		}
	}
	
	/*信用信息按钮点击事件响应*/
	public void creditInfoButtonClick(View v) {
		if(myApp.getIsSigned()) {				//已登录
			Intent creditInfoIntent = new Intent(MineInfoActivity.this, CreditInfoActivity.class);
			startActivity(creditInfoIntent);
		}else {		//未登录
			new AlertDialog.Builder(this).setMessage("请登录").setPositiveButton("确定", new loginListener()).show();
		}
	}
	
	public void applyStationButtonClick(View v) {
		if(myApp.getIsSigned()) {				//已登录
			Intent stationInfoIntent = new Intent(MineInfoActivity.this, StationInfoActivity.class);
			startActivity(stationInfoIntent);
		}else {		//未登录
			new AlertDialog.Builder(this).setMessage("请登录").setPositiveButton("确定", new loginListener()).show();
		}
	}
	
	/*菜单栏租赁按钮点击响应事件*/
	public void mainButtonClick(View v)
	{
		Intent signinIntent = new Intent(MineInfoActivity.this, MainActivity.class);
		startActivity(signinIntent);
	}
	
	/*菜单栏购物按钮点击响应事件*/
	public void shopButtonClick(View v)
	{
		Intent signinIntent = new Intent(MineInfoActivity.this, ShopActivity.class);
		startActivity(signinIntent);
	}
	
	/*菜单栏论坛按钮点击响应事件*/
	public void forumButtonClick(View v)
	{
		
	}
	
	/*菜单栏其他按钮点击响应事件*/
	public void elseButtonClick(View v)
	{
		
	}
	
	/*菜单栏租赁按钮点击响应事件*/
	public void mineButtonClick(View v)
	{
		Intent signinIntent = new Intent(MineInfoActivity.this, MineInfoActivity.class);
		startActivity(signinIntent);
	}
}
