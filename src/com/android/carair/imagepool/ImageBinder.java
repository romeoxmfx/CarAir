package com.android.carair.imagepool;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * ImageBinder
 * 实现绑定的接口定义
 * @deprecated
 * */
public interface ImageBinder
{
	/**
	 * bindImg2View
	 * 把Drawable设置到view上
	 * @param d Drawable对象
	 * @param view 待绑定的控件
	 * @return 是否绑定成功
	 * */
	public boolean bindImg2View( Drawable d, View view);
	
	/**
	 * unbind
	 * Destroy时和View解绑的操作
	 * @param view 绑定的控件
	 * @return 是否解绑成功
	 * */
	public boolean unbind(View view);
	
}