package com.example.sroffice;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sroffice.SigninActivity.signinListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginActivity extends Activity {
	/*布局变量*/
	private EditText userIdET;
	private EditText userPsdET;
	private CheckBox psdCB;
	/*全局变量*/
	private MyApp myApp;
	private long userId;
	private String TAG = "Login";
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0x00) {
				Bundle bundle = msg.getData();
				try {
					JSONObject data = new JSONObject(bundle.get("msg").toString());
					String result = data.getString("result");
					Log.i(TAG, result);
					if(result.equals("success")) {
						Log.i(TAG, "登录成功");
						myApp.setIsSigned(true);
						Log.i(TAG, data.getLong("user_no")+"");
						myApp.setUserNo(userId);
						Log.i(TAG, data.getString("user"));
						myApp.setUserName(data.getString("user"));
						Log.i(TAG, "登录成功222");
						showMessageNew("登录成功");
					}
					else {
						int errType = data.getInt("errType");
						Log.i(TAG, errType+"");
						if(errType == 9002) {
							showMessage("用户名或密码错误");
						}
						else if(errType == 9901) {
							showMessage("数据库错误！");
						}else if(errType == 9902) {
							showMessage("无法连接区块链");
						}
					}
				}catch(JSONException e) {
					e.printStackTrace();
				}
			}
			else {
				showMessage("连接失败！");
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		/*初始化*/
		userIdET = (EditText) findViewById(R.id.ETUserId);
		userPsdET = (EditText) findViewById(R.id.ETUserPsd);
		psdCB = (CheckBox) findViewById(R.id.CBShowPsd);
		/*显示密码响应事件*/
		psdCB.setOnClickListener(new psdCBClickListener());
		/*初始化全局变量*/
		myApp = (MyApp)getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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
	
	/*显示密码CheckBox监听器*/
	private class psdCBClickListener implements OnClickListener {
		public void onClick(View v)
		{
			if (psdCB.isChecked()) {  //密码可见
	            userPsdET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());  
	        } else {  //密码不可见
	            userPsdET.setTransformationMethod(PasswordTransformationMethod.getInstance());  
	        }  
		}
	}
	
	/*登录按钮点击响应事件*/
	public void loginButtonClick(View v)
	{
		/*合法性检查*/
		if(TextUtils.isEmpty(userIdET.getText().toString())){
			showMessage("请输入用户名");
			return;
		}
		userId = Long.valueOf(userIdET.getText().toString());
		String userPsd = userPsdET.getText().toString();
		if(TextUtils.isEmpty(userPsd)) {
			showMessage("请输入密码");
			return;
		}
		/*向服务器发送数据*/
		JSONObject jo = new JSONObject();
		try {
			jo.put("operate", 2);
			jo.put("user_no", userId);
			jo.put("passwd", getSHA(userPsd));
		}catch(JSONException e) {
			Log.i(TAG, "Json Err");
			return ;
		}
		/*socketThread*/
		SocketThread sktThread = new SocketThread(jo.toString(), handler);
		sktThread.start();
	}
	
	/*显示提示框*/
	private void showMessage(String str) {
		new AlertDialog.Builder(this).setMessage(str).setPositiveButton("确定", null).show();
	}
	
	/*显示提示框*/
	private void showMessageNew(String str) {
		new AlertDialog.Builder(this).setMessage(str).setPositiveButton("确定", new loginListener()).show();
	}
	
	public class loginListener implements DialogInterface.OnClickListener{
		public void onClick(DialogInterface dialog, int which) {
			Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(mainIntent);
		}
	}
	
	/*注册按钮点击响应事件*/
	public void signinButtonClick(View v)
	{
		Intent signinIntent = new Intent(LoginActivity.this, SigninActivity.class);
		startActivity(signinIntent);
	}
	
	private String getSHA(String val) {  
		try {
	        MessageDigest md5 = MessageDigest.getInstance("SHA-1");  
	        md5.update(val.getBytes());  
	        byte[] m = md5.digest();//加密  
	        return getString(m);
		}catch(NoSuchAlgorithmException e) {
			Log.i(TAG, "不存在SHA1算法");
			return "";
		}
}  
            
    private static String getString(byte[] b){  
        StringBuffer sb = new StringBuffer();  
        for(int i = 0; i < b.length; i ++){  
          sb.append(b[i]);  
        }  
        return sb.toString();  
    }
}
