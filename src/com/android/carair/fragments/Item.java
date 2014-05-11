package com.android.carair.fragments;

import java.util.Map;

public class Item {

	public static final int SECTION = 0;
	public static final int ITEM_IN_CAR = 1;
	public static final int ITEM_OUT_CAR = 2;
	public static final int ITEM_SETTING = 3;
	public static final int ITEM_MAP = 4;
	public static final int VIEW_TYPE_COUNT = ITEM_MAP + 1;

	public final int type;
	public final String text;

	ViewHolder holder;
	
	Map<String, String> map;
	
	public int sectionPosition;
	public int listPosition;

	public Item(int type, String text) {
		this.type = type;
		this.text = text;
	}
	
	public Item(int type, String text , Map<String, String> map) {
		this.type = type;
		this.text = text;
		this.map = map;
	}
	
	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	
	public void setViewHolder (ViewHolder holder) {
		this.holder = holder;
	}
	
	public ViewHolder getViewHolder () {
		return holder;
	}

	public Map<String, String> getMap() {
		return map;
	}
	
	@Override
	public String toString() {
		return text;
	}

}
