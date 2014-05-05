package com.android.carair.imagepool;

import android.graphics.drawable.Drawable;
import android.view.View;


/**
 * ImageBackgroudBinder
 * 预定义的实现，把图片设置到View的background
 * @deprecated
 * */
public  class ImageBackgroudBinder implements ImageBinder
{
	/**
	 * bindImg2View
	 * 把Drawable设置为View的Background
	 * @param d Drawable对象
	 * @param view 待绑定的控件
	 * @return 是否绑定成功
	 * */
	public boolean bindImg2View( Drawable d, View view)
	{
		view.setBackgroundDrawable(d);			
		return true;			
	}
	
	public boolean unbind(View view)
	{
		view.setBackgroundDrawable(null);
		return true;
	}

	
}