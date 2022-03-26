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
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class Signin2Activity extends Activity {
	/*布局变量*/
	private EditText ETUserName;
	private EditText ETUserPsd;
	private EditText ETUserCfmPsd;
	private EditText ETUserRealName;
	private EditText ETUserPhone;
	private EditText ETUserEmail;
	private EditText ETUserAddr;
	
	private String userName;
	
	private MyApp myApp;
	private String TAG = "Individual signin";
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0x00) {
				Bundle bundle = msg.getData();
				try {
					JSONObject data = new JSONObject(bundle.get("msg").toString());
					String result = data.getString("result");
					if(result == "success")			//注册成功
					{
						myApp.setIsSigned(true);
						myApp.setUserName(userName);
						myApp.setUserNo(data.getLong("user_no"));
						showMessageNew("注册成功, 用户ID：" + data.getLong("user_no"));
					}
					else {
						int errType = data.getInt("errType");
						if(errType == 9901) {
							showMessage("数据库错误！");
						}else if(errType == 9902) {
							showMessage("无法连接区块链");
						}
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
		setContentView(R.layout.activity_signin2);
		/*布局变量初始化*/
		ETUserName = (EditText)findViewById(R.id.ETUserName);
		ETUserPsd = (EditText)findViewById(R.id.ETUserPsd);
		ETUserCfmPsd = (EditText)findViewById(R.id.ETConformPsd);
		ETUserRealName = (EditText)findViewById(R.id.ETName);
		ETUserPhone = (EditText)findViewById(R.id.ETPhone);
		ETUserEmail = (EditText)findViewById(R.id.ETEmail);
		ETUserAddr = (EditText)findViewById(R.id.ETAddress);
		/*获取全局变量*/
		myApp = (MyApp)getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signin2, menu);
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
	
	/*显示提示框*/
	private void showMessageNew(String str) {
		new AlertDialog.Builder(this).setMessage(str).setPositiveButton("确定", new signinListener()).show();
	}
	
	public class signinListener implements OnClickListener{
		public void onClick(DialogInterface dialog, int which) {
			Intent loginIntent = new Intent(Signin2Activity.this, LoginActivity.class);
			startActivity(loginIntent);
		}
	}
	
	/*注册按钮点击事件*/
	private void singinClick(View v) {
		userName = ETUserName.getText().toString();
		String userPsd = ETUserPsd.getText().toString();
		String userCfmPsd = ETUserCfmPsd.getText().toString();
		String userRealName = ETUserRealName.getText().toString();
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
		if(TextUtils.isEmpty(userCfmPsd))
		{
			showMessage("确认密码为空，请重试！");
			return;
		}
		if(!userPsd.equals(userCfmPsd))		//密码不一致
		{
			showMessage("密码不一致，请重试！");
			return;
		}
		if(TextUtils.isEmpty(userRealName))
		{
			showMessage("姓名为空，请重试！");
			return;
		}
		if(TextUtils.isEmpty(userPhone))
		{
			showMessage("联系电话为空，请重试！");
			return;
		}
		if(!isMobile(userPhone))		
		{
			showMessage("联系电话格式错误，请重试！");
			return;
		}
		/*向服务器发送数据*/
		JSONObject jo = new JSONObject();
		try {
			jo.put("operate", 1);
			jo.put("user", userName);
			jo.put("passwd", getSHA(userPsd));
			jo.put("phone", userPhone);
			if(!TextUtils.isEmpty(userEmail))
				jo.put("email", userEmail);
			if(!TextUtils.isEmpty(userAddr))
				jo.put("address", userAddr);
			jo.put("type", "enterprise");
			jo.put("represent", userRealName);
		}catch(JSONException e) {
			Log.i(TAG, "Json Err");
			return;
		}
		/*socketThread*/
		SocketThread sktThread = new SocketThread(jo.toString(), handler);
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
