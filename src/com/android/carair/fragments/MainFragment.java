
package com.android.carair.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.carair.R;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;

public class MainFragment extends BaseFragment {
    String str;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (FragmentViewBase) inflater.inflate(
                R.layout.carair_fragment_main, null);
        if (getArguments() != null) {
            str = getArguments().getString("text");
        }
        TextView tv = (TextView) mMainView.findViewById(R.id.tv);
        if (!TextUtils.isEmpty(str)) {
            tv.setText(str);
        }
        return mMainView;
    }

}
