package com.android.carair.activities;

import com.android.carair.R;
import com.android.carair.utils.Util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

public class LogoActivity extends Activity{
    Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent();
            intent.setClass(LogoActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        };
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carair_splash_activity);
        if(Util.isFirstLogin(this)){
            Intent intent = new Intent();
            intent.setClass(LogoActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            mHandler.sendEmptyMessageDelayed(0,1000);
        }
    }
}
