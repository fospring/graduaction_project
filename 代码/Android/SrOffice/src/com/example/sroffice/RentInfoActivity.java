package com.example.sroffice;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sroffice.LoginActivity.loginListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RentInfoActivity extends Activity {
	/*布局变量*/
	private LinearLayout LLStation;
	private LinearLayout LLMeetingRoom;
	/*全局变量*/
	private MyApp myApp;
	private static String TAG = "RentInfo";
	private class myButton extends Button {
		public long tradeNo;
		
		public myButton(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
	}
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Log.i(TAG, msg.what+"");
			if(msg.what == 0x00) {
				Bundle bundle = msg.getData();
				try {
					JSONObject data = new JSONObject(bundle.getString("msg").toString());
					String result = data.getString("result");
					Log.i(TAG, result);
					if(result.equals("success")) {
						int len = data.getInt("len");
						Log.i(TAG, "len:" + len);
						for(int i = 0; i < len; i++) {
							final JSONObject json = new JSONObject(data.getString((i+1) + ""));
							LinearLayout info = new LinearLayout(RentInfoActivity.this);
							LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 120);
							lp.setMargins(5, 5, 0, 5);
							info.setLayoutParams(lp);
							info.setOrientation(LinearLayout.HORIZONTAL);
							Log.i(TAG, "TextView");
							/*TextView*/
							TextView name = new TextView(RentInfoActivity.this);
							name.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3));
							name.setGravity(Gravity.LEFT);
							name.setText(json.getLong("station_no")+"");
							name.setTextColor(Color.BLACK);
							name.setTextSize(12);
							info.addView(name);
							Log.i(TAG, "TextView");
							/*TextView*/
							TextView expireTime = new TextView(RentInfoActivity.this);
							expireTime.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 5));
							expireTime.setGravity(Gravity.CENTER_HORIZONTAL);
							expireTime.setText(json.getString("time"));
							expireTime.setTextColor(Color.BLACK);
							expireTime.setTextSize(12);
							info.addView(expireTime);
							Log.i(TAG, "Button");
							/*Button*/
							Button reExpire = new Button(RentInfoActivity.this);
							LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f);
							blp.gravity = Gravity.CENTER_HORIZONTAL;
							reExpire.setLayoutParams(blp);
							reExpire.setText("续租");
							reExpire.setTextColor(Color.BLACK);
							reExpire.setBackgroundColor(Color.GRAY);
							reExpire.setTextSize(12);
							info.addView(reExpire);
							Log.i(TAG, "TextView");
							/*TextView*/
							TextView tv = new TextView(RentInfoActivity.this);
							tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.4f));
							info.addView(tv);
							if(!json.getBoolean("finish")) {
								/*Button*/
								Log.i(TAG, "Button");
								myButton exitExpire = new myButton(RentInfoActivity.this);
								LinearLayout.LayoutParams blp2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f);
								blp2.gravity = Gravity.CENTER_HORIZONTAL;
								exitExpire.setLayoutParams(blp2);
								exitExpire.setText("退租");
								exitExpire.setTextColor(Color.BLACK);
								exitExpire.setBackgroundColor(Color.GRAY);
								exitExpire.setTextSize(12);
								Log.i(TAG, json.getLong("trade_no")+"");
								exitExpire.tradeNo = json.getLong("trade_no");
								Log.i(TAG, "Button2");
								exitExpire.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										Log.i(TAG, "Click");
										JSONObject jo = new JSONObject();
										int id = v.getId();
										Log.i(TAG, "V");
										myButton b = (myButton)v;
										try {
											Log.i(TAG, "JSON");
											jo.put("operate", 9);
											jo.put("user_no", myApp.getUserNo());
											jo.put("trade_no", b.tradeNo);
											Log.i(TAG, b.tradeNo+"");
										}catch(JSONException e) {
											Log.i(TAG, "Json Err");
											return;
										}
										/*socketThread*/
										SocketThread sktThread = new SocketThread(jo.toString(), exitHandler);
										sktThread.start();
									}
								});
								Log.i(TAG, "Button3");
								info.addView(exitExpire);
							}else {
								TextView tv2 = new TextView(RentInfoActivity.this);
								tv2.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f));
								tv2.setText("已完成");
								tv2.setTextColor(Color.BLACK);
								tv2.setTextSize(12);
								info.addView(tv2);
							}
							//Log.i(TAG, json.getString("type"));
							if(json.getString("type").equals("buyer")) {
								Log.i(TAG, "BUYER");
								LLStation.addView(info);
							}
							else {
								Log.i(TAG, "SELLER");
								LLMeetingRoom.addView(info);
							}
						}
						Log.i(TAG, "END");
					}
				}catch(JSONException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	};
	private Handler exitHandler = new Handler() {
		public void handleMessage(Message msg) {
			showMessageNew("退租成功");
		}
	};
	/*显示提示框*/
	private void showMessageNew(String str) {
		new AlertDialog.Builder(this).setMessage(str).setPositiveButton("确定", new loginListener()).show();
	}
	
	public class loginListener implements DialogInterface.OnClickListener{
		public void onClick(DialogInterface dialog, int which) {
			Intent mainIntent = new Intent(RentInfoActivity.this, RentInfoActivity.class);
			startActivity(mainIntent);
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rent_info);
		/*获取布局变量*/
		LLStation = (LinearLayout)findViewById(R.id.LLStation);
		LLMeetingRoom = (LinearLayout)findViewById(R.id.LLMeetingRoom);
		/*获取全局变量*/
		myApp = (MyApp)getApplication();
		getRentInfo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rent_info, menu);
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
	
	/*获取租赁信息*/
	private void getRentInfo() {
		Log.i(TAG, "json");
		JSONObject jo = new JSONObject();
		try {
			jo.put("operate", 11);
			jo.put("user_no", myApp.getUserNo());
		}catch(JSONException e) {
			Log.i(TAG, "Json Err");
			return;
		}
		/*socketThread*/
		SocketThread sktThread = new SocketThread(jo.toString(), handler);
		sktThread.start();
	}
}
