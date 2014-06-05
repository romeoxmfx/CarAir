
package com.android.carair.fragments;

import java.util.Timer;
import java.util.TimerTask;

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

import com.android.carair.R;
import com.android.carair.api.Air;
import com.android.carair.api.CarAirReqTask;
import com.android.carair.api.DevInfo;
import com.android.carair.api.RespProtocolPacket;
import com.android.carair.common.CarairConstants;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.net.HttpErrorBean;
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
        setState(mIsConnection);
        query();
        return mMainView;
    }

    private void query() {
        if(getActivity() == null){
            return;
        }
        new CarAirReqTask() {
            
            @Override
            public void onCompleteSucceed(RespProtocolPacket packet) {
                try {
//                    DevInfo info = packet.getRespMessage().getDevinfo();
//                    Air air = packet.getRespMessage().getAir();
//                    if(info != null && CarairConstants.CONN_ON.equals(info.getStates())){
//                        mIsConnection = true;
//                        setState(mIsConnection);
//                        if(!TextUtils.isEmpty(info.getPm25())){
//                            rbInner.setProgress(Integer.parseInt(info.getPm25()));
//                        }
//                        if(air != null && !TextUtils.isEmpty(air.getOpm25())){
//                            rbOuter.setProgress(Integer.parseInt(air.getOpm25()));
//                        }
//                    }
                    
                    setState(true);
                    rbInner.setProgress(100);
                    rbOuter.setProgress(50);
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
    
    public void stopTimer(){
        if(timer != null){
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
    
    private void setState(boolean isconnection){
        if(isconnection){
            mPrompt.setText("宝宝可以进入");
            outText.setVisibility(View.VISIBLE);
            innerText.setVisibility(View.VISIBLE);
            ibValue.setVisibility(View.VISIBLE);
            ibValue.setVisibility(View.VISIBLE);
            ibTimer.setVisibility(View.VISIBLE);
            ibData.setVisibility(View.VISIBLE);
            ibClean.setEnabled(true);
        }else{
            mPrompt.setText("净化器未连接");
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
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        switch (id) {
            case R.id.ibClean:
                if (isCleaning) {
                    switchBackground.clearAnimation();
                    cleanText.setText("");
                    isCleaning = false;
                } else {
                    isCleaning = true;
                    cleanText.setText("净化中");
                    switchBackground.startAnimation(mAnimation);
                }
                break;
            case R.id.ibValue:
                break;
            case R.id.ibTimer:
                break;
            case R.id.ibData:
                break;
            default:
                break;
        }
    }
}
