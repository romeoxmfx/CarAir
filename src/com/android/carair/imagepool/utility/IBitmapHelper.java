package com.android.carair.imagepool.utility;

import android.graphics.Bitmap;

/** BitmapHelperFactory用于支持可扩展的图片格式的解压
 *  IBitmapHelper定义了用于解压图片的BitmapHelper的接口
 * */
public interface IBitmapHelper
{
	/** 
	 *  给予数据流和文件URL，判断是否支持
	 *  @param  b  数据流
	 *  @param url 文件URL
	 *  @return 是否支持这个图片文件的解压 
	 * */
	boolean isSupport( byte[] b , String url);
	
	/** 
	 *  给予数据流和文件URL，返回解压出来的Bitamp
	 *  @param  b  数据流
	 *  @param url 文件URL
	 *  @return 解压出来的Bitamp，失败返回Null 
	 * */
	Bitmap Bytes2Bimap(byte[] b, String url);
	
}
