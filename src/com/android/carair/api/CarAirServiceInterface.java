package com.android.carair.api;

import android.content.Context;


public interface CarAirServiceInterface {

	public void reg(Context context);
	
	public void query(Context context);
	
	public void devctrl(Context context,boolean isopen);
	
	public void history(Context context);
}
