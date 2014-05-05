package com.android.carair.imagepool.utility;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import com.android.carair.imagepool.ImageHandler;


/** 
 * TBDrawable继承于BitmapDrawable
 * 扩展了回收Bitamp在Native层内存资源
 * */
public class TBDrawable extends BitmapDrawable {
		
	private ImageHandler m_ih;
	
	/** 构造函数
	 * @param bp 管理的Bitmap对象
	 * */	
	@SuppressWarnings("deprecation")
	public TBDrawable(Bitmap bp)
	{
		super(bp);		
		//m_ih = null;
	}
	
	/** 重载了draw,catch在recycle后的图片上的异常
	 * @param canvas 画布对象
	 * */
	 @Override
	public void	 draw(Canvas canvas)
	{			
		 try
		 {
			super.draw(canvas);
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }

	}

	 /** 是否在回收状态	 
		* */
	public boolean isRecycled()
	{
		Bitmap bp = getBitmap();
		if( bp != null && !bp.isRecycled())
		{
			return false;
		}
		return true;
			
	}
	
	/** 回收Drawable的Bitmap在native层次的资源	 
	* */	 	 
	public boolean recycle()
	{
		Bitmap bp = getBitmap();
		if( bp != null && !bp.isRecycled())
		{			
			//TaoLog.Loge(TaoLog.IMGPOOL_TAG, "TBDrawable recycled: " + bp);
			bp.recycle();			
			//m_ih = null;
			return true;
		}					
		return false;			
	}
	
	/** bitmap占有字节大小
	* */	
	public int bitmapSize()
	{
		int size = 0;
						
		Bitmap bp = getBitmap();
		if( bp != null)
		{
			if(bp.isRecycled())
				return 0;
			
			//用RawBytes来判断			
			size = bp.getRowBytes() * bp.getHeight();
			//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "!!!!!!bitmap size:" + size);			
								
		}
		return size;
	}
	
	/** 设置关联的ImageHandler
	* */
	public void setImageHandler(ImageHandler ih )
	{
		m_ih = ih;
	}
	
	/** 获得ImageHandler
	* */
	public ImageHandler getImageHandler()
	{
		return m_ih;
	}
}
