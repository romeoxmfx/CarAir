package com.android.carair.imagepool;

import android.graphics.Bitmap;


//图像转换
/** BitmapConvertor定义了图像转换的接口	  
 * 获得ImageHandler时可以提供一个转换方法（如做圆角），来改变ImageHandler
 * 管理的Bitmap
 */
public interface BitmapConvertor
{	
	/**从原先的Bitmap转换为新的Bitmap	  
	 * @param origianl 老的Bitmap 
	 * @return 转换后的Bitmap
	 */
	public Bitmap convertTo(Bitmap original);		
}