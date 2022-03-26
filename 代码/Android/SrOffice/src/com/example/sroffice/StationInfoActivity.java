package com.example.sroffice;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class StationInfoActivity extends Activity {
	/*���ֱ���*/
	private EditText ETStationName;
	private EditText ETStationAddr;
	private EditText ETStationCap;
	private EditText ETStationPrice;
	private EditText ETStationDescription;
	private Button BClick;
	/*ȫ�ֱ���*/
	private MyApp myApp;
	private static String TAG = "StationInfo";
	private String type;
	private Handler submitHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == 0x00) {
				Bundle bundle = msg.getData();
				try {
					JSONObject data = new JSONObject(bundle.get("msg").toString());
					String result = data.getString("result");
					Log.i(TAG, result);
					if(result.equals("success")) {
						showMessageNew("�����ɹ�");
					}
					else {
						int errType = data.getInt("errType");
						Log.i(TAG, errType+"");
						
						if(errType == 9901) {
							showMessage("���ݿ����");
						}else if(errType == 9902) {
							showMessage("�޷�����������");
						}else {
							showMessage("����ʧ��");
						}
					}
				}catch(JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};
	private Handler imageHandler = new Handler() {
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station_info);
		/*���ֱ�����ʼ��*/
		ETStationName = (EditText)findViewById(R.id.ETStationName);
		ETStationAddr = (EditText)findViewById(R.id.ETStationAddr);
		ETStationCap = (EditText)findViewById(R.id.ETStationCap);
		ETStationPrice = (EditText)findViewById(R.id.ETStationPrice);
		ETStationDescription = (EditText)findViewById(R.id.ETStationDescription);
		BClick = (Button)findViewById(R.id.BSubmit);
		BClick.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	submitClick(v);
            }
        });
		/*ȫ�ֱ�����ʼ��*/
		myApp = (MyApp)getApplication();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.station_info, menu);
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
		new AlertDialog.Builder(this).setMessage(str).setPositiveButton("ȷ��", new loginListener()).show();
	}
	
	public class loginListener implements DialogInterface.OnClickListener{
		public void onClick(DialogInterface dialog, int which) {
			Intent mainIntent = new Intent(StationInfoActivity.this, MainActivity.class);
			startActivity(mainIntent);
		}
	}
	
	/*�ύ��ť����¼���Ӧ*/
	private void submitClick(View v) {
		Log.i(TAG, "submit");
		String stationName = ETStationName.getText().toString();
		String stationType;
		String stationAddr = ETStationAddr.getText().toString();
		String stationCap = ETStationCap.getText().toString();
		String stationPrice = ETStationPrice.getText().toString();
		String stationDescription = ETStationDescription.getText().toString();
		int cap, price;
		/*�Ϸ����ж�*/
		try {
			cap = Integer.parseInt(stationCap);
		}catch(NumberFormatException e) {
			showMessage("����������ʽ����");
			return;
		}
		try {
			price = Integer.parseInt(stationPrice);
		}catch(NumberFormatException e) {
			showMessage("�۸��ʽ����");
			return;
		}
		/*���������������*/
		Log.i(TAG, "json");
		JSONObject jo = new JSONObject();
		try {
			jo.put("operate", 6);
			jo.put("user_no", myApp.getUserNo());
			jo.put("name", stationName);
			//jo.put("type", stationType);
			jo.put("address", stationAddr);
			jo.put("size", cap);
			jo.put("price", price);
			jo.put("introduce", stationDescription);
			
		}catch(JSONException e) {
			Log.i(TAG, "Json Err");
			return;
		}
		/*socketThread*/
		SocketThread sktThread = new SocketThread(jo.toString(), submitHandler);
		sktThread.start();
	}
}
