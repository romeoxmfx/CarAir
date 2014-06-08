
package com.android.carair.activities;

import com.android.carair.R;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {
    Button btScan;
    Button btLogin;
    EditText etText;
    String id;
    ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.carair_login_activity);
        btScan = (Button) findViewById(R.id.login_richscan);
        btLogin = (Button) findViewById(R.id.login_button);
        mProgress = (ProgressBar) findViewById(R.id.common_mask_icon);

        btScan.setOnClickListener(this);

        btLogin.setOnClickListener(this);

        if (isLogin()) {
            startMainActivity();
        }

    }

    private boolean isLogin() {
        if(!TextUtils.isEmpty(Util.getDeviceId(this))){
            return true;
        }
        return false;
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.login_richscan:
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, CameraActivity.class);
                this.startActivity(intent);
                break;
            case R.id.login_button:
                // 输入对话框
                loginAlert();
                break;
            default:
                break;
        }
    }

    private void loginAlert() {
        final Builder builder = new AlertDialog.Builder(this).setTitle("请输入设备id")
                .setPositiveButton("确认",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                mProgress.setVisibility(View.VISIBLE);
                                id = etText.getText().toString();
                                if(TextUtils.isEmpty(id)){
                                    Toast.makeText(LoginActivity.this, "请输入正确的id", 1).show();
                                    return;
                                }
                                new CarAirReqTask() {
                                    
                                    @Override
                                    public void onCompleteSucceed(RespProtocolPacket packet) {
                                        mProgress.setVisibility(View.INVISIBLE);
                                        if("0".equals(packet.getStatus())){
                                            //save id
                                            Util.saveDeviceId(id, LoginActivity.this);
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }else{
                                            Toast.makeText(LoginActivity.this, "登录失败，请重试", 1).show();
                                        }
                                    }
                                    
                                    @Override
                                    public void onCompleteFailed(int type, HttpErrorBean error) {
                                        Toast.makeText(LoginActivity.this, "登录失败，请重试", 1).show();
                                    }
                                }.reg(LoginActivity.this,id);
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        etText = new EditText(this);
        builder.setView(etText);
        builder.show();
    }

}
