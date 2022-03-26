package com.example.sroffice;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class UserInfoActivity extends Activity {
	/*布局变量*/
	private EditText ETUserName;
	private EditText ETUserPsd;
	private EditText ETUserPhone;
	private EditText ETUserEmail;
	private EditText ETUserAddr;
	/*全局变量*/
	private MyApp myApp;
	private String TAG = "userInfo";
	private Handler reviseHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0x00) {
				Bundle bundle = msg.getData();
				try {
					JSONObject data = new JSONObject(bundle.get("msg").toString());
					String result = data.getString("result");
					if(result == "success") {
						showMessage("修改成功");
					}
					else {
						
					}
				}catch(JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};
	private Handler requestHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0x00) {
				Bundle bundle = msg.getData();
				try {
					JSONObject data = new JSONObject(bundle.get("msg").toString());
					String result = data.getString("result");
					if(result == "success") {
						ETUserName.setText(data.getString("user"));
						ETUserPsd.setText(data.getString("passwd"));
						ETUserPhone.setText(data.getString("phone"));
						ETUserEmail.setText(data.getString("email"));
						ETUserAddr.setText(data.getString("address"));
					}
					else {
						
					}
				}catch(JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info);
		/*布局变量初始化*/
		ETUserName = (EditText)findViewById(R.id.ETUserName);
		ETUserPsd = (EditText)findViewById(R.id.ETUserPsd);
		ETUserEmail = (EditText)findViewById(R.id.ETUserEmail);
		ETUserPhone = (EditText)findViewById(R.id.ETUserPhone);
		ETUserAddr = (EditText)findViewById(R.id.ETUserAddr);
		/*全局变量初始化*/
		myApp = (MyApp)getApplication();
		/*界面初始化*/
		getUserInfo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_info, menu);
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
	
	/*显示提示框*/
	private void showMessage(String str) {
		new AlertDialog.Builder(this).setMessage(str).setPositiveButton("确定", null).show();
	}
	
	/*获取用户信息*/
	private void getUserInfo() {
		/*构造json字符串*/
		JSONObject jo = new JSONObject();
		try {
			jo.put("operate", "requestUserInfo");
			jo.put("user_no", myApp.getUserNo());
		}catch(JSONException e) {
			Log.i(TAG, "Json Err");
			return ;
		}
		/*socketThread*/
		SocketThread sktThread = new SocketThread(jo.toString(), requestHandler);
		sktThread.start();
	}
	
	/*修改用户信息*/
	private void reviseClick(View v) {
		String userName = ETUserName.getText().toString();
		String userPsd= ETUserPsd.getText().toString();
		String userPhone = ETUserPhone.getText().toString();
		String userEmail = ETUserEmail.getText().toString();
		String userAddr = ETUserAddr.getText().toString();
		/*合法性判断*/
		if(TextUtils.isEmpty(userName))
		{
			showMessage("用户名为空，请重试！");
			return;
		}
		if(TextUtils.isEmpty(userPsd))
		{
			showMessage("密码为空，请重试！");
			return;
		}
		if(TextUtils.isEmpty(userPhone))
		{
			showMessage("联系电话为空，请重试！");
			return;
		}
		if(!isMobile(userPhone))		//联系电话符合格式
		{
			showMessage("联系电话格式错误，请重试！");
			return;
		}
		/*向服务器发送数据*/
		JSONObject jo = new JSONObject();
		try {
			jo.put("operate", "revise");
			jo.put("user_no", myApp.getUserNo());
			jo.put("user", userName);
			jo.put("passwd", userPsd);
			jo.put("phone", userPhone);
		}catch(JSONException e) {
			Log.i(TAG, "Json Err");
			return;
		}
		/*socketThread*/
		SocketThread sktThread = new SocketThread(jo.toString(), reviseHandler);
		sktThread.start();
	}
	
	private boolean isMobile(String number) {
	    /*
	    移动：134、135、136、137、138、139、150、151、152、157(TD)、158、159、178(新)、182、184、187、188
	    联通：130、131、132、152、155、156、185、186
	    电信：133、153、170、173、177、180、181、189、（1349卫通）
	    总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
	    */
	        String num = "[1][34578]\\d{9}";//"[1]"代表第1位为数字1，"[34578]"代表第二位可以为3、4、5、7、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
	        if (TextUtils.isEmpty(number)) {
	            return false;
	        } else {
	            //matches():字符串是否在给定的正则表达式匹配
	            return number.matches(num);
	        }
	}
}
