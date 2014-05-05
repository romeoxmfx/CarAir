package com.android.carair.imagepool;

import android.graphics.Bitmap;


/**
 * BitmapCreator定义了图像生成的接口	  
 * 获得ImageHandler时可以提供一个图片生成方法，
 * 来自定义ImageHandler生成图片的方式
 * @author chengzheng.hcz
 */
public interface BitmapCreator
{	
	/**
	 * 可自定义的生成图片的方法
	 * @param uri 图片生成的依赖(也是图片内存缓存的key)
	 * @return 生成的图片
	 */
	public Bitmap createBitmap(String uri);
}