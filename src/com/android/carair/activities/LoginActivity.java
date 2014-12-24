
package com.android.carair.activities;

import com.android.goodhelpercarair.R;
import com.android.carair.api.AppInfo;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.AESUtils;
import com.android.carair.utils.Util;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
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
        
        UmengUpdateAgent.update(this);
        btScan = (Button) findViewById(R.id.login_richscan);
//        btLogin = (Button) findViewById(R.id.login_button);
        mProgress = (ProgressBar) findViewById(R.id.common_mask_icon);

        btScan.setOnClickListener(this);

//        btLogin.setOnClickListener(this);

        if (isLogin()) {
            startMainActivity();
        }

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

    private boolean isLogin() {
        if (!TextUtils.isEmpty(Util.getDeviceId(this))) {
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
                this.startActivityForResult(intent, 0);
                break;
//            case R.id.login_button:
//                // 输入对话框
//                loginAlert();
//                break;
            default:
                break;
        }
    }

    private void login(final String id) {
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                mProgress.setVisibility(View.INVISIBLE);
                if ("0".equals(packet.getStatus())) {
                    // save id
                    Util.saveDeviceId(id, LoginActivity.this);
                    //save feature info
                    AppInfo info  = packet.getRespMessage().getAppinfo();
                    if(info != null){
                        Util.saveFeature(info, LoginActivity.this);
                    }
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "登录失败，请重试", 1).show();
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {
                mProgress.setVisibility(View.INVISIBLE);
                Toast.makeText(LoginActivity.this, "登录失败，请重试", 1).show();
            }
        }.reg(LoginActivity.this, id);
    }

    private void loginAlert() {
        final Builder builder = new AlertDialog.Builder(this).setTitle("请输入设备id")
                .setPositiveButton("确认",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                mProgress.setVisibility(View.VISIBLE);
                                id = etText.getText().toString();
                                if (TextUtils.isEmpty(id)) {
                                    Toast.makeText(LoginActivity.this, "请输入正确的id", 1).show();
                                    return;
                                }
                                login(id);
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
    
    private void encode(String id){
         byte[] buffer_64 = Base64.decode(id, Base64.DEFAULT);
         byte[] sec = "176489149810280637ff4d2ba81e6b3b".getBytes();
         byte[] iv = "df63b8c8189ad9a1".getBytes();
         String result = AESUtils.decryptScan(sec,buffer_64,iv);
         login(result);
//         Toast.makeText(this, result, 1).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode) {
            if (data != null) {
                String id = data.getStringExtra("data");
                mProgress.setVisibility(View.VISIBLE);
//                login(id);
                encode(id);
            }
        }
    }

}
