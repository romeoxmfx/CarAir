package com.android.carair.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.maps2d.MapView;
import com.android.carair.R;
import com.android.carair.fragments.MainFragment;
import com.android.carair.views.PinnedSectionListView.PinnedSectionListAdapter;

public class MainListApapter extends BaseAdapter implements
		PinnedSectionListAdapter {

	protected final Context mContext;
	protected final LayoutInflater mInflater;
	protected int mResource;
	private MainFragment mFragment;
	private ArrayList<Item> items = new ArrayList<Item>();

	static class Item {

		public static final int SECTION = 0;
		public static final int ITEM_IN_CAR = 1;
		public static final int ITEM_OUT_CAR = 2;
		public static final int ITEM_SETTING = 3;
		public static final int ITEM_MAP = 4;
		public static final int VIEW_TYPE_COUNT = ITEM_MAP + 1;

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

	static class ViewHolder {
		TextView sectionText;
		MapView map;
	}

	public MainListApapter(Context context, String[] itemTitles,MainFragment fragment) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.mFragment = fragment;
		for (int i = 0; i < itemTitles.length; i++) {
			Item section = new Item(Item.SECTION, itemTitles[i]);
			items.add(section);
			if (mContext.getResources().getString(R.string.in_car)
					.equals(itemTitles[i])) {
				Item item = new Item(Item.ITEM_IN_CAR, itemTitles[i]);
				items.add(item);
			} else if (mContext.getResources().getString(R.string.out_car)
					.equals(itemTitles[i])) {
				Item item = new Item(Item.ITEM_OUT_CAR, itemTitles[i]);
				items.add(item);
			} else if (mContext.getResources().getString(R.string.setting)
					.equals(itemTitles[i])) {
				Item item = new Item(Item.ITEM_SETTING, itemTitles[i]);
				items.add(item);
			} else if (mContext.getResources().getString(R.string.map)
					.equals(itemTitles[i])) {
				Item item = new Item(Item.ITEM_MAP, itemTitles[i]);
				items.add(item);
			}
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
		ViewHolder holder = null;
		int type = getItemViewType(position);
		if (convertView == null) {
			holder = new ViewHolder();
			switch (type) {
			case Item.SECTION:
				convertView = mInflater.inflate(
						R.layout.carair_main_list_section, null);
				holder.sectionText = (TextView) convertView
						.findViewById(R.id.section_text);
				holder.sectionText.setText(items.get(position).text);
				convertView.setTag(holder);
				break;
			case Item.ITEM_IN_CAR:
				convertView = mInflater.inflate(
						R.layout.carair_main_list_item_incar, null);
				convertView.setTag(holder);
				break;
			case Item.ITEM_OUT_CAR:
				convertView = mInflater.inflate(
						R.layout.carair_main_list_item_outcar, null);
				convertView.setTag(holder);
				break;
			case Item.ITEM_SETTING:
				convertView = mInflater.inflate(
						R.layout.carair_main_list_item_setting, null);
				convertView.setTag(holder);
				break;
			case Item.ITEM_MAP:
				convertView = mInflater.inflate(
						R.layout.carair_main_list_item_map, null);
				holder.map = (MapView) convertView.findViewById(R.id.map);
				mFragment.setMap(holder.map);
				convertView.setTag(holder);
				break;

			default:
				break;
			}
		} else {
			holder = (ViewHolder) convertView.getTag();
			switch (type) {
			case Item.SECTION:
				convertView = mInflater.inflate(
						R.layout.carair_main_list_section, null);
				holder.sectionText = (TextView) convertView
						.findViewById(R.id.section_text);
				holder.sectionText.setText(items.get(position).text);
				convertView.setTag(holder);
				break;
			case Item.ITEM_IN_CAR:
				break;
			case Item.ITEM_OUT_CAR:
				break;
			case Item.ITEM_SETTING:
				break;
			case Item.ITEM_MAP:
				break;
			default:
				break;
			}
		}

		return convertView;
	}

	@Override
	public boolean isItemViewTypePinned(int viewType) {
		return viewType == Item.SECTION;
	}

}
