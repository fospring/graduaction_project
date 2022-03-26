package com.example.sroffice;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	/*布局变量*/
	private EditText ETPlace;
	private Button BStation;
	private Button BMeetingRoom;
	private LinearLayout LLStationInfo;
	private Button BShowAll;
	/*全局变量*/
	private MyApp myApp;
	private static String TAG = "MainActivity";
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
						int len = data.getInt("len");
						Log.i(TAG, len+"");
						for(int i = 0; i < len; i++) {
							final JSONObject json = new JSONObject(data.getString((i+1) + ""));
							Log.i(TAG, json.toString());
							LinearLayout station = new LinearLayout(MainActivity.this);
							LayoutParams lp = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200);
							station.setLayoutParams(lp);
							station.setOrientation(LinearLayout.HORIZONTAL);
							station.setClickable(true);
							Log.i(TAG, "ImageView");
							/*ImageView*/
							ImageView image = new ImageView(MainActivity.this);
							image.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3));;
							image.setScaleType(ScaleType.FIT_XY);
							image.setImageDrawable(getResources().getDrawable(R.drawable.main_station_example1));
							station.addView(image);
							Log.i(TAG, "TextClick");
							/*TextView*/
							TextView description = new TextView(MainActivity.this);
							LinearLayout.LayoutParams TVLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 5);
							TVLP.setMargins(5, 0, 0, 0);
							description.setLayoutParams(TVLP);
							//Log.i(TAG, "Description:" + json.getString("name") + "\n" + json.getString("introduce"));
							description.setText(json.getString("name") + "\n" + json.getString("introduce"));
							description.setTextColor(Color.RED);
							station.addView(description);
							//Log.i(TAG, description.getText().toString());
							Log.i(TAG, "TexTVIew");
							/*TextView*/
							TextView price = new TextView(MainActivity.this);
							price.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 2));
							price.setTextColor(0xff0000);
							Log.i(TAG, "Price:" + json.getString("stationPrice"));
							price.setText(json.getString("stationPrice") + "\n元/天");
							price.setTextColor(Color.RED);
							station.addView(price);
							//Log.i(TAG, price.getText().toString());
							Log.i(TAG, "CLICK");
							/*点击事件*/
							station.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									Log.i(TAG, "LinearLayout Click");
									Intent rentIntent = new Intent(MainActivity.this, RentStationActivity.class);
									try {
										Log.i(TAG, json.getString("station_no"));
										rentIntent.putExtra("station_no", json.getString("station_no"));
										Log.i(TAG, json.getString("user_no"));
										rentIntent.putExtra("user_no", json.getString("user_no"));
										Log.i(TAG, json.getString("name"));
										rentIntent.putExtra("stationName", json.getString("name"));
										Log.i(TAG, json.getString("type"));
										rentIntent.putExtra("stationType", json.getString("type"));
										Log.i(TAG, json.getString("address"));
										rentIntent.putExtra("stationAddr", json.getString("address"));
										Log.i(TAG, json.getString("stationCap"));
										rentIntent.putExtra("stationCap", json.getString("stationCap"));
										Log.i(TAG, json.getString("stationPrice"));
										rentIntent.putExtra("stationPrice", json.getString("stationPrice"));
										Log.i(TAG, json.getString("introduce"));
										rentIntent.putExtra("stationDescription", json.getString("introduce"));
										startActivity(rentIntent);
									}catch(JSONException e) {
										Log.i(TAG, "Intent Json Error");
										return;
									}
								}
							});
							/*添加到LLStationInfo中*/
							LLStationInfo.addView(station);
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
		setContentView(R.layout.activity_main);
		/*布局变量初始化*/
		LLStationInfo = (LinearLayout)findViewById(R.id.LLInfo);
		BShowAll = (Button)findViewById(R.id.BShowAll);
		BShowAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	requestStation();
            }
        });
		/*初始化*/
		myApp = (MyApp)getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	
	/*菜单栏租赁按钮点击响应事件*/
	public void mainButtonClick(View v)
	{
		Intent signinIntent = new Intent(MainActivity.this, MainActivity.class);
		startActivity(signinIntent);
	}
	
	/*菜单栏购物按钮点击响应事件*/
	public void shopButtonClick(View v)
	{
		Intent signinIntent = new Intent(MainActivity.this, ShopActivity.class);
		startActivity(signinIntent);
	}
	
	/*菜单栏论坛按钮点击响应事件*/
	public void forumButtonClick(View v)
	{
		
	}
	
	/*菜单栏其他按钮点击响应事件*/
	public void elseButtonClick(View v)
	{
		
	}
	
	/*菜单栏租赁按钮点击响应事件*/
	public void mineButtonClick(View v)
	{
		Intent signinIntent = new Intent(MainActivity.this, MineInfoActivity.class);
		startActivity(signinIntent);
	}
	
	/*查询所有工位信息*/
	private void requestStation() {
		Log.i(TAG, "Json");
		JSONObject jo = new JSONObject();
		try {
			jo.put("operate", 10);
			jo.put("start_no", 100000);
		}catch(JSONException e) {
			Log.i(TAG, "Json Err");
			return;
		}
		/*socketThread*/
		SocketThread sktThread = new SocketThread(jo.toString(), handler);
		sktThread.start();
	}
}
