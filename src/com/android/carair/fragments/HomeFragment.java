
package com.android.carair.fragments;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.carair.R;
import com.android.carair.activities.HistoryActivity;
import com.android.carair.activities.MainActivity;
import com.android.carair.api.Air;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.DevInfo;
import com.android.carair.api.Loc;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.common.CarairConstants;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.net.HttpErrorBean;
import com.android.carair.utils.Util;
import com.android.carair.views.MySwitch;
import com.android.carair.views.RoundProgressBar;

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
    boolean mIsConnection = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(R.layout.carair_home_fragment, null);
        init();
        setState(mIsConnection, null);
        // query();
        return mMainView;
    }

    private void query() {
        if (getActivity() == null) {
            return;
        }
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                try {
//                    mIsConnection = true;
//                    setState(mIsConnection, "宝宝可进");
//                    rbInner.setTextColor(Util.getPMColor(500));
//                    rbInner.setProgress(5);
//                    rbOuter.setTextColor(Util.getPMColor(500));
//                    rbOuter.setProgress(20);

                    // 保存loc
                    if (packet.getRespMessage() != null) {
                        Loc loc = packet.getRespMessage().getLoc();
                        if (loc != null && getActivity() != null) {
                            Util.saveLoc(loc, getActivity());
                        }
                        if (CarairConstants.CONN_ON.equals(packet.getRespMessage().getDevinfo()
                                .getConn())) {
                            int pm25 = (int) Float.parseFloat(packet.getRespMessage()
                                    .getDevinfo().getPm25());
                            int opm25 = (int) Float.parseFloat(packet.getRespMessage().getAir()
                                    .getOpm25());
                            String message = "";
                            if (opm25 > pm25) {
                                message = "不宜开窗";
                            }
                            if (opm25 > 40) {
                                message += " 孕妇勿入";
                            } else if (pm25 > 70) {
                                message += " 宝宝勿入";
                            }
                            setState(true, message);
                            rbInner.setProgress(pm25);
                            rbOuter.setProgress(opm25);
                            if (CarairConstants.CONN_ON.equals(packet.getRespMessage().getDevinfo()
                                    .getStates())) {
                                startCleanAnimation(true);
                            } else {
                                startCleanAnimation(false);
                            }
                        } else {
                            setState(false, null);
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

    @Override
    public void onStop() {
        super.onStop();
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

    private void setState(boolean isconnection, String message) {
        if (isconnection) {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("净化器已连接");
            mPrompt.setVisibility(View.VISIBLE);
            mPrompt.setText(message);
            // mPrompt.setBackgroundResource(R.drawable.shape_prompt);
            outText.setVisibility(View.VISIBLE);
            innerText.setVisibility(View.VISIBLE);
            ibValue.setVisibility(View.VISIBLE);
            ibValue.setVisibility(View.VISIBLE);
            ibTimer.setVisibility(View.VISIBLE);
            ibData.setVisibility(View.VISIBLE);
            ibClean.setEnabled(true);
            switchBackground.setBackgroundResource(R.drawable.switch_bg);
        } else {
            // mPrompt.setText("净化器未连接");
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("净化器未连接");
            // mPrompt.setBackgroundResource(android.R.color.transparent);
            mPrompt.setVisibility(View.INVISIBLE);
            outText.setVisibility(View.INVISIBLE);
            innerText.setVisibility(View.INVISIBLE);
            rbOuter.setProgress(0);
            rbInner.setProgress(0);
            ibValue.setVisibility(View.INVISIBLE);
            ibValue.setVisibility(View.INVISIBLE);
            ibTimer.setVisibility(View.INVISIBLE);
            cleanText.setText("");
            ibData.setVisibility(View.INVISIBLE);
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

    private void startCleanAnimation(boolean start) {
        if (start) {
            switchBackground.startAnimation(mAnimation);
            isCleaning = true;
            cleanText.setText("净化中");
        } else {
            switchBackground.clearAnimation();
            cleanText.setText("");
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
                    devctrl(false);
                    startCleanAnimation(false);
                } else {
                    devctrl(true);
                    startCleanAnimation(true);
                }
                break;
            case R.id.ibValue:
                break;
            case R.id.ibTimer:
                break;
            case R.id.ibData:
                // 打开历史
                Intent intent = new Intent();
                intent.putExtra("type", Item.ITEM_IN_CAR);
                intent.setClass(getActivity(), HistoryActivity.class);
                getActivity().startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void devctrl(boolean ison) {
        new CarAirReqTask() {

            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                Toast.makeText(getActivity(), "操作成功", 1).show();
            }

            @Override
            public void onCompleteFailed(int type, HttpErrorBean error) {
                Toast.makeText(getActivity(), "操作失败", 1).show();
            }
        }.devctrl(getActivity(), ison);
    }
}
