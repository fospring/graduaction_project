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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class RentStationActivity extends Activity {
	/*布局变量*/
	private TextView TVStationName;
	private TextView TVStationAddr;
	private TextView TVStationCap;
	private TextView TVStationPrice;
	private TextView TVStationDescription;
	private Button BRent;
	/*局部变量*/
	private String stationNo;
	private String stationName;
	private String stationType;
	private String stationAddr;
	private String stationCap;
	private String stationPrice;
	private String stationDescription;
	/*全局变量*/
	private MyApp myApp;
	private static String TAG = "rent";
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == 0x00) {
				Bundle bundle = msg.getData();
				try {
					JSONObject data = new JSONObject(bundle.get("msg").toString());
					String result = data.getString("result");
					if(result == "success") {
						showMessage("租用成功");
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
		setContentView(R.layout.activity_rent_station);
		Log.i(TAG, "Init");
		/*全局变量初始化*/
		myApp = (MyApp)getApplication();
		/*布局变量初始化*/
		TVStationName = (TextView)findViewById(R.id.TVStationName);
		//TVStationType = (TextView)findViewById(R.id.TVStationType);
		TVStationAddr = (TextView)findViewById(R.id.TVStationAddr);
		TVStationCap = (TextView)findViewById(R.id.TVStationCap);
		TVStationPrice = (TextView)findViewById(R.id.TVStationPrice);
		TVStationDescription = (TextView)findViewById(R.id.TVStationDescription);
		BRent = (Button)findViewById(R.id.BSubmit);
		BRent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	rentClick(v);
            }
        });
		Log.i(TAG, "Intent");
		/*获取intent对象*/
		Intent intent = getIntent();
		stationNo = intent.getStringExtra("station_no");
		stationName = intent.getStringExtra("stationName");
		stationType = intent.getStringExtra("stationType");
		stationAddr = intent.getStringExtra("stationAddr");
		stationCap = intent.getStringExtra("stationCap");
		stationPrice = intent.getStringExtra("stationPrice");
		stationDescription = intent.getStringExtra("stationDescrition");
		Log.i(TAG, "Info");
		/*界面工位信息初始化*/
		TVStationName.setText("工位名称：" + stationName);
		//TVStationType.setText(stationType);
		TVStationAddr.setText("工位地址：" + stationAddr);
		TVStationCap.setText("容纳人数：" + stationCap);
		TVStationPrice.setText("租赁价格：" + stationPrice);
		if(!TextUtils.isEmpty(stationDescription))
			TVStationDescription.setText("工位描述:" + stationDescription);
		else
			TVStationDescription.setText("工位描述:无");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rent_station, menu);
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
			Intent loginIntent = new Intent(RentStationActivity.this, LoginActivity.class);
			startActivity(loginIntent);
		}
	}
	
	/*租用按钮点击事件响应*/
	private void rentClick(View v) {
		Log.i(TAG, "RENT CLICK");
		if(myApp.getIsSigned()) {
			Intent pactIntent = new Intent(RentStationActivity.this, PactActivity.class);
			pactIntent.putExtra("stationNo", stationNo);
			pactIntent.putExtra("stationName",stationName );
			pactIntent.putExtra("stationAddr", stationAddr);
			pactIntent.putExtra("stationCap", stationCap);
			pactIntent.putExtra("stationPrice", stationPrice);
			Log.i(TAG, "end CLICK");
			startActivity(pactIntent);
		}
		else {
			showMessageNew("请登录！");
		}
	}
}
