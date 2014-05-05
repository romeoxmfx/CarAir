package com.android.carair.imagepool;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

/**
 * ImageForegroudBinder
 * 预定义的实现，把图片设置到View的foreground
 * @deprecated
 * */
public  class ImageForegroudBinder implements ImageBinder
{
	/**
	 * bindImg2View
	 * 把Drawable设置为View的Background
	 * @param d Drawable对象
	 * @param view 待绑定的view,必须是ImageView
	 * @return 是否绑定成功
	 * */
	public boolean bindImg2View( Drawable d, View view)
	{
		if( view instanceof ImageView )
		{
			 ((ImageView) view).setImageDrawable(d);
		}
		else
		{
		}
		
		return true;			
	}
	
	public boolean unbind( View view)
	{
		if( view instanceof ImageView )
		{
			 ((ImageView) view).setImageDrawable(null);
		}
		else
		{
		}
		
		return true;			
	}
}