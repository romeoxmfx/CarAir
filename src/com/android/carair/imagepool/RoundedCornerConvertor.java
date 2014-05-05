package com.android.carair.imagepool;

import com.android.carair.imagepool.utility.BitmapHelper;

import android.graphics.Bitmap;

/**BitmapConvertor的一个实现	  
 * 圆角转化
 */	
public  class RoundedCornerConvertor implements BitmapConvertor
{
	@Override
	public Bitmap convertTo(Bitmap original) {
		// TODO Auto-generated method stub			
		return BitmapHelper.getRoundedCornerBitmap(original);			
	}
}