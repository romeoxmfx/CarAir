package com.android.carair.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.carair.activities.MainActivity;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.goodhelpercarair.R;

public class AboutUsFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(R.layout.carair_aboutus_fragment, null);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("");
        TextView tvVer = (TextView) mMainView.findViewById(R.id.tvVer);
        try {
            String version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            tvVer.setText(version);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mMainView;
    }
}
