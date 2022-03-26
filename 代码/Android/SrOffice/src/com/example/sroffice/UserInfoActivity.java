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
	/*���ֱ���*/
	private EditText ETUserName;
	private EditText ETUserPsd;
	private EditText ETUserPhone;
	private EditText ETUserEmail;
	private EditText ETUserAddr;
	/*ȫ�ֱ���*/
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
						showMessage("�޸ĳɹ�");
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
		/*���ֱ�����ʼ��*/
		ETUserName = (EditText)findViewById(R.id.ETUserName);
		ETUserPsd = (EditText)findViewById(R.id.ETUserPsd);
		ETUserEmail = (EditText)findViewById(R.id.ETUserEmail);
		ETUserPhone = (EditText)findViewById(R.id.ETUserPhone);
		ETUserAddr = (EditText)findViewById(R.id.ETUserAddr);
		/*ȫ�ֱ�����ʼ��*/
		myApp = (MyApp)getApplication();
		/*�����ʼ��*/
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
	
	/*��ʾ��ʾ��*/
	private void showMessage(String str) {
		new AlertDialog.Builder(this).setMessage(str).setPositiveButton("ȷ��", null).show();
	}
	
	/*��ȡ�û���Ϣ*/
	private void getUserInfo() {
		/*����json�ַ���*/
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
	
	/*�޸��û���Ϣ*/
	private void reviseClick(View v) {
		String userName = ETUserName.getText().toString();
		String userPsd= ETUserPsd.getText().toString();
		String userPhone = ETUserPhone.getText().toString();
		String userEmail = ETUserEmail.getText().toString();
		String userAddr = ETUserAddr.getText().toString();
		/*�Ϸ����ж�*/
		if(TextUtils.isEmpty(userName))
		{
			showMessage("�û���Ϊ�գ������ԣ�");
			return;
		}
		if(TextUtils.isEmpty(userPsd))
		{
			showMessage("����Ϊ�գ������ԣ�");
			return;
		}
		if(TextUtils.isEmpty(userPhone))
		{
			showMessage("��ϵ�绰Ϊ�գ������ԣ�");
			return;
		}
		if(!isMobile(userPhone))		//��ϵ�绰���ϸ�ʽ
		{
			showMessage("��ϵ�绰��ʽ���������ԣ�");
			return;
		}
		/*���������������*/
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
	    �ƶ���134��135��136��137��138��139��150��151��152��157(TD)��158��159��178(��)��182��184��187��188
	    ��ͨ��130��131��132��152��155��156��185��186
	    ���ţ�133��153��170��173��177��180��181��189����1349��ͨ��
	    �ܽ��������ǵ�һλ�ض�Ϊ1���ڶ�λ�ض�Ϊ3��5��8������λ�õĿ���Ϊ0-9
	    */
	        String num = "[1][34578]\\d{9}";//"[1]"�����1λΪ����1��"[34578]"����ڶ�λ����Ϊ3��4��5��7��8�е�һ����"\\d{9}"��������ǿ�����0��9�����֣���9λ��
	        if (TextUtils.isEmpty(number)) {
	            return false;
	        } else {
	            //matches():�ַ����Ƿ��ڸ�����������ʽƥ��
	            return number.matches(num);
	        }
	}
}
