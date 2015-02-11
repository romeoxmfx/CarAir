
package com.android.carair.fragments;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.carair.R;
import com.android.carair.activities.CleanRatioActivity;
import com.android.carair.activities.CleanTimerActivity;
import com.android.carair.activities.CommonWebViewActivity;
import com.android.carair.activities.HistoryActivity;
import com.android.carair.activities.MainActivity;
import com.android.carair.activities.base.BaseActivity;
import com.android.carair.api.Air;
import com.android.carair.api.AppInfo;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.DevInfo;
import com.android.carair.api.Filter;
import com.android.carair.api.Loc;
import com.android.carair.api.Notice;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.common.CarAirManager;
import com.android.carair.common.CarairConstants;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.Log;
import com.android.carair.utils.Util;
import com.android.carair.views.MySwitch;
import com.android.carair.views.RoundProgressBar;
import com.android.carair.views.RoundProgressBarNormal;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.EmailHandler;
import com.umeng.socialize.sso.SmsHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class HomeFragment extends BaseFragment {
    View itemMain;
    View itemStrainer;
    ImageButton ibClean;
    ImageButton ibValue;
    ImageButton ibTimer;
    ImageButton ibData;
    ImageView switchBackground;
    RoundProgressBar rbOuter;
    RoundProgressBar rbInner;
    TextView mPrompt;
    TextView outText;
    TextView innerText;
    TextView cleanText;
    boolean isCleaning = false;
    Animation mAnimation;
    private Timer timer;
    private Timer syncTimer;
    private Timer syncWindTimer;
    boolean mIsConnection = false;
    ImageView ivBattery;
    ImageView ivCharging;
    ImageButton ivShare;
    TextView tvBattery;
    TextView tvWindValue;
    RelativeLayout llWind;
    TextView tvWindStrong;
    TextView tvWindWeak;
    TextView tvWindAuto;
    TextView tvInCar;
    TextView tvOutCar;
    TextView tvTemseq;
    TextView tvTem;
    FrameLayout flProgress;
    LinearLayout llTem;
    ImageView[] ivTabIndicators;
    TextView tvTemIn;
    TextView tvHumidity;
    TextView tvProgress;
    RoundProgressBarNormal mStrainerProgress;
    Button mStrainerReset;
    ImageView mStrainerAlertIcon;
    Filter mFilter;
    private LinearLayout llTabIndicator;
    private RelativeLayout rlPagerRoot;
    boolean windButonshow = false;
    boolean firstStart = false;
    boolean firstPushWind = false;
    private static final int MSG_CLOSE_SYNC_DIALOG = 0;
    private static final int MSG_BREAK_METER_INCAR = 1;
    private static final int MSG_BREAK_METER_OUTCAR = 2;
    private static final int MSG_FILTER_PROGRESS = 3;
    int syncCount = 20;
    int syncWindCount = 20;
    String lat;
    String lng;
    boolean charging;
    int currentBreakMeterColorInCarId;
    int currentBreakMeterColorOutCarId;
    int currentPM;
    int currentHarmful;
    boolean breakPMIng;
    boolean breakHarmfulIng;
    boolean timerSyncWindStart;
    boolean timerStart;
    boolean timerSyncStart;
    boolean nopower = false;

    public static int pmIn = 0;
    public static int pmOut = 0;
    public static int temIn = 0;
    public static int temOut = 0;

    int sleeping = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(R.layout.carair_home_fragment, null);
        init();
        // setState(mIsConnection, null);
        // query();
        return mMainView;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_CLOSE_SYNC_DIALOG) {
                String text = null;
                try {
                    text = (String) msg.obj;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(text)) {
                    Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                }
                flProgress.setVisibility(View.GONE);
                String prompt = (String) msg.obj;
                tvProgress.setText(prompt);
            } else if (msg.what == MSG_BREAK_METER_INCAR) {
                breakPMIng = true;
                if (currentBreakMeterColorInCarId == R.drawable.shape_car_progress_red) {
                    currentBreakMeterColorInCarId = R.drawable.shape_car_progress_purple;
                    tvInCar.setBackgroundResource(R.drawable.shape_car_progress_purple);
                } else {
                    currentBreakMeterColorInCarId = R.drawable.shape_car_progress_red;
                    tvInCar.setBackgroundResource(R.drawable.shape_car_progress_red);
                }
                if (currentPM < 500) {
                    breakPMIng = false;
                    return;
                }
                Message msg_break = mHandler.obtainMessage();
                msg_break.what = MSG_BREAK_METER_INCAR;
                sendMessageDelayed(msg_break, 600);
            } else if (msg.what == MSG_BREAK_METER_OUTCAR) {
                breakHarmfulIng = true;
                if (currentBreakMeterColorOutCarId == R.drawable.shape_car_progress_red) {
                    currentBreakMeterColorOutCarId = R.drawable.shape_car_progress_purple;
                    tvOutCar.setBackgroundResource(R.drawable.shape_car_progress_purple);
                } else {
                    currentBreakMeterColorOutCarId = R.drawable.shape_car_progress_red;
                    tvOutCar.setBackgroundResource(R.drawable.shape_car_progress_red);
                }
                if (currentHarmful < 500) {
                    breakHarmfulIng = false;
                    return;
                }
                Message msg_break = mHandler.obtainMessage();
                msg_break.what = MSG_BREAK_METER_OUTCAR;
                sendMessageDelayed(msg_break, 600);
            } else if (msg.what == MSG_FILTER_PROGRESS) {
                int progress = (int) msg.obj;
                mStrainerProgress.setProgress(progress);
            }
        }
    };
    private int currentState;
    private int currentWind;
    private int syncState;
    private UMSocialService mController;
    private ViewPager vpHome;
    private List<View> homeViewList;
    private HomeViewPagerAdapter homeAdapter;

    private void querySync() {
        syncCount--;
        if (syncCount == 0) {
            stopSyncTimer();
            syncCount = 20;
            startTimer();
            // tvProgress.setText("状态同步失败");
            Message msg = new Message();
            msg.what = MSG_CLOSE_SYNC_DIALOG;
            msg.obj = "状态同步失败";
            mHandler.sendMessageDelayed(msg, 200);
        }
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                if (packet.getRespMessage() != null) {
                    if (CarairConstants.CONN_ON.equals(packet.getRespMessage().getDevinfo()
                            .getConn())) {
                        int isOn = Util.statusToDevCtrl(Integer.parseInt(packet
                                .getRespMessage().getDevinfo().getStates()));
                        int status = Integer.parseInt(packet.getRespMessage().getDevinfo()
                                .getStates());
                        if (status > -1) {
                            Util.saveStatusHeader(status, HomeFragment.this.getActivity());
                        }
                        Log.i("当前同步状态 = " + syncState);
                        if (syncState == isOn) {
                            currentState = syncState;
                            Log.i("状态同步完成");
                            int wind = Util.decodeStatus(Integer.parseInt(packet
                                    .getRespMessage().getDevinfo().getStates()));
                            setWindValue(false, wind);
                            if (syncState == CarairConstants.OFF) {
                                Log.i("状态同步完成 ---> 关闭");
                                startCleanAnimation(false);
                            } else if (syncState == CarairConstants.ON) {
                                Log.i("状态同步完成 ---> 开启");
                                startCleanAnimation(true);
                            }
                            stopSyncTimer();
                            // tvProgress.setText("状态同步完成");
                            Message msg = new Message();
                            msg.obj = "状态同步完成";
                            syncCount = 20;
                            msg.what = MSG_CLOSE_SYNC_DIALOG;
                            mHandler.sendMessageDelayed(msg, 200);
                            startTimer();
                        }
                    }
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {

            }
        }.query(getActivity());
    }

    private void queryWindSync() {
        syncWindCount--;
        if (syncWindCount == 0) {
            stopSyncWindTimer();
            syncWindCount = 20;
            startTimer();
            // tvProgress.setText("状态同步失败");
            Message msg = new Message();
            msg.what = MSG_CLOSE_SYNC_DIALOG;
            msg.obj = "状态同步失败";
            mHandler.sendMessageDelayed(msg, 200);
        }
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                if (packet.getRespMessage() != null) {
                    if (CarairConstants.CONN_ON.equals(packet.getRespMessage().getDevinfo()
                            .getConn())) {
                        // int isOn =
                        // Util.statusToDevCtrl(Integer.parseInt(packet
                        // .getRespMessage().getDevinfo().getStates()));
                        int wind = Util.decodeStatus(Integer.parseInt(packet
                                .getRespMessage().getDevinfo().getStates()));
                        int status = Integer.parseInt(packet.getRespMessage().getDevinfo()
                                .getStates());
                        if (status > -1) {
                            Util.saveStatusHeader(status, HomeFragment.this.getActivity());
                        }
                        // setWindValue(false, wind);
                        if (currentWind == wind) {
                            setWindValue(false, wind);
                            stopSyncWindTimer();
                            // tvProgress.setText("状态同步完成");
                            Message msg = new Message();
                            msg.obj = "状态同步完成";
                            syncWindCount = 20;
                            msg.what = MSG_CLOSE_SYNC_DIALOG;
                            mHandler.sendMessageDelayed(msg, 200);
                            startTimer();
                            // if (currentState == CarairConstants.OFF) {
                            // startCleanAnimation(true);
                            // } else if (currentState == CarairConstants.ON) {
                            // startCleanAnimation(false);
                            // }
                        }
                    }
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {

            }
        }.query(getActivity());
    }

    private void query() {
        if (getActivity() == null) {
            return;
        }
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                try {
                    // mIsConnection = true;
                    // setState(mIsConnection, "宝宝可进");
                    // int pm = 500;
                    // int harmful = 500;
                    // currentPM = 500;
                    // currentHarmful = 500;
                    // // rbInner.setTextColor(Util.getPMColor(80));
                    // rbInner.setProgress(pm);
                    // setTextColor(true, pm);
                    // // rbOuter.setTextColor(Util.getPMColor(480));
                    // rbOuter.setProgress(harmful);
                    // setTextColor(false, harmful);
                    // ivBattery.setImageResource(getBatteryDrawableId(80));
                    // 修改状态
                    CarAirManager.getInstance().setState(MainActivity.STATE_OPEN);
                    // 保存loc
                    if (packet.getRespMessage() != null) {
                        if (CarairConstants.CONN_ON.equals(packet.getRespMessage().getDevinfo()
                                .getConn())) {
                            // 保存status
                            int status =
                                    Integer.parseInt(packet.getRespMessage().getDevinfo()
                                            .getStates());
                            if (status > -1) {
                                Util.saveStatusHeader(status,
                                        HomeFragment.this.getActivity());
                            }
                            int pm25 = (int) Float.parseFloat(packet.getRespMessage()
                                    .getDevinfo().getPm25());
                            pmIn = pm25;
                            currentPM = pm25;
                            // int opm25 = (int)
                            // Float.parseFloat(packet.getRespMessage().getAir()
                            // .getOpm25());
                            int harmairth = (int)
                                    Float.parseFloat(packet.getRespMessage()
                                            .getDevinfo().getHarmair());
                            currentHarmful = harmairth;
                            String notice =
                                    packet.getRespMessage().getAppinfo().getNotice();
                            // StringBuffer sb = new StringBuffer();
                            // sb.append(notice.getWin() + "," +
                            // notice.getBaby() + ","
                            // + notice.getPregn());
                            setState(true, notice, false);
                            int battery =
                                    Integer.parseInt(packet.getRespMessage().getDevinfo()
                                            .getBattery());
                            if (battery > 100) {
                                ivCharging.setVisibility(View.VISIBLE);
                                ibValue.setEnabled(true);
                                battery = battery - 100;
                                charging = true;
                                int wind = Util.decodeStatus(status);
                                if (wind > -1) {
                                    Util.saveRatio(wind, getActivity());
                                }
                                setWindValue(false, wind);
                            } else {
                                charging = false;
                                ivCharging.setVisibility(View.INVISIBLE);
                                ibValue.setEnabled(true);
                                Util.saveRatio(CarairConstants.RATIO_AUTO, getActivity());
                                setWindValue(false, CarairConstants.RATIO_AUTO);
                            }
                            ivBattery.setImageResource(getBatteryDrawableId(battery));
                            tvBattery.setText(battery + "");
                            // temperature
                            String temIn = packet.getRespMessage().getDevinfo().getTemper();
                            try {
                                HomeFragment.temIn = Integer.parseInt(temIn);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String humi = packet.getRespMessage().getDevinfo().getHumi();
                            // humi = "";
                            if (!TextUtils.isEmpty(temIn)) {
                                // tvTemseq.setVisibility(View.VISIBLE);
                                // tvTem.setVisibility(View.VISIBLE);
                                tvTemIn.setText(temIn);
                            }
                            if (TextUtils.isEmpty(humi) || "0".equals(humi)) {
                                // llTem.setVisibility(View.INVISIBLE);
                                tvTemseq.setVisibility(View.GONE);
                                tvTem.setVisibility(View.GONE);
                                tvHumidity.setVisibility(View.GONE);
                            } else {
                                tvHumidity.setText(humi);
                                tvHumidity.setVisibility(View.VISIBLE);
                                tvTemseq.setVisibility(View.VISIBLE);
                                tvTem.setVisibility(View.VISIBLE);
                            }
                            rbInner.setProgress(pm25);
                            // rbInner.setTextColor(Util.getPMColor(pm25));
                            setTextColor(true, pm25);
                            rbOuter.setProgress(harmairth);
                            setTextColor(false, harmairth);
                            // rbOuter.setTextColor(Util.getPMColor(harmairth));

                            int isOn = Util.statusToDevCtrl(Integer.parseInt(packet
                                    .getRespMessage().getDevinfo().getStates()));
                            sleeping = Util.decodeSleep(Integer.parseInt(packet.getRespMessage()
                                    .getDevinfo().getStates()));
                            if (isOn == 1) {
                                currentState = CarairConstants.ON;
                                startCleanAnimation(true);
                            } else {
                                currentState = CarairConstants.OFF;
                                startCleanAnimation(false);
                                tvWindValue.setText("");
                            }
                            String shareStr = String
                                    .format("车内空气状况速报：颗粒物浓度 %s, 有害气体浓度 %s, 温度 %s, 湿度 %s; 温馨提示：%s \n更多详情：http://www.sumcreate.com \n#AirStory智能车载净化器#"
                                            , pm25, harmairth, temIn, humi, notice);
                            mController
                                    .setShareContent(shareStr);
                            nopower = false;
                        } else if (CarairConstants.CONN_OFF_NOPOWER.equals(packet.getRespMessage()
                                .getDevinfo()
                                .getConn())) {
                            nopower = true;
                            setState(false, null, true);
                            currentPM = 0;
                            currentHarmful = 0;
                        } else {
                            nopower = false;
                            setState(false, null, false);
                            currentPM = 0;
                            currentHarmful = 0;
                        }
                        Loc loc = packet.getRespMessage().getLoc();
                        String lat = packet.getRespMessage().getDevinfo().getLat();
                        String lng = packet.getRespMessage().getDevinfo().getLng();
                        Log.i("lat = %s,lng = %s", lat, lng);
                        loc.setLat(Double.parseDouble(lat));
                        loc.setLng(Double.parseDouble(lng));
                        if (loc != null) {
                            Util.saveLoc(loc, getActivity());
                        }
                        Util.saveLocation(getActivity(), lat, lng);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    currentPM = 0;
                    currentHarmful = 0;
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {
                setState(false, null, false);
                currentPM = 0;
                currentHarmful = 0;
                Util.clearLocation();
            }
        }.query(getActivity());
    }

    public void setTextColor(boolean inCar, int progress) {
        RoundProgressBar rb;
        TextView tv;
        if (inCar) {
            rb = rbInner;
            tv = tvInCar;
        } else {
            rb = rbOuter;
            tv = tvOutCar;
        }
        if (progress > 0 && progress <= 43) {
            rb.setTextColor(Color.WHITE);
            tv.setBackgroundResource(R.drawable.shape_car_progress_green);
            tv.setText("优");
        } else if (progress > 43 && progress <= 94) {
            rb.setTextColor(Color.rgb(0xb1, 0xf6, 0xff));
            tv.setBackgroundResource(R.drawable.shape_car_progress_green);
            tv.setText("良");
        } else if (progress > 94 && progress <= 149) {
            rb.setTextColor(Color.rgb(0x7a, 0xf6, 0xff));
            tv.setBackgroundResource(R.drawable.shape_car_progress_bluegreen);
            tv.setText("轻度污染");
        } else if (progress > 149 && progress <= 200) {
            rb.setTextColor(Color.rgb(0xfd, 0xd8, 0xd9));
            tv.setBackgroundResource(R.drawable.shape_car_progress_pink);
            tv.setText("中度污染");
        } else if (progress > 200 && progress <= 300) {
            rb.setTextColor(Color.rgb(0xff, 0x7e, 0x82));
            tv.setBackgroundResource(R.drawable.shape_car_progress_red);
            tv.setText("重度污染");
        } else if (progress > 300 && progress < 500) {
            rb.setTextColor(Color.rgb(0xb1, 0x49, 0x7c));
            tv.setBackgroundResource(R.drawable.shape_car_progress_purple);
            tv.setText("严重污染");
        } else if (progress >= 500) {
            if (inCar && !breakPMIng) {
                rb.setTextColor(Color.rgb(0xb1, 0x49, 0x7c));
                tv.setText("严重污染");
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_BREAK_METER_INCAR;
                mHandler.sendMessage(msg);
            } else if (!inCar && !breakHarmfulIng) {
                rb.setTextColor(Color.rgb(0xb1, 0x49, 0x7c));
                tv.setText("严重污染");
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_BREAK_METER_OUTCAR;
                msg.obj = inCar;
                mHandler.sendMessage(msg);
            }
        }
        else {
            tv.setBackgroundResource(R.drawable.shape_car_progress_green);
            tv.setText("未知");
            rb.setTextColor(Color.WHITE);
        }
    }

    private int getBatteryDrawableId(int battery) {
        if (battery > 0 && battery <= 20) {
            return R.drawable.battery_1;
        } else if (battery > 20 && battery <= 40) {
            return R.drawable.battery_2;
        } else if (battery > 40 && battery <= 60) {
            return R.drawable.battery_3;
        } else if (battery > 60 && battery <= 80) {
            return R.drawable.battery_4;
        } else if (battery > 80 && battery <= 100) {
            return R.drawable.battery_5;
        } else {
            return R.drawable.battery;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // stopTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimer();
    }

    public void stopTimer() {
        if (timer != null) {
            timerStart = false;
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /**
     * 普通状态查询
     */
    public void startTimer() {
        if (timerStart) {
            return;
        }
        timerStart = true;
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                query();
            }
        }, 0, 1000 * 10);
    }

    public void startSyncTimer() {
        if (timerSyncStart) {
            return;
        }
        syncTimer = new Timer();
        syncTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                querySync();
            }
        }, 0, 1000);
    }

    public void stopSyncWindTimer() {
        if (syncWindTimer != null) {
            timerSyncWindStart = false;
            syncWindTimer.cancel();
            syncWindTimer.purge();
            syncWindTimer = null;
        }
    }

    public void startSyncWindTimer() {
        if (timerSyncWindStart) {
            return;
        }
        syncWindTimer = new Timer();
        syncWindTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                queryWindSync();
            }
        }, 0, 1000);
    }

    public void stopSyncTimer() {
        if (syncTimer != null) {
            timerSyncStart = false;
            syncTimer.cancel();
            syncTimer.purge();
            syncTimer = null;
        }
    }

    private void setState(boolean isconnection, String message, boolean nopower) {
        if (getActivity() == null)
            return;
        if (isconnection) {
            mIsConnection = true;
            CarAirManager.getInstance().setmConnection(true);
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("  净化器已连接");
            mPrompt.setText(message);
            if (TextUtils.isEmpty(message)) {
                mPrompt.setVisibility(View.INVISIBLE);
            } else {
                mPrompt.setVisibility(View.VISIBLE);
            }
            // mPrompt.setBackgroundResource(R.drawable.shape_prompt);
            // llTem.setVisibility(View.VISIBLE);
            outText.setVisibility(View.VISIBLE);
            ivBattery.setVisibility(View.VISIBLE);
            innerText.setVisibility(View.VISIBLE);
            ibValue.setVisibility(View.VISIBLE);
            ibTimer.setVisibility(View.VISIBLE);
            ibData.setVisibility(View.VISIBLE);
            ibClean.setEnabled(true);
            ibValue.setEnabled(true);
            switchBackground.setBackgroundResource(R.drawable.switch_bg);
        } else {
            mIsConnection = false;
            CarAirManager.getInstance().setmConnection(false);
            if (nopower) {
                mPrompt.setText("请充电");
            } else {
                mPrompt.setText("请确保净化器处于有信号的区域");
            }
            if (!firstStart) {
                if (nopower) {
                    Toast.makeText(getActivity(), "净化器电量耗尽，请确保净化器处于有电状态后再操作", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getActivity(), "净化器未连接，请确保净化器处于有信号的地区以后再操作", Toast.LENGTH_SHORT)
                            .show();
                }
                firstStart = true;
            }
            if (nopower) {
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("  净化器电量耗尽");
            } else {
                ((MainActivity) getActivity()).getSupportActionBar().setTitle("  净化器未连接");
            }
            // mPrompt.setBackgroundResource(android.R.color.transparent);
            // mPrompt.setVisibility(View.INVISIBLE);
            // outText.setVisibility(View.INVISIBLE);
            // innerText.setVisibility(View.INVISIBLE);
            // llTem.setVisibility(View.INVISIBLE);
            rbOuter.setProgress(0);
            rbInner.setProgress(0);
            ivCharging.setVisibility(View.INVISIBLE);
            tvInCar.setText("无");
            tvOutCar.setText("无");
            // ivBattery.setVisibility(View.INVISIBLE);
            // ibValue.setVisibility(View.INVISIBLE);
            // ibTimer.setVisibility(View.INVISIBLE);
            cleanText.setText("净化");
            // ibData.setVisibility(View.INVISIBLE);
            ibClean.setEnabled(true);
            ibValue.setEnabled(true);
            tvWindValue.setText("");
            switchBackground.setBackgroundResource(R.drawable.not_connected_switch_bg);

        }
    }

    private void init() {
        if (getActivity() != null) {
            mController = UMServiceFactory.getUMSocialService("com.umeng.share");
            mController
                    .setShareContent("国内首个智能车载净化器，支持通过App实时监测车内空气状况，远程遥控净化器状态，诚心之作，火爆销售中，详情请见：http://www.sumcreate.com \n#AirStory智能车载净化器#");
            mController.setAppWebSite(SHARE_MEDIA.WEIXIN, "http://www.sumcreate.com");
            mController.setAppWebSite(SHARE_MEDIA.WEIXIN_CIRCLE, "http://www.sumcreate.com");
            mController.setAppWebSite(SHARE_MEDIA.SINA, "http://www.sumcreate.com");
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
            mController.setShareImage(new UMImage(getActivity(), bitmap));
            mController.getConfig().removePlatform(SHARE_MEDIA.RENREN, SHARE_MEDIA.DOUBAN,
                    SHARE_MEDIA.TENCENT);
            String appId = "wx38ef5294614fce3a";
            // 添加微信平台
            UMWXHandler wxHandler = new UMWXHandler(getActivity(), appId);
            wxHandler.addToSocialSDK();
            // 设置微信好友分享内容
            WeiXinShareContent weixinContent = new WeiXinShareContent();
            // 设置分享文字
            weixinContent
                    .setShareContent("国内首个智能车载净化器，支持通过App实时监测车内空气状况，远程遥控净化器状态，诚心之作，火爆销售中，详情请见：http://www.sumcreate.com \n#AirStory智能车载净化器#");
            // 设置title
            weixinContent.setTitle("Air Story智能车载空气净化器");
            // 设置分享内容跳转URL
            weixinContent.setTargetUrl("http://www.sumcreate.com");
            mController.setShareMedia(weixinContent);
            // 支持微信朋友圈
            UMWXHandler wxCircleHandler = new UMWXHandler(getActivity(), appId);
            wxCircleHandler.setToCircle(true);
            wxCircleHandler.addToSocialSDK();
            // 设置微信朋友圈分享内容
            CircleShareContent circleMedia = new CircleShareContent();
            circleMedia
                    .setShareContent("国内首个智能车载净化器，支持通过App实时监测车内空气状况，远程遥控净化器状态，诚心之作，火爆销售中，详情请见：http://www.sumcreate.com \n#AirStory智能车载净化器#");
            // 设置朋友圈title
            circleMedia.setTitle("Air Story智能车载空气净化器");
            circleMedia.setTargetUrl("http://www.sumcreate.com");
            mController.setShareMedia(circleMedia);
            // 添加短信
            SmsHandler smsHandler = new SmsHandler();
            smsHandler.addToSocialSDK();
            // 添加email
            EmailHandler emailHandler = new EmailHandler();
            emailHandler.addToSocialSDK();
        }

        initPager();

        ibClean = (ImageButton) itemMain.findViewById(R.id.ibClean);
        ibValue = (ImageButton) itemMain.findViewById(R.id.ibValue);
        ibTimer = (ImageButton) itemMain.findViewById(R.id.ibTimer);
        ibData = (ImageButton) itemMain.findViewById(R.id.ibData);
        rbOuter = (RoundProgressBar) itemMain.findViewById(R.id.outCarProgress);
        rbInner = (RoundProgressBar) itemMain.findViewById(R.id.inCarProgress);
        mPrompt = (TextView) itemMain.findViewById(R.id.promptText);
        outText = (TextView) itemMain.findViewById(R.id.tvOutCar);
        innerText = (TextView) itemMain.findViewById(R.id.tvInCar);
        cleanText = (TextView) itemMain.findViewById(R.id.cleanText);
        ivBattery = (ImageView) itemMain.findViewById(R.id.ivBattery);
        ivCharging = (ImageView) itemMain.findViewById(R.id.ivCharging);
        llWind = (RelativeLayout) itemMain.findViewById(R.id.llWind);
        tvWindAuto = (TextView) itemMain.findViewById(R.id.windAuto);
        tvWindStrong = (TextView) itemMain.findViewById(R.id.windStrong);
        tvWindWeak = (TextView) itemMain.findViewById(R.id.windWeak);
        tvWindValue = (TextView) itemMain.findViewById(R.id.windValue);
        tvInCar = (TextView) itemMain.findViewById(R.id.tvInCarPrompt);
        tvOutCar = (TextView) itemMain.findViewById(R.id.tvOutCarPrompt);
        flProgress = (FrameLayout) itemMain.findViewById(R.id.flProgress);
        tvProgress = (TextView) itemMain.findViewById(R.id.tv_progress_sync);
        tvBattery = (TextView) itemMain.findViewById(R.id.tvTextBattery);
        llTem = (LinearLayout) itemMain.findViewById(R.id.llTem);
        tvTem = (TextView) itemMain.findViewById(R.id.tvTem);
        tvTemIn = (TextView) itemMain.findViewById(R.id.tvTemIn);
        tvHumidity = (TextView) itemMain.findViewById(R.id.tvHumidity);
        tvTemseq = (TextView) itemMain.findViewById(R.id.temseq);
        ivShare = (ImageButton) itemMain.findViewById(R.id.ibShare);
        ivShare.setOnClickListener(this);
        AppInfo info = Util.getFeature(this.getActivity());
        if (info != null) {
            if (CarairConstants.OFF == info.getHas_share()) {
                ivShare.setVisibility(View.INVISIBLE);
            }
        }
        // setWindValue(true);
        llWind.setOnClickListener(this);
        tvWindAuto.setOnClickListener(this);
        tvWindStrong.setOnClickListener(this);
        tvWindWeak.setOnClickListener(this);
        rbOuter.setMax(500);
        rbInner.setMax(500);
        switchBackground = (ImageView) itemMain.findViewById(R.id.switchBackground);
        mAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_ani);
        LinearInterpolator lin = new LinearInterpolator();
        mAnimation.setInterpolator(lin);
        mAnimation.setFillEnabled(true);
        mAnimation.setFillAfter(true);
        mAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }
        });

        ibClean.setOnClickListener(this);
        ibValue.setOnClickListener(this);
        ibTimer.setOnClickListener(this);
        ibData.setOnClickListener(this);

        initStrainer();
    }

    private void initStrainer() {
        mStrainerAlertIcon = (ImageView) itemStrainer.findViewById(R.id.strainerAlertIcon);
        mStrainerProgress = (RoundProgressBarNormal) itemStrainer
                .findViewById(R.id.rdStrainerProgress);
        mStrainerReset = (Button) itemStrainer.findViewById(R.id.btStrainerReset);
        mStrainerReset.setOnClickListener(this);
        mStrainerAlertIcon.setOnClickListener(this);
        mStrainerProgress.setProgress(0);
        requestConfig(false);
    }

    private void initPager() {
        vpHome = (ViewPager) mMainView.findViewById(R.id.vp_home);
        vpHome.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                if (arg0 == 1) {
                    fillFilter();
                }
                if (ivTabIndicators != null && ivTabIndicators.length > 0
                        && ivTabIndicators.length > arg0) {
                    for (int i = 0; i < ivTabIndicators.length; i++) {
                        ImageView iv = ivTabIndicators[i];
                        if (arg0 == i) {
                            iv.setBackgroundResource(R.drawable.shape_tab_indicator_white);
                        } else {
                            iv.setBackgroundResource(R.drawable.shape_tab_indicator_darkgray);
                        }
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
        rlPagerRoot = (RelativeLayout) mMainView.findViewById(R.id.rlRoot);
        llTabIndicator = (LinearLayout) mMainView.findViewById(R.id.llTabIndicator);
        homeViewList = new ArrayList<View>();
        itemMain = LayoutInflater.from(getActivity()).inflate(R.layout.carair_home_item_main, null);
        itemStrainer = LayoutInflater.from(getActivity()).inflate(
                R.layout.carair_home_item_strainer, null);
        homeViewList.add(itemMain);
        homeViewList.add(itemStrainer);
        homeAdapter = new HomeViewPagerAdapter(homeViewList);
        ImageView iv = null;
        ivTabIndicators = new ImageView[homeViewList.size()];
        for (int i = 0; i < homeViewList.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Util.dip2px(
                    getActivity(), 8), Util.dip2px(getActivity(), 8));
            params.setMargins(25, 0, 0, 0);
            iv = new ImageView(getActivity());
            ivTabIndicators[i] = iv;
            // iv.setLayoutParams(new LayoutParams(Util.dip2px(getActivity(),
            // 15), Util.dip2px(getActivity(), 15)));
            if (i == 0) {
                iv.setBackgroundResource(R.drawable.shape_tab_indicator_white);
            } else {
                iv.setBackgroundResource(R.drawable.shape_tab_indicator_darkgray);
            }
            llTabIndicator.addView(iv, params);
        }
        vpHome.setAdapter(homeAdapter);
    }

    private void fillFilter() {
        if (mFilter != null) {
            float precent = mFilter.getLeft_precent();
            BigDecimal b = new BigDecimal(precent);
            float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            // b.setScale(2, BigDecimal.ROUND_HALF_UP) 表明四舍五入，保留两位小数
            float f = f1 * 100;
            final int k = (int) f;
            // int intPrecent = (int)precent * 100;
            // mStrainerProgress.setTextSize(30);
            if (k <= 5) {
                mStrainerProgress.setTextColor(Color.rgb(177, 73, 124));
                mStrainerProgress.setCricleProgressColor(Color.rgb(177, 73, 124));
            } else if (k > 5 && k <= 40) {
                mStrainerProgress.setTextColor(Color.rgb(255, 255, 00));
                mStrainerProgress.setCricleProgressColor(Color.rgb(255, 255, 00));
            } else if (k > 40 && k <= 100) {
                mStrainerProgress.setTextColor(Color.rgb(51, 189, 11));
                mStrainerProgress.setCricleProgressColor(Color.rgb(51, 189, 11));
            }
            mStrainerProgress.setTextTip("请" + mFilter.getLeft_day() + "天后更换");
            new Thread(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i <= k; i++) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = MSG_FILTER_PROGRESS;
                        msg.obj = i;
                        mHandler.sendMessage(msg);
                        try {
                            Thread.sleep(16);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

            // mStrainerProgress.setProgress((int)mFilter.getLeft_precent());
            // mStrainerProgress.setTextAlignment(textAlignment);
        }
    }

    private void setWindValue(boolean auto, int... value) {
        int windValue;
        if (auto) {
            // devctrl(true);
            windValue = Util.getRatio(getActivity());
        } else {
            windValue = value[0];
        }
        switch (windValue) {
            case CarairConstants.RATIO_AUTO:
                tvWindValue.setText("自动");
                tvWindAuto.setBackgroundResource(R.drawable.shape_prompt);
                tvWindStrong.setBackgroundDrawable(null);
                tvWindWeak.setBackgroundDrawable(null);
                break;
            case CarairConstants.RATIO_HIGH:
                tvWindValue.setText("强风");
                tvWindStrong.setBackgroundResource(R.drawable.shape_prompt);
                tvWindWeak.setBackgroundDrawable(null);
                tvWindAuto.setBackgroundDrawable(null);
                break;
            case CarairConstants.RATIO_LOW:
                tvWindValue.setText("弱风");
                tvWindWeak.setBackgroundResource(R.drawable.shape_prompt);
                tvWindStrong.setBackgroundDrawable(null);
                tvWindAuto.setBackgroundDrawable(null);
                break;
            default:
                tvWindValue.setText("");
                break;
        }
    }

    private void requestConfig(final boolean refresh) {
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                if (packet != null && "0".equals(packet.getStatus())) {
                    if (packet.getRespMessage() != null
                            && packet.getRespMessage().getDevinfo() != null) {
                        mFilter = packet.getRespMessage().getDevinfo().getFilter();
                        if(refresh){
                            fillFilter();
                        }
                    }
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {

            }
        }.config(getActivity());
    }

    private void filterReSet() {
        new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_dialog_info).setTitle("重置滤网").setMessage("是否重置滤网?").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Filter filter = new Filter();
                filter.setReset_filter(1);
                new CarAirReqTask() {

                    @Override
                    public void onCompleteSucceed(RespProtocolPacket packet) {
                        if(packet != null && !"2".equals(packet.getStatus())){
                            Toast.makeText(getActivity(), "重置成功", 1).show();
                            requestConfig(true);
                        }else{
                            Toast.makeText(getActivity(), "重置失败，请重试!", 1).show();
                        }
                    }

                    @Override
                    public void onCompleteFailed(int type, HttpErrorBean error) {
                        Toast.makeText(getActivity(), "重置失败，请重试!", 1).show();
                    }
                }.configset(getActivity(), filter);
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void startCleanAnimation(boolean start) {
        if (start) {
            if (!isCleaning) {
                switchBackground.startAnimation(mAnimation);
            }
            isCleaning = true;
            cleanText.setText("净化中");
        } else {
            switchBackground.clearAnimation();
            cleanText.setText("净化");
            isCleaning = false;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        switch (id) {
            case R.id.ibClean:
                if (sleeping == 1) {
                    Toast.makeText(getActivity(), "净化器已休眠，请确保净化器处于非休眠状态再操作", 1).show();
                    return;
                }
                if (!mIsConnection) {
                    if (nopower) {
                        Toast.makeText(getActivity(), "净化器电量用尽，请确保净化器处于有电状态后再操作", 1).show();
                    } else {
                        Toast.makeText(getActivity(), "净化器未连接，请确保净化器处于有信号的地区以后再操作", 1).show();
                    }
                    return;
                }
                if (isCleaning) {
                    // Toast.makeText(getActivity(), "正在关闭净化功能",
                    // Toast.LENGTH_SHORT).show();
                    flProgress.setVisibility(View.VISIBLE);
                    tvProgress.setText("指令发送中...");
                    devctrl(false);
                    // startCleanAnimation(false);
                } else {
                    flProgress.setVisibility(View.VISIBLE);
                    tvProgress.setText("指令发送中...");
                    // Toast.makeText(getActivity(), "正在开启净化功能",
                    // Toast.LENGTH_SHORT).show();
                    devctrl(true);
                    // startCleanAnimation(true);
                }
                break;
            case R.id.ibValue:
                if (sleeping == 1) {
                    Toast.makeText(getActivity(), "净化器已休眠，请确保净化器处于非休眠状态再操作", 1).show();
                    return;
                }
                if (!mIsConnection) {
                    if (nopower) {
                        Toast.makeText(getActivity(), "净化器电量用尽，请确保净化器处于有电状态后再操作", 1).show();
                    } else {
                        Toast.makeText(getActivity(), "净化器未连接，请确保净化器处于有信号的地区以后再操作", 1).show();
                    }
                    return;
                }
                if (CarairConstants.OFF == currentState) {
                    Toast.makeText(getActivity(), "净化器已关闭，请确保净化器处于开启状态后再操作", 1).show();
                    return;
                }
                if (charging) {
                    startWindControl();
                } else {
                    Toast.makeText(getActivity(), "为了保护净化器电池，请确保净化器处于充电状态在操作风力", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case R.id.ibTimer:
                Intent i1 = new Intent(getActivity(), CleanTimerActivity.class);
                getActivity().startActivity(i1);
                break;
            case R.id.ibData:
                // 打开历史
                Intent intent = new Intent();
                intent.putExtra("type", Item.ITEM_IN_CAR);
                intent.setClass(getActivity(), HistoryActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.windAuto:
                // Toast.makeText(getActivity(), "自动", 1).show();
                // Util.saveRatio(CarairConstants.RATIO_AUTO, getActivity());
                flProgress.setVisibility(View.VISIBLE);
                tvProgress.setText("指令发送中...");
                devWindCtrl(CarairConstants.RATIO_AUTO);
                startWindControl();
                break;
            case R.id.windStrong:
                // Toast.makeText(getActivity(), "强风", 1).show();
                // Util.saveRatio(CarairConstants.RATIO_HIGH, getActivity());
                flProgress.setVisibility(View.VISIBLE);
                tvProgress.setText("指令发送中...");
                devWindCtrl(CarairConstants.RATIO_HIGH);
                startWindControl();
                break;
            case R.id.windWeak:
                // Toast.makeText(getActivity(), "弱风", 1).show();
                // Util.saveRatio(CarairConstants.RATIO_LOW, getActivity());
                flProgress.setVisibility(View.VISIBLE);
                tvProgress.setText("指令发送中...");
                devWindCtrl(CarairConstants.RATIO_LOW);
                startWindControl();
                break;
            case R.id.llWind:

                break;
            case R.id.ibShare:
                // 是否只有已登录用户才能打开分享选择页
                mController.openShare(getActivity(), false);
                break;
            case R.id.btStrainerReset:
                filterReSet();
                break;
            case R.id.strainerAlertIcon:
                Intent iStrainerAlert = new Intent();
                iStrainerAlert.setClass(getActivity(), CommonWebViewActivity.class);
                String url = "http://www.sumcreate.com/qa/filter.html";
                AppInfo info = Util.getFeature(this.getActivity());
                if(info != null){
                    url = info.getUsage_url();
                }
                iStrainerAlert.putExtra("url", url);
                iStrainerAlert.putExtra("title", "滤网说明");
                getActivity().startActivity(iStrainerAlert);
                break;
            default:
                break;
        }
    }

    private void startWindControl() {
        if (!windButonshow) {
            windButonshow = true;
            // if (!firstPushWind) {
            // Toast.makeText(getActivity(), "为了保护净化器电池，请确保净化器处于充电状态在操作风力",
            // Toast.LENGTH_SHORT)
            // .show();
            // firstPushWind = true;
            // }
            tvWindValue.setVisibility(View.GONE);
            llWind.setVisibility(View.VISIBLE);
            final RelativeLayout.LayoutParams params = (LayoutParams) tvWindStrong
                    .getLayoutParams();
            ValueAnimator windOut = ValueAnimator.ofInt(params.topMargin,
                    Util.Dp2Px(getActivity(), 4));
            windOut.addUpdateListener(new AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator arg0) {
                    int i = (Integer) arg0.getAnimatedValue();
                    params.topMargin = i;
                    tvWindStrong.setLayoutParams(params);
                }
            });
            windOut.setDuration(500);
            windOut.start();

            final RelativeLayout.LayoutParams pauto = (LayoutParams) tvWindAuto
                    .getLayoutParams();
            ValueAnimator windautoOut = ValueAnimator.ofInt(pauto.topMargin,
                    Util.Dp2Px(getActivity(), 35));
            windautoOut.addUpdateListener(new AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator arg0) {
                    int i = (Integer) arg0.getAnimatedValue();
                    pauto.topMargin = i;
                    tvWindAuto.setLayoutParams(pauto);
                }
            });
            windautoOut.setDuration(500);
            windautoOut.start();

            final RelativeLayout.LayoutParams pweak = (LayoutParams) tvWindWeak
                    .getLayoutParams();
            ValueAnimator windweakOut = ValueAnimator.ofInt(pweak.topMargin,
                    Util.Dp2Px(getActivity(), 66));
            windweakOut.addUpdateListener(new AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator arg0) {
                    int i = (Integer) arg0.getAnimatedValue();
                    pweak.topMargin = i;
                    tvWindWeak.setLayoutParams(pweak);
                }
            });
            windweakOut.setDuration(500);
            windweakOut.start();

            final RelativeLayout.LayoutParams pvalue = (LayoutParams) ibValue
                    .getLayoutParams();
            ValueAnimator windButtonOut = ValueAnimator.ofInt(pvalue.topMargin,
                    Util.Dp2Px(getActivity(), 100));
            windButtonOut.addUpdateListener(new AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator arg0) {
                    int i = (Integer) arg0.getAnimatedValue();
                    pvalue.topMargin = i;
                    ibValue.setLayoutParams(pvalue);
                }
            });
            windButtonOut.setDuration(500);
            windButtonOut.start();

            // Animation aniwindbuttonout =
            // AnimationUtils.loadAnimation(getActivity(),
            // R.anim.wind_button_out);
            // aniwindbuttonout.setFillAfter(true);
            // ibValue.startAnimation(aniwindbuttonout);

        } else {
            windButonshow = false;
            llWind.setVisibility(View.VISIBLE);
            final RelativeLayout.LayoutParams params = (LayoutParams) tvWindStrong
                    .getLayoutParams();
            ValueAnimator windOut = ValueAnimator.ofInt(params.topMargin,
                    Util.Dp2Px(getActivity(), 66));
            windOut.addUpdateListener(new AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator arg0) {
                    int i = (Integer) arg0.getAnimatedValue();
                    params.topMargin = i;
                    tvWindStrong.setLayoutParams(params);
                }
            });

            final RelativeLayout.LayoutParams paramsAuto = (LayoutParams) tvWindAuto
                    .getLayoutParams();
            ValueAnimator windOutAuto = ValueAnimator.ofInt(paramsAuto.topMargin,
                    Util.Dp2Px(getActivity(), 66));
            windOutAuto.addUpdateListener(new AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator arg0) {
                    int i = (Integer) arg0.getAnimatedValue();
                    paramsAuto.topMargin = i;
                    tvWindAuto.setLayoutParams(paramsAuto);
                }
            });

            windOutAuto.setDuration(500);
            windOutAuto.start();

            windOut.addListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator arg0) {
                    // setWindValue(true);
                }

                @Override
                public void onAnimationRepeat(Animator arg0) {

                }

                @Override
                public void onAnimationEnd(Animator arg0) {
                    tvWindValue.setVisibility(View.VISIBLE);
                    llWind.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator arg0) {

                }
            });
            windOut.setDuration(500);
            windOut.start();

            final RelativeLayout.LayoutParams pvalue = (LayoutParams) ibValue
                    .getLayoutParams();
            ValueAnimator windButtonOut = ValueAnimator.ofInt(pvalue.topMargin,
                    Util.Dp2Px(getActivity(), 90));
            windButtonOut.addUpdateListener(new AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator arg0) {
                    int i = (Integer) arg0.getAnimatedValue();
                    pvalue.topMargin = i;
                    ibValue.setLayoutParams(pvalue);
                }
            });
            windButtonOut.setDuration(500);
            windButtonOut.start();

        }
    }

    private void syncState(boolean ison) {
        flProgress.setVisibility(View.VISIBLE);
        tvProgress.setText("状态同步中...");
        if (ison) {
            syncState = CarairConstants.ON;
            // 休眠位开启
            // startSyncTimer();
        } else {
            // 休眠位关闭
            syncState = CarairConstants.OFF;
            // startSyncTimer(CarairConstants.ON);
        }
        startSyncTimer();
    }

    private void syncWindState(int wind) {
        flProgress.setVisibility(View.VISIBLE);
        tvProgress.setText("状态同步中...");
        // if (ison) {
        // currentState = CarairConstants.ON;
        // // 休眠位开启
        // // startSyncTimer();
        // } else {
        // // 休眠位关闭
        // currentState = CarairConstants.OFF;
        // // startSyncTimer(CarairConstants.ON);
        // }
        currentWind = wind;
        startSyncWindTimer();
    }

    private void devWindCtrl(final int wind) {
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                // Toast.makeText(getActivity(), "操作成功",
                // Toast.LENGTH_SHORT).show();
                String state = packet.getStatus();
                if ("0".equals(state) || "1".equals(state)) {
                    tvProgress.setText("指令发送成功...");
                    syncWindState(wind);
                } else {
                    tvProgress.setText("指令发送失败...");
                    Toast.makeText(getActivity(), "指令发送失败", Toast.LENGTH_SHORT).show();
                    flProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {
                tvProgress.setText("指令发送失败...");
                flProgress.setVisibility(View.GONE);
                // Toast.makeText(getActivity(), "操作失败",
                // Toast.LENGTH_SHORT).show();
            }
        }.devWindCtrl(getActivity(), wind, CarairConstants.ON == currentState ? true : false);
    }

    private void devctrl(final boolean ison) {
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                // Toast.makeText(getActivity(), "操作成功",
                // Toast.LENGTH_SHORT).show();
                String state = packet.getStatus();
                if ("0".equals(state) || "1".equals(state)) {
                    tvProgress.setText("指令发送成功...");
                    syncState(ison);
                } else {
                    tvProgress.setText("指令发送失败...");
                    Toast.makeText(getActivity(), "指令发送失败", Toast.LENGTH_SHORT).show();
                    flProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {
                tvProgress.setText("指令发送失败...");
                flProgress.setVisibility(View.GONE);
                // Toast.makeText(getActivity(), "操作失败",
                // Toast.LENGTH_SHORT).show();
            }
        }.devctrl(getActivity(), ison, !charging);
    }

    public class HomeViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public HomeViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;// 构造方法，参数是我们的页卡，这样比较方便。
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));// 删除页卡
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) { // 这个方法用来实例化页卡
            container.addView(mListViews.get(position), 0);// 添加页卡
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();// 返回页卡的数量
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;// 官方提示这样写
        }
    }

}
