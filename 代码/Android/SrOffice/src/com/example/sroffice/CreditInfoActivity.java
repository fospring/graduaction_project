package com.example.sroffice;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CreditInfoActivity extends Activity {
	/*布局变量*/
	private TextView TVCredit;
	private LinearLayout LLCredit;
	/*全局变量*/
	private MyApp myApp;
	private static String TAG = "Credit Info";
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == 0x00) {
				Bundle bundle = msg.getData();
				try {
					JSONObject data = new JSONObject(bundle.get("msg").toString());
					String result = data.getString("result");
					if(result == "success") {
						int len = data.getInt("len");
						TVCredit.setText("当前积分：" + data.getString("credit"));
						for(int i = 0; i < len; i++) {
							final JSONObject json = new JSONObject(data.getString((i+1) + ""));
							LinearLayout credit = new LinearLayout(CreditInfoActivity.this);
							LinearLayout.LayoutParams creditLP = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0, 1);
							creditLP.setMargins(5, 5, 0, 5);
							credit.setLayoutParams(creditLP);
							credit.setOrientation(LinearLayout.HORIZONTAL);
							/*TextView*/
							TextView time = new TextView(CreditInfoActivity.this);
							time.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3));
							time.setGravity(Gravity.LEFT);
							time.setTextColor(0x000000);
							time.setText(json.getString("time"));
							time.setTextSize(15);
							credit.addView(time);
							/*TextView*/
							TextView description = new TextView(CreditInfoActivity.this);
							description.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5));
							description.setGravity(Gravity.CENTER_HORIZONTAL);
							description.setTextColor(0x000000);
							description.setText(json.getString("description"));
							description.setTextSize(15);
							credit.addView(description);
							/*TextView*/
							TextView change = new TextView(CreditInfoActivity.this);
							change.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
							change.setGravity(Gravity.CENTER_HORIZONTAL);
							change.setTextColor(0x000000);
							change.setText(json.getString("asset"));
							change.setTextSize(15);
							credit.addView(change);
							/*添加信息*/
							LLCredit.addView(credit);
						}
					}
				}catch(JSONException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit_info);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.credit_info, menu);
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
	
	/*获取信用信息*/
	private void getCreditInfo() {
		JSONObject jo = new JSONObject();
		try {
			jo.put("operate", "requestCredit");
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
