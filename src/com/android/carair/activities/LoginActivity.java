package com.android.carair.activities;

import com.android.carair.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.carair_login_activity);
//		if (!isFirstLogin()) {
//			startMainActivity();
//		}
		
		
	}
	
	private boolean isFirstLogin() {
		return true;
	}

	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
	
}
