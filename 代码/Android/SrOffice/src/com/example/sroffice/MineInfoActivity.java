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
	/*���ֱ���*/
	private TextView TVUserName;
	private MyApp myApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mine_info);
		/*��ȡ���ֱ���*/
		TVUserName = (TextView)findViewById(R.id.TVUserName);
		/*��ʼ��*/
		myApp = (MyApp)getApplication();
		if(!myApp.getIsSigned()) {		//�û�δ��¼
			new AlertDialog.Builder(this).setMessage("���¼").setPositiveButton("ȷ��", new loginListener()).show();
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
	
	/*�û���Ϣ��ť����¼���Ӧ*/
	public void userInfoButtonClick(View v) {
		if(myApp.getIsSigned()) {				//�ѵ�¼
			Intent userInfoIntent = new Intent(MineInfoActivity.this, UserInfoActivity.class);
			startActivity(userInfoIntent);
		}else {
			new AlertDialog.Builder(this).setMessage("���¼").setPositiveButton("ȷ��", new loginListener()).show();
		}
	}
	
	/*������Ϣ��ť����¼���Ӧ*/
	public void rentInfoButtonClick(View v) {
		if(myApp.getIsSigned()) {				//�ѵ�¼
			Intent rentInfoIntent = new Intent(MineInfoActivity.this, RentInfoActivity.class);
			startActivity(rentInfoIntent);
		}else {		//δ��¼
			new AlertDialog.Builder(this).setMessage("���¼").setPositiveButton("ȷ��", new loginListener()).show();
		}
	}
	
	/*������Ϣ��ť����¼���Ӧ*/
	public void creditInfoButtonClick(View v) {
		if(myApp.getIsSigned()) {				//�ѵ�¼
			Intent creditInfoIntent = new Intent(MineInfoActivity.this, CreditInfoActivity.class);
			startActivity(creditInfoIntent);
		}else {		//δ��¼
			new AlertDialog.Builder(this).setMessage("���¼").setPositiveButton("ȷ��", new loginListener()).show();
		}
	}
	
	public void applyStationButtonClick(View v) {
		if(myApp.getIsSigned()) {				//�ѵ�¼
			Intent stationInfoIntent = new Intent(MineInfoActivity.this, StationInfoActivity.class);
			startActivity(stationInfoIntent);
		}else {		//δ��¼
			new AlertDialog.Builder(this).setMessage("���¼").setPositiveButton("ȷ��", new loginListener()).show();
		}
	}
	
	/*�˵������ް�ť�����Ӧ�¼�*/
	public void mainButtonClick(View v)
	{
		Intent signinIntent = new Intent(MineInfoActivity.this, MainActivity.class);
		startActivity(signinIntent);
	}
	
	/*�˵������ﰴť�����Ӧ�¼�*/
	public void shopButtonClick(View v)
	{
		Intent signinIntent = new Intent(MineInfoActivity.this, ShopActivity.class);
		startActivity(signinIntent);
	}
	
	/*�˵�����̳��ť�����Ӧ�¼�*/
	public void forumButtonClick(View v)
	{
		
	}
	
	/*�˵���������ť�����Ӧ�¼�*/
	public void elseButtonClick(View v)
	{
		
	}
	
	/*�˵������ް�ť�����Ӧ�¼�*/
	public void mineButtonClick(View v)
	{
		Intent signinIntent = new Intent(MineInfoActivity.this, MineInfoActivity.class);
		startActivity(signinIntent);
	}
}
