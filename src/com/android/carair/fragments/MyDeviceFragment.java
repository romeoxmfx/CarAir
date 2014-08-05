package com.android.carair.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.carair.R;
import com.android.carair.activities.LoginActivity;
import com.android.carair.activities.MainActivity;
import com.android.carair.activities.base.BaseActivity;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentPageManager;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.utils.Util;

public class MyDeviceFragment extends BaseFragment {
    TextView tvDevice;
    Button btLoginOut;
    LinearLayout llDevice;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(R.layout.carair_mydevice_fragment, null);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("");
        tvDevice = (TextView) mMainView.findViewById(R.id.tv_device);
        btLoginOut = (Button) mMainView.findViewById(R.id.login_out);
//        tvDevice.setText(Util.getDeviceId(getActivity()));
        llDevice = (LinearLayout) mMainView.findViewById(R.id.lldevice);
        llDevice.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                changeContent(new HomeFragment(), null);
            }
        });
        btLoginOut.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Util.clearDeviceId(getActivity());
                Intent intent = new Intent();
                intent.setClass(getActivity(), LoginActivity.class);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });
        return mMainView;
    }
    
    private void changeContent(Fragment frg, Bundle arg) {
        if (getActivity() == null) {
            return;
        }
        FragmentPageManager.getInstance().setFragmentManager(
                getActivity().getSupportFragmentManager());
        FragmentPageManager.getInstance().pushContentPage(frg, frg.getClass().getName(), arg);
        ((BaseActivity) getActivity()).getSlidingMenu().showContent();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            changeContent(new HomeFragment(), null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
