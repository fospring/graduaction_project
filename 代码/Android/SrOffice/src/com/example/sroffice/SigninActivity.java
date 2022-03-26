package com.example.sroffice;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sroffice.MineInfoActivity.loginListener;

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

public class SigninActivity extends Activity {
	/**/
	private String userName;			//�û���
	private String userPsd;				//�û�����
	private String userCfmPsd;			//ȷ������
	private String companyName;			//��˾����
	private String companyPhone;		//��ϵ�绰
	private String companyEmail;		//email
	private String companyAddr;			//��˾��ַ
	private String companyRep;			//��˾����
	private int companyIncome;			//��˾Ӫҵ��
	
	private String TAG = "Enterprise signin";
	private MyApp myApp;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "handleMessage");
			if(msg.what == 0x00) {
				Bundle bundle = msg.getData();
				try {
					JSONObject data = new JSONObject(bundle.get("msg").toString());
					String result = data.getString("result");
					Log.i(TAG, result);
					if(result.equals("success"))			//ע��ɹ�
					{
						myApp.setIsSigned(true);
						myApp.setUserName(userName);
						myApp.setUserNo(data.getLong("user_no"));
						showMessageNew("ע��ɹ�, �û�ID��" + data.getLong("user_no"));
					}
					else {
						int errType = data.getInt("errType");
						if(errType == 9901) {
							showMessage("���ݿ����");
						}else if(errType == 9902) {
							showMessage("�޷�����������");
						}
					} 
				}catch(JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	/*���ֱ���*/
	private EditText ETUserName;
	private EditText ETUserPsd;
	private EditText ETUserCfmPsd;
	private EditText ETCompanyName;
	private EditText ETCompanyPhone;
	private EditText ETCompanyEmail;
	private EditText ETCompanyAddr;
	private EditText ETCompanyRep;
	private EditText ETCompanyIncome;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
		/*��ȡ���ֱ���*/
		ETUserName = (EditText)findViewById(R.id.ETUserName);
		ETUserPsd = (EditText)findViewById(R.id.ETUserPsd);
		ETUserCfmPsd = (EditText)findViewById(R.id.ETConformPsd);
		ETCompanyName = (EditText)findViewById(R.id.ETCompanyName);
		ETCompanyPhone = (EditText)findViewById(R.id.ETPhone);
		ETCompanyEmail = (EditText)findViewById(R.id.ETUserName);
		ETCompanyAddr = (EditText)findViewById(R.id.ETCompanyAddr);
		ETCompanyRep = (EditText)findViewById(R.id.ETPresidentor);
		ETCompanyIncome = (EditText)findViewById(R.id.ETYearTurnOver);
		/*��ȡȫ�ֱ���*/
		myApp = (MyApp)getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signin, menu);
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
	
	/*��ʾ��ʾ��*/
	private void showMessageNew(String str) {
		new AlertDialog.Builder(this).setMessage(str).setPositiveButton("ȷ��", new signinListener()).show();
	}
	
	public class signinListener implements OnClickListener{
		public void onClick(DialogInterface dialog, int which) {
			Intent loginIntent = new Intent(SigninActivity.this, LoginActivity.class);
			startActivity(loginIntent);
		}
	}
	
	/*ע�ᰴť����¼���Ӧ*/
	public void signinClick(View v) {
		userName = ETUserName.getText().toString();
		userPsd = ETUserPsd.getText().toString();
		userCfmPsd = ETUserCfmPsd.getText().toString();
		companyName = ETCompanyName.getText().toString();
		companyPhone = ETCompanyPhone.getText().toString();
		companyEmail = ETCompanyEmail.getText().toString();
		companyAddr = ETCompanyAddr.getText().toString();
		companyRep = ETCompanyRep.getText().toString();
		/*�Ϸ����ж�*/
		if(userName.isEmpty())
		{
			showMessage("�û���Ϊ�գ������ԣ�");
			return;
		}
		if(TextUtils.isEmpty(userPsd))
		{
			showMessage("����Ϊ�գ������ԣ�");
			return;
		}
		if(TextUtils.isEmpty(userCfmPsd))
		{
			showMessage("ȷ������Ϊ�գ������ԣ�");
			return;
		}
		if(!userPsd.equals(userCfmPsd))		//���벻һ��
		{
			showMessage("���벻һ�£������ԣ�");
			return;
		}
		if(TextUtils.isEmpty(companyName))
		{
			showMessage("��˾����Ϊ�գ������ԣ�");
			return;
		}
		if(TextUtils.isEmpty(companyPhone))
		{
			showMessage("��ϵ�绰Ϊ�գ������ԣ�");
			return;
		}
		if(!isMobile(companyPhone))		//��ϵ�绰���ϸ�ʽ
		{
			showMessage("��ϵ�绰��ʽ���������ԣ�");
			return;
		}
		if(TextUtils.isEmpty(companyRep))
		{
			showMessage("��˾����Ϊ�գ������ԣ�");
			return;
		}
		try {
			companyIncome = Integer.parseInt(ETCompanyIncome.getText().toString());
		}catch(NumberFormatException e) {
			showMessage("�����ʽ���������ԣ�");
			return;
		}
		/*���������������*/
		JSONObject jo = new JSONObject();
		try {
			jo.put("operate", 1);
			jo.put("user", userName);
			jo.put("passwd", getSHA(userPsd));
			jo.put("phone", companyPhone);
			if(!TextUtils.isEmpty(companyEmail))
				jo.put("email", companyEmail);
			if(!TextUtils.isEmpty(companyAddr))
				jo.put("address", companyAddr);
			jo.put("type", "enterprise");
			jo.put("company", companyName);
			jo.put("represent", companyRep);
			jo.put("income", companyIncome);
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
