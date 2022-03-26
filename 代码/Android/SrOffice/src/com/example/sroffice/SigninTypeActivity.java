package com.example.sroffice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SigninTypeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin_type);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signin_type, menu);
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
	
	/*个人按钮点击事件响应*/
	public void individualButtonClick(View v) {
		Intent individualIntent = new Intent(SigninTypeActivity.this, SigninActivity.class);
		startActivity(individualIntent);
	}
	
	/*企业按钮点击事件响应*/
	public void enterpriseButtonClick(View v) {
		Intent enterpriseIntent = new Intent(SigninTypeActivity.this, SigninActivity.class);
		startActivity(enterpriseIntent);
	}
}
