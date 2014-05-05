package com.android.carair.imagepool.utility;

import java.util.ArrayList;

import android.graphics.Bitmap;


/** BitmapHelperFactory用于支持可扩展的图片格式的解压
 *  
 * */
public class BitmapHelperFactory {
	
	//helper的列表
	static ArrayList<IBitmapHelper> m_helpers = new ArrayList<IBitmapHelper>();
	
	/** 
	 *  SDK使用者扩展图片格式，可以通过registerHelper加入支持的格式
	 *  @param  helper BitampHelper的实现 
	 * */
	public static void registerHelper( IBitmapHelper helper )
	{
		synchronized(m_helpers)
		{
			if( !m_helpers.contains(helper))
			{
				m_helpers.add(helper);	
			}
			
		}
	}
	
	/** 
	 *  给予数据流和文件URL，返回解压出来的Bitamp
	 *  @param  b  数据流
	 *  @param url 文件URL
	 *  @return 解压出来的Bitamp，失败返回Null 
	 * */
	public static Bitmap Bytes2Bimap(byte[] b, String url){				
		
		Object[] obsCopy = null;
		
		synchronized (m_helpers) {
			obsCopy = m_helpers.toArray();
		}
				
		for( int i = 0 ; i < obsCopy.length ;i++)
		{
				IBitmapHelper helper = (IBitmapHelper) obsCopy[i];
				if( helper.isSupport(b, url) )
				{
					return helper.Bytes2Bimap(b, url);
				}						
		}						
		return BitmapHelper.Bytes2Bimap(b);
	}
	
	/** 
	 *  给予数据流和文件URL，返回是否支持改格式
	 *  @param  b  数据流
	 *  @param url 文件URL
	 *  @return 是否支持该格式 
	 * */
	public static boolean isSupport(byte[] b, String url){				
		synchronized(m_helpers)
		{
			for( int i = 0 ; i < m_helpers.size() ;i++)
			{
				IBitmapHelper helper = m_helpers.get(i);
				if( helper.isSupport(b, url) )
				{
					return true;
				}
			}
		}
		return false;
	}
}
