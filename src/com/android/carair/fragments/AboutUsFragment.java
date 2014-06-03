package com.android.carair.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.carair.R;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;

public class AboutUsFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(R.layout.carair_aboutus_fragment, null);
        return mMainView;
    }
}
