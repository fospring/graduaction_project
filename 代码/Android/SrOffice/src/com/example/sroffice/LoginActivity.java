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
	/*���ֱ���*/
	private EditText userIdET;
	private EditText userPsdET;
	private CheckBox psdCB;
	/*ȫ�ֱ���*/
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
						Log.i(TAG, "��¼�ɹ�");
						myApp.setIsSigned(true);
						Log.i(TAG, data.getLong("user_no")+"");
						myApp.setUserNo(userId);
						Log.i(TAG, data.getString("user"));
						myApp.setUserName(data.getString("user"));
						Log.i(TAG, "��¼�ɹ�222");
						showMessageNew("��¼�ɹ�");
					}
					else {
						int errType = data.getInt("errType");
						Log.i(TAG, errType+"");
						if(errType == 9002) {
							showMessage("�û������������");
						}
						else if(errType == 9901) {
							showMessage("���ݿ����");
						}else if(errType == 9902) {
							showMessage("�޷�����������");
						}
					}
				}catch(JSONException e) {
					e.printStackTrace();
				}
			}
			else {
				showMessage("����ʧ�ܣ�");
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		/*��ʼ��*/
		userIdET = (EditText) findViewById(R.id.ETUserId);
		userPsdET = (EditText) findViewById(R.id.ETUserPsd);
		psdCB = (CheckBox) findViewById(R.id.CBShowPsd);
		/*��ʾ������Ӧ�¼�*/
		psdCB.setOnClickListener(new psdCBClickListener());
		/*��ʼ��ȫ�ֱ���*/
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
	
	/*��ʾ����CheckBox������*/
	private class psdCBClickListener implements OnClickListener {
		public void onClick(View v)
		{
			if (psdCB.isChecked()) {  //����ɼ�
	            userPsdET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());  
	        } else {  //���벻�ɼ�
	            userPsdET.setTransformationMethod(PasswordTransformationMethod.getInstance());  
	        }  
		}
	}
	
	/*��¼��ť�����Ӧ�¼�*/
	public void loginButtonClick(View v)
	{
		/*�Ϸ��Լ��*/
		if(TextUtils.isEmpty(userIdET.getText().toString())){
			showMessage("�������û���");
			return;
		}
		userId = Long.valueOf(userIdET.getText().toString());
		String userPsd = userPsdET.getText().toString();
		if(TextUtils.isEmpty(userPsd)) {
			showMessage("����������");
			return;
		}
		/*���������������*/
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
	
	/*��ʾ��ʾ��*/
	private void showMessage(String str) {
		new AlertDialog.Builder(this).setMessage(str).setPositiveButton("ȷ��", null).show();
	}
	
	/*��ʾ��ʾ��*/
	private void showMessageNew(String str) {
		new AlertDialog.Builder(this).setMessage(str).setPositiveButton("ȷ��", new loginListener()).show();
	}
	
	public class loginListener implements DialogInterface.OnClickListener{
		public void onClick(DialogInterface dialog, int which) {
			Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(mainIntent);
		}
	}
	
	/*ע�ᰴť�����Ӧ�¼�*/
	public void signinButtonClick(View v)
	{
		Intent signinIntent = new Intent(LoginActivity.this, SigninActivity.class);
		startActivity(signinIntent);
	}
	
	private String getSHA(String val) {  
		try {
	        MessageDigest md5 = MessageDigest.getInstance("SHA-1");  
	        md5.update(val.getBytes());  
	        byte[] m = md5.digest();//����  
	        return getString(m);
		}catch(NoSuchAlgorithmException e) {
			Log.i(TAG, "������SHA1�㷨");
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
