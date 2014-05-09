package com.android.carair.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.carair.R;
import com.android.carair.adapters.MainListApapter;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.views.PinnedSectionListView;

public class MainFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mMainView = (FragmentViewBase) inflater.inflate(
				R.layout.carair_fragment_main, null);

		PinnedSectionListView listView = (PinnedSectionListView) mMainView
				.findViewById(R.id.main_list);

		listView.setAdapter(new MainListApapter(getActivity(), getResources()
				.getStringArray(R.array.item_title)));

		return mMainView;
	}

}
