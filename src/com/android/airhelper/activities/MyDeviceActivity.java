
package com.android.airhelper.activities;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.carair.R;
import com.android.carair.utils.Util;
import com.umeng.analytics.MobclickAgent;

public class MyDeviceActivity extends SherlockActivity {
    Button btLoginOut;
    TextView tvDeviceId;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carair_mydevice_fragment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("我的设备");
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.actionbar_background));
        btLoginOut = (Button) findViewById(R.id.login_out);
        tvDeviceId = (TextView) findViewById(R.id.tvDeviceId);
        String id = String.format("设备号:%s", Util.getDeviceId(this));
        tvDeviceId.setText(id);
        btLoginOut.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Util.clearDeviceId(MyDeviceActivity.this);
                Intent intent = new Intent();
                intent.setClass(MyDeviceActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
