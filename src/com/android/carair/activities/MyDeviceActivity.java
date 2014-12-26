
package com.android.carair.activities;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.goodhelpercarair.R;
import com.android.carair.api.AppInfo;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.Gyroscope;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.api.Sleep_period;
import com.android.carair.common.CarAirManager;
import com.android.carair.common.CarairConstants;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.Util;
import com.umeng.analytics.MobclickAgent;

public class MyDeviceActivity extends SherlockActivity {
    Button btLoginOut;
    TextView tvDeviceId;
    LinearLayout sleep_period;
    Button gyroscopes;
    Button btsleep_start;
    Button btsleep_end;
    // Button btSleepSend;
    public static final int DIALOG_SLEEP_START = 0;
    public static final int DIALOG_SLEEP_END = 1;
    public static final int DIALOG_GYROSCOPE = 2;
    private boolean getConfigSleep;
    private boolean getConfiggyroscope;
    int start_hour = 0;
    int start_min = 0;
    int end_hour = 0;
    int end_min = 0;
    int sensitivity = 0;
    private boolean configChanged = false;

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

        sleep_period = (LinearLayout) findViewById(R.id.sleep_period);
        gyroscopes = (Button) findViewById(R.id.gyroscopes);
        btsleep_start = (Button) findViewById(R.id.btsleep_start);
        btsleep_end = (Button) findViewById(R.id.btsleep_end);
        // btSleepSend = (Button) findViewById(R.id.btSendSleepTime);

        // btSleepSend.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        //
        // }
        // });

        btsleep_start.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(CarAirManager.getInstance().ismConnection()){
                    openDialog(DIALOG_SLEEP_START);
                }else{
                    Toast.makeText(MyDeviceActivity.this, "请确保净化器处于连接状态再操作", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btsleep_end.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(CarAirManager.getInstance().ismConnection()){
                    openDialog(DIALOG_SLEEP_END);
                }else{
                    Toast.makeText(MyDeviceActivity.this, "请确保净化器处于连接状态再操作", Toast.LENGTH_SHORT).show();
                }
            }
        });

        gyroscopes.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openDialog(DIALOG_GYROSCOPE);
            }
        });

        AppInfo appinfo = Util.getFeature(this);
        getConfigSleep = false;
        getConfiggyroscope = false;
        if (appinfo != null) {
            int gyroscpes = appinfo.getHas_gyroscopes();
            if (CarairConstants.ON == gyroscpes) {
                getConfiggyroscope = true;
                gyroscopes.setVisibility(View.VISIBLE);
            }
            int sleep = appinfo.getHas_sleepperiod();
            if (CarairConstants.ON == sleep) {
                getConfigSleep = true;
                sleep_period.setVisibility(View.VISIBLE);
            }
        }

        if (getConfigSleep || getConfiggyroscope) {
            requestConfig();
        }
    }

    private void requestConfig() {
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                if (packet != null && "0".equals(packet.getStatus())) {
                    if (packet.getRespMessage() != null
                            && packet.getRespMessage().getDevinfo() != null) {
                        Sleep_period sleep = packet.getRespMessage().getDevinfo().getSleep_period();
                        if (sleep != null) {
                            Util.saveSleepPeriod(MyDeviceActivity.this, sleep);
                        }
                        Gyroscope gyroscope = packet.getRespMessage().getDevinfo().getGyroscope();
                        if (gyroscope != null) {
                            Util.saveGyroscope(MyDeviceActivity.this, gyroscope);
                        }
                        refreshInfo();
                    }
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {

            }
        }.config(this);
    }

    private void openDialog(int type) {
        Dialog dialog = null;
        switch (type) {
            case DIALOG_SLEEP_START:
                dialog = new TimePickerDialog(this, new OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // if(hourOfDay > end_hour){
                        // Toast.makeText(MyDeviceActivity.this, "开始时间必须小于结束时间",
                        // Toast.LENGTH_SHORT).show();
                        // }else if(hourOfDay == end_hour && minute >= end_min){
                        // Toast.makeText(MyDeviceActivity.this, "开始时间必须小于结束时间",
                        // Toast.LENGTH_SHORT).show();
                        // }else{
                        if (start_hour != hourOfDay || start_min != minute) {
                            configChanged = true;
                        }
                        start_hour = hourOfDay;
                        start_min = minute;
                        btsleep_start.setText(start_hour + ":" + start_min);
                        // }

                    }
                }, start_hour, start_min, true);
                dialog.setTitle("休眠开始时间设置");
                break;
            case DIALOG_SLEEP_END:
                dialog = new TimePickerDialog(this, new OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // if(hourOfDay < start_hour){
                        // Toast.makeText(MyDeviceActivity.this, "结束时间必须大于开始时间",
                        // Toast.LENGTH_SHORT).show();
                        // }else if(hourOfDay == start_hour && minute <=
                        // start_min){
                        // Toast.makeText(MyDeviceActivity.this, "结束时间必须大于开始时间",
                        // Toast.LENGTH_SHORT).show();
                        // }else{
                        if (end_hour != hourOfDay || end_min != minute) {
                            configChanged = true;
                        }
                        end_hour = hourOfDay;
                        end_min = minute;
                        btsleep_end.setText(end_hour + ":" + end_min);
                        // }
                    }
                }, end_hour, end_min, true);
                dialog.setTitle("休眠结束时间设置");
                break;
            case DIALOG_GYROSCOPE:
                Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("陀螺仪灵敏度设置");
                builder.setSingleChoiceItems(R.array.item_sensitivity, sensitivity,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (sensitivity != which) {
                                    configChanged = true;
                                }
                                sensitivity = which;
                                gyroscopes.setText("陀螺仪灵敏度:" + convertSensitivity(sensitivity));
                            }

                        });
                dialog = builder.create();
                break;
            default:
                break;
        }
        if (dialog != null) {
            dialog.show();
        }
    }

    public void configSet() {
        if (!configChanged) {
            return;
        }

        Sleep_period sleep = new Sleep_period();
        sleep.setEnd_hour(end_hour);
        sleep.setEnd_min(end_min);
        sleep.setStart_hour(start_hour);
        sleep.setStart_min(start_min);

        Gyroscope gyroscope = new Gyroscope();
        gyroscope.setSensitivity(sensitivity);

        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {

            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {

            }
        }.configset(this, sleep, gyroscope);
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

    private String convertSensitivity(int type) {
        String str = "";
        switch (type) {
            case CarairConstants.SENSITIVITY_HIGH:
                str = "高";
                break;
            case CarairConstants.SENSITIVITY_MIDDLE:
                str = "中";
                break;
            case CarairConstants.SENSITIVITY_LOW:
                str = "低";
                break;
            default:

        }
        return str;
    }

    private void refreshInfo() {
        if (getConfiggyroscope) {
            Gyroscope gyroscope = Util.getGyroscope(this);
            if (gyroscope != null) {
                sensitivity = gyroscope.getSensitivity();
            }
            gyroscopes.setText("陀螺仪灵敏度:" + convertSensitivity(sensitivity));
        }

        if (getConfigSleep) {
            Sleep_period sleep = Util.getSleepPeriod(this);
            if (sleep != null) {
                start_hour = sleep.getStart_hour();
                start_min = sleep.getStart_min();
                end_hour = sleep.getEnd_hour();
                end_min = sleep.getEnd_min();
            }
            btsleep_start.setText(start_hour + ":" + start_min);
            btsleep_end.setText(end_hour + ":" + end_min);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        refreshInfo();
    }

    @Override
    protected void onPause() {
        configSet();
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
