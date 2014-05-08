package com.android.carair.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.carair.R;
import com.android.carair.fragments.base.BaseFragment;
import com.android.carair.fragments.base.FragmentViewBase;
import com.android.carair.views.PinnedSectionListView;
import com.android.carair.views.PinnedSectionListView.PinnedSectionListAdapter;

public class MainFragment extends BaseFragment {

	private static final String[] ITEM_TITLE = new String[] { "车内", "车外", "设置",
			"地图" };

	static class Item {

		public static final int VIEW_TYPE_COUNT = 2;
		public static final int ITEM = 0;
		public static final int SECTION = 1;

		public final int type;
		public final String text;

		public int sectionPosition;
		public int listPosition;

		public Item(int type, String text) {
			this.type = type;
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}

	}

	static class SimpleAdapter extends BaseAdapter implements
			PinnedSectionListAdapter {
		protected final LayoutInflater mInflater;
		protected int mResource;
		private ArrayList<Item> items = new ArrayList<MainFragment.Item>();
		
		public SimpleAdapter(Context context) {
			
			this.mInflater = LayoutInflater.from(context);
			for (int i = 0; i < ITEM_TITLE.length; i++) {
				Item section = new Item(Item.SECTION,ITEM_TITLE[i]);
				items.add(section);
				Item item = new Item(Item.ITEM,ITEM_TITLE[i]);
				items.add(item);
			}
		}
		
		@Override
		public int getCount() {
			return items.size();
		}


		@Override
		public Object getItem(int position) {
			return items.get(position);
		}


		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public int getViewTypeCount() {
			return Item.VIEW_TYPE_COUNT;
		}
		
		@Override
		public int getItemViewType(int position) {
			return items.get(position).type;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Item item = items.get(position);
			if (item.type == Item.SECTION) {
				mResource = R.layout.carair_main_list_section;
				convertView = mInflater.inflate(mResource, null);
				TextView textView = (TextView) convertView.findViewById(R.id.section_text);
				textView.setText(item.text);
			}
			
			if (item.type == Item.ITEM) {
				if ("车内".equals(item.text)) {
					mResource = R.layout.carair_main_list_item_incar;
					convertView = mInflater.inflate(mResource, null);
				}
				if ("车外".equals(item.text)) {
					mResource = R.layout.carair_main_list_item_outcar;
					convertView = mInflater.inflate(mResource, null);
				}
				if ("设置".equals(item.text)) {
					mResource = R.layout.carair_main_list_item_setting;
					convertView = mInflater.inflate(mResource, null);
				}
				if ("地图".equals(item.text)) {
					mResource = R.layout.carair_main_list_item_map;
					convertView = mInflater.inflate(mResource, null);
				}
			}
			
			return convertView;
		}
		

		@Override
		public boolean isItemViewTypePinned(int viewType) {
			 return viewType == Item.SECTION;
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mMainView = (FragmentViewBase) inflater.inflate(
				R.layout.carair_fragment_main, null);

		PinnedSectionListView listView = (PinnedSectionListView) mMainView.findViewById(R.id.main_list);
		
		listView.setAdapter(new SimpleAdapter(getActivity()));

		return mMainView;
	}

}
