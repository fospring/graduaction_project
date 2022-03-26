package com.example.sroffice;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sroffice.LoginActivity.loginListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class PactActivity extends Activity {
	/*布局变量*/
	private EditText ETStationName;
	private EditText ETStationAddr;
	private EditText ETStationCap;
	private EditText ETStationPrice;
	private EditText ETStationUserNo;
	private EditText ETStationRentNo;
	private EditText ETStationRentTime;
	private CheckBox check;
	private Button BConfirm;
	/*局部变量*/
	private long stationNo;
	private String stationName;
	private String stationAddr;
	private String stationPrice;
	private String stationCap;
	private int stationRentTime;
	/*全局变量*/
	private static String TAG = "Pact";
	private MyApp myApp;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == 0x00) {
				Bundle bundle = msg.getData();
				try {
					JSONObject data = new JSONObject(bundle.get("msg").toString());
					String result = data.getString("result");
					Log.i(TAG, result);
					if(result.equals("success")) {
						showMessageNew("租赁成功");
					}else {
						int errType = data.getInt("errType");
						Log.i(TAG, errType+"");
						if(errType == 9004) {
							showMessage("工位不存在");
						}else if(errType == 9005) {
							showMessage("自己的工位");
						}else if(errType == 9007){
							showMessage("工位已被使用");
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
		setContentView(R.layout.activity_pact);
		/*获取布局变量*/
		ETStationName = (EditText)findViewById(R.id.ETStationName);
		ETStationAddr = (EditText)findViewById(R.id.ETStationAddr);
		ETStationCap = (EditText)findViewById(R.id.ETStationCap);
		ETStationPrice = (EditText)findViewById(R.id.ETStationPrice);
		ETStationRentNo = (EditText)findViewById(R.id.ETRentNo);
		ETStationRentTime = (EditText)findViewById(R.id.ETRentTime);
		check = (CheckBox)findViewById(R.id.CBAccept);
		BConfirm = (Button)findViewById(R.id.BConfirm);
		BConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	conformClick(v);
            }
        });
		/*获取全局变量*/
		myApp = (MyApp)getApplication();
		/*获取intent*/
		Intent intent = getIntent();
		stationNo = Long.valueOf(intent.getStringExtra("stationNo"));
		stationName = intent.getStringExtra("stationName");
		stationAddr = intent.getStringExtra("stationAddr");
		stationCap = intent.getStringExtra("stationCap");
		stationPrice = intent.getStringExtra("stationPrice");
		/*合同信息显示*/
		ETStationName.setText(stationName);
		ETStationAddr.setText(stationAddr);
		ETStationCap.setText(stationCap);
		ETStationPrice.setText(stationPrice);
		ETStationRentNo.setText(myApp.getUserNo()+"");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pact, menu);
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
		new AlertDialog.Builder(this).setMessage(str).setPositiveButton("确定", new loginListener()).show();
	}
	
	public class loginListener implements DialogInterface.OnClickListener{
		public void onClick(DialogInterface dialog, int which) {
			Intent mainIntent = new Intent(PactActivity.this, MainActivity.class);
			startActivity(mainIntent);
		}
	}
	
	/*确认按钮点击事件响应*/
	private void conformClick(View v) {
		/*合法性判断*/
		try {
			stationRentTime = Integer.parseInt(ETStationRentTime.getText().toString());
		}catch(NumberFormatException e) {
			showMessage("租赁时长格式错误");
			return;
		}
		if(!check.isChecked()) {
			showMessage("请接受租赁守则");
			return;
		}
		/*向服务器发送数据*/
		Log.i(TAG, "JSON");
		JSONObject jo = new JSONObject();
		try {
			jo.put("operate", 8);
			jo.put("user_no", myApp.getUserNo());
			jo.put("station_no", stationNo);
			jo.put("time", stationRentTime);
		}catch(JSONException e) {
			Log.i(TAG, "Json Err");
			return;
		}
		/*socketThread*/
		SocketThread sktThread = new SocketThread(jo.toString(), handler);
		sktThread.start();
	}
}
