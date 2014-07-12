
package com.android.carair.fragments;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
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
import com.android.carair.activities.HistoryActivity;
import com.android.carair.activities.MainActivity;
import com.android.carair.activities.base.BaseActivity;
import com.android.carair.api.Air;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.DevInfo;
import com.android.carair.api.Loc;
import com.android.carair.api.Notice;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.common.CarairConstants;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.Util;
import com.android.carair.views.MySwitch;
import com.android.carair.views.RoundProgressBar;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class HomeFragment extends BaseFragment {
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
    boolean mIsConnection = false;
    ImageView ivBattery;
    ImageView ivCharging;
    TextView tvWindValue;
    RelativeLayout llWind;
    TextView tvWindStrong;
    TextView tvWindWeak;
    TextView tvWindAuto;
    TextView tvInCar;
    TextView tvOutCar;
    FrameLayout flProgress;
    TextView tvProgress;
    boolean windButonshow = false;
    boolean firstStart = false;
    boolean firstPushWind = false;
    private static final int MSG_CLOSE_SYNC_DIALOG = 0;
    int syncCount = 20;
    String lat;
    String lng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(R.layout.carair_home_fragment, null);
        init();
        setState(mIsConnection, null);
        // query();
        return mMainView;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_CLOSE_SYNC_DIALOG) {
                flProgress.setVisibility(View.GONE);
            }
        }
    };

    private void querySync(final int state) {
        syncCount--;
        if (syncCount == 0) {
            stopSyncTimer();
            syncCount = 20;
            startTimer();
            tvProgress.setText("状态同步失败");
            Message msg = new Message();
            msg.what = MSG_CLOSE_SYNC_DIALOG;
            mHandler.sendMessageDelayed(msg, 500);
        }
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                if (packet.getRespMessage() != null) {
                    if (CarairConstants.CONN_ON.equals(packet.getRespMessage().getDevinfo()
                            .getConn())) {
                        int isOn = Util.statusToDevCtrl(Integer.parseInt(packet
                                .getRespMessage().getDevinfo().getStates()));
                        if (state == isOn) {
                            stopSyncTimer();
                            tvProgress.setText("状态同步成功完成");
                            Message msg = new Message();
                            msg.what = MSG_CLOSE_SYNC_DIALOG;
                            mHandler.sendMessageDelayed(msg, 500);
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
                    // int pm = 320;
                    // int harmful = 450;
                    // // rbInner.setTextColor(Util.getPMColor(80));
                    // rbInner.setProgress(pm);
                    // setTextColor(true,pm);
                    // // rbOuter.setTextColor(Util.getPMColor(480));
                    // rbOuter.setProgress(harmful);
                    // setTextColor(false,harmful);
                    // ivBattery.setImageResource(getBatteryDrawableId(80));

                    // 保存loc
                    if (packet.getRespMessage() != null) {
                        if (CarairConstants.CONN_ON.equals(packet.getRespMessage().getDevinfo()
                                .getConn())) {
                            // 保存status
                            int status = Integer.parseInt(packet.getRespMessage().getDevinfo()
                                    .getStates());
                            if (status > -1) {
                                Util.saveStatusHeader(status, HomeFragment.this.getActivity());
                            }
                            int pm25 = (int) Float.parseFloat(packet.getRespMessage()
                                    .getDevinfo().getPm25());
                            // int opm25 = (int)
                            // Float.parseFloat(packet.getRespMessage().getAir()
                            // .getOpm25());
                            int harmairth = (int) Float.parseFloat(packet.getRespMessage()
                                    .getDevinfo().getHarmair());
                            String notice = packet.getRespMessage().getAppinfo().getNotice();
                            // StringBuffer sb = new StringBuffer();
                            // sb.append(notice.getWin() + "," +
                            // notice.getBaby() + ","
                            // + notice.getPregn());
                            setState(true, notice);
                            int battery = Integer.parseInt(packet.getRespMessage().getDevinfo()
                                    .getBattery());
                            if (battery > 100) {
                                ivCharging.setVisibility(View.VISIBLE);
                                ibValue.setEnabled(true);
                                int wind = Util.decodeStatus(status);
                                if (wind > -1) {
                                    Util.saveRatio(wind, getActivity());
                                }
                                setWindValue();
                                battery = battery - 100;
                            } else {
                                ivCharging.setVisibility(View.INVISIBLE);
                                ibValue.setEnabled(false);
                            }
                            ivBattery.setImageResource(getBatteryDrawableId(battery));
                            rbInner.setProgress(pm25);
                            // rbInner.setTextColor(Util.getPMColor(pm25));
                            setTextColor(true, pm25);
                            rbOuter.setProgress(harmairth);
                            setTextColor(false, harmairth);
                            // rbOuter.setTextColor(Util.getPMColor(harmairth));
                            // if
                            // (CarairConstants.CONN_ON.equals(packet.getRespMessage().getDevinfo()
                            // .getStates())) {
                            int isOn = Util.statusToDevCtrl(Integer.parseInt(packet
                                    .getRespMessage().getDevinfo().getStates()));
                            if (isOn == 1) {
                                startCleanAnimation(true);
                            } else {
                                startCleanAnimation(false);
                            }
                        } else {
                            setState(false, null);
                        }
                        Loc loc = packet.getRespMessage().getLoc();
//                        lat = loc.getLat();
//                        lng = loc.getLng();
                        if (loc != null && getActivity() != null) {
                            Util.saveLoc(loc, getActivity());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {

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
        } else if (progress > 300) {
            rb.setTextColor(Color.rgb(0xb1, 0x49, 0x7c));
            tv.setBackgroundResource(R.drawable.shape_car_progress_purple);
            tv.setText("严重污染");
        } else {
            tv.setBackgroundResource(R.drawable.shape_car_progress_green);
            tv.setText("未知");
            rb.setTextColor(Color.WHITE);
        }
    }

    private int getBatteryDrawableId(int battery) {
        if (battery == 0) {
            return R.drawable.battery0;
        }
        else if (battery > 0 && battery <= 2) {
            return R.drawable.battery2;
        }
        else if (battery > 2 && battery <= 4) {
            return R.drawable.battery4;
        }
        else if (battery > 4 && battery <= 6) {
            return R.drawable.battery6;
        }
        else if (battery > 6 && battery <= 8) {
            return R.drawable.battery8;
        }
        else if (battery > 8 && battery <= 10) {
            return R.drawable.battery10;
        }
        else if (battery > 10 && battery <= 12) {
            return R.drawable.battery12;
        }
        else if (battery > 12 && battery <= 14) {
            return R.drawable.battery14;
        }
        else if (battery > 14 && battery <= 16) {
            return R.drawable.battery16;
        }
        else if (battery > 16 && battery <= 18) {
            return R.drawable.battery18;
        } else if (battery > 18 && battery <= 20) {
            return R.drawable.battery20;
        } else if (battery > 20 && battery <= 22) {
            return R.drawable.battery22;
        } else if (battery > 22 && battery <= 24) {
            return R.drawable.battery24;
        } else if (battery > 24 && battery <= 26) {
            return R.drawable.battery26;
        } else if (battery > 26 && battery <= 28) {
            return R.drawable.battery28;
        } else if (battery > 28 && battery <= 31) {
            return R.drawable.battery31;
        } else if (battery > 31 && battery <= 34) {
            return R.drawable.battery34;
        } else if (battery > 34 && battery <= 37) {
            return R.drawable.battery37;
        } else if (battery > 37 && battery <= 40) {
            return R.drawable.battery40;
        } else if (battery > 40 && battery <= 43) {
            return R.drawable.battery43;
        } else if (battery > 43 && battery <= 46) {
            return R.drawable.battery46;
        } else if (battery > 46 && battery <= 49) {
            return R.drawable.battery49;
        } else if (battery > 49 && battery <= 52) {
            return R.drawable.battery52;
        } else if (battery > 52 && battery <= 55) {
            return R.drawable.battery55;
        } else if (battery > 55 && battery <= 58) {
            return R.drawable.battery58;
        } else if (battery > 58 && battery <= 61) {
            return R.drawable.battery61;
        } else if (battery > 61 && battery <= 64) {
            return R.drawable.battery64;
        } else if (battery > 64 && battery <= 67) {
            return R.drawable.battery67;
        } else if (battery > 67 && battery <= 70) {
            return R.drawable.battery70;
        } else if (battery > 70 && battery <= 73) {
            return R.drawable.battery73;
        } else if (battery > 73 && battery <= 76) {
            return R.drawable.battery76;
        } else if (battery > 76 && battery <= 79) {
            return R.drawable.battery79;
        } else if (battery > 79 && battery <= 82) {
            return R.drawable.battery82;
        } else if (battery > 82 && battery <= 85) {
            return R.drawable.battery85;
        } else if (battery > 85 && battery <= 88) {
            return R.drawable.battery88;
        } else if (battery > 88 && battery <= 91) {
            return R.drawable.battery91;
        } else if (battery > 91 && battery <= 94) {
            return R.drawable.battery94;
        } else if (battery > 94 && battery <= 97) {
            return R.drawable.battery97;
        } else if (battery > 97 && battery <= 100) {
            return R.drawable.battery100;
        } else {
            return R.drawable.battery_unknow;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // stopTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimer();
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void startTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    query();
                }
            }, 0, 1000 * 10);
        }
    }

    public void startSyncTimer(final int state) {
        if (syncTimer == null) {
            syncTimer = new Timer();
            syncTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    querySync(state);
                }
            }, 0, 1000 * 10);
        }
    }

    public void stopSyncTimer() {
        if (syncTimer != null) {
            syncTimer.cancel();
        }
    }

    private void setState(boolean isconnection, String message) {
        if (isconnection) {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("净化器已连接");
            mPrompt.setVisibility(View.VISIBLE);
            mPrompt.setText(message);
            // mPrompt.setBackgroundResource(R.drawable.shape_prompt);
            outText.setVisibility(View.VISIBLE);
            ivBattery.setVisibility(View.VISIBLE);
            innerText.setVisibility(View.VISIBLE);
            ibValue.setVisibility(View.VISIBLE);
            ibTimer.setVisibility(View.VISIBLE);
            ibData.setVisibility(View.VISIBLE);
            ibClean.setEnabled(true);
            switchBackground.setBackgroundResource(R.drawable.switch_bg);
        } else {
            // mPrompt.setText("净化器未连接");
            if (!firstStart) {
                Toast.makeText(getActivity(), "净化器未连接，请确保净化器处于有信号的地区以后再操作", Toast.LENGTH_SHORT)
                        .show();
                firstStart = true;
            }
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("净化器未连接");
            // mPrompt.setBackgroundResource(android.R.color.transparent);
            mPrompt.setVisibility(View.INVISIBLE);
            // outText.setVisibility(View.INVISIBLE);
            // innerText.setVisibility(View.INVISIBLE);
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
            ibClean.setEnabled(false);
            switchBackground.setBackgroundResource(R.drawable.not_connected_switch_bg);

        }
    }

    private void init() {
        ibClean = (ImageButton) mMainView.findViewById(R.id.ibClean);
        ibValue = (ImageButton) mMainView.findViewById(R.id.ibValue);
        ibTimer = (ImageButton) mMainView.findViewById(R.id.ibTimer);
        ibData = (ImageButton) mMainView.findViewById(R.id.ibData);
        rbOuter = (RoundProgressBar) mMainView.findViewById(R.id.outCarProgress);
        rbInner = (RoundProgressBar) mMainView.findViewById(R.id.inCarProgress);
        mPrompt = (TextView) mMainView.findViewById(R.id.promptText);
        outText = (TextView) mMainView.findViewById(R.id.tvOutCar);
        innerText = (TextView) mMainView.findViewById(R.id.tvInCar);
        cleanText = (TextView) mMainView.findViewById(R.id.cleanText);
        ivBattery = (ImageView) mMainView.findViewById(R.id.ivBattery);
        ivCharging = (ImageView) mMainView.findViewById(R.id.ivCharging);
        llWind = (RelativeLayout) mMainView.findViewById(R.id.llWind);
        tvWindAuto = (TextView) mMainView.findViewById(R.id.windAuto);
        tvWindStrong = (TextView) mMainView.findViewById(R.id.windStrong);
        tvWindWeak = (TextView) mMainView.findViewById(R.id.windWeak);
        tvWindValue = (TextView) mMainView.findViewById(R.id.windValue);
        tvInCar = (TextView) mMainView.findViewById(R.id.tvInCarPrompt);
        tvOutCar = (TextView) mMainView.findViewById(R.id.tvOutCarPrompt);
        flProgress = (FrameLayout) mMainView.findViewById(R.id.flProgress);
        tvProgress = (TextView) mMainView.findViewById(R.id.tv_progress_sync);
        setWindValue();
        llWind.setOnClickListener(this);
        tvWindAuto.setOnClickListener(this);
        tvWindStrong.setOnClickListener(this);
        tvWindWeak.setOnClickListener(this);
        rbOuter.setMax(500);
        rbInner.setMax(500);
        switchBackground = (ImageView) mMainView.findViewById(R.id.switchBackground);
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
    }

    private void setWindValue() {
        int windValue = Util.getRatio(getActivity());
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
                break;
        }
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
                if (isCleaning) {
                    Toast.makeText(getActivity(), "正在关闭净化功能", Toast.LENGTH_SHORT).show();
                    devctrl(false);
                    startCleanAnimation(false);
                } else {
                    Toast.makeText(getActivity(), "正在开启净化功能", Toast.LENGTH_SHORT).show();
                    devctrl(true);
                    startCleanAnimation(true);
                }
                break;
            case R.id.ibValue:
                // Intent ivalue = new Intent(getActivity(),
                // CleanRatioActivity.class);
                // getActivity().startActivity(ivalue);
                startWindControl();
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
                Util.saveRatio(CarairConstants.RATIO_AUTO, getActivity());
                startWindControl();
                break;
            case R.id.windStrong:
                // Toast.makeText(getActivity(), "强风", 1).show();
                Util.saveRatio(CarairConstants.RATIO_HIGH, getActivity());
                startWindControl();
                break;
            case R.id.windWeak:
                // Toast.makeText(getActivity(), "弱风", 1).show();
                Util.saveRatio(CarairConstants.RATIO_LOW, getActivity());
                startWindControl();
                break;
            case R.id.llWind:

                break;
            default:
                break;
        }
    }

    private void startWindControl() {
        if (!windButonshow) {
            windButonshow = true;
            if (!firstPushWind) {
                Toast.makeText(getActivity(), "为了保护净化器电池，请确保净化器处于充电状态在操作风力", Toast.LENGTH_SHORT)
                        .show();
                firstPushWind = true;
            }
            tvWindValue.setVisibility(View.GONE);
            llWind.setVisibility(View.VISIBLE);
            final RelativeLayout.LayoutParams params = (LayoutParams) tvWindStrong
                    .getLayoutParams();
            ValueAnimator windOut = ValueAnimator.ofInt(params.topMargin,
                    Util.Dp2Px(getActivity(), 0));
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
                    Util.Dp2Px(getActivity(), 23));
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
                    Util.Dp2Px(getActivity(), 50));
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
                    Util.Dp2Px(getActivity(), 80));
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
                    Util.Dp2Px(getActivity(), 46));
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
                    Util.Dp2Px(getActivity(), 46));
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
                    setWindValue();
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
                    Util.Dp2Px(getActivity(), 70));
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
        if (ison) {
            startSyncTimer(CarairConstants.ON);
        } else {
            startSyncTimer(CarairConstants.OFF);
        }
    }

    private void devctrl(final boolean ison) {
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                Toast.makeText(getActivity(), "操作成功", Toast.LENGTH_SHORT).show();
                syncState(ison);
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {
                Toast.makeText(getActivity(), "操作失败", Toast.LENGTH_SHORT).show();
            }
        }.devctrl(getActivity(), ison);
    }
}
