package com.android.carair.imagepool;

import com.android.carair.imagepool.utility.TBDrawable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Printer;
/**
 * 图片资源句柄的作用
 * 1. 保存着已加载到内存中的，可直接显示的位图图片数据
 * 2. 使用弱引用维护该图片的drawable数据，可以在无强引用以后被释放。如果被释放了，则该句柄应该返回无效，也可以被释放
 * 3. 维护图片是否缓存的策略设置
 * 4. 提供接口可以对图片进行尺寸调整和格式转换
 * 5. 可以将位图图片序列化成指定格式的图片数据流以便缓存进行保存
 * 6. 增加引用计数
 * 7. 维护在内存、本地、下载和回收的状态 
 */

public class ImageHandler 
{		
			
	//图片对象的当前状态
	static final int NOT_LOADED = 0;
	static final int LOADING = 2;
	static final int LOADED = 3;
	static final int LOAD_FAILED = 4;
	static final int LOAD_RECYCLE = 5;
	static final int CONVERTED = 6;
	
	//构造函数
	protected ImageHandler( String uri , int cp)
	{		
		m_URI = uri;
		m_cachePolicy = cp;
	}
	
	private BitmapCreator m_bitmapCreator;
	
	public BitmapCreator getBitmapCreator() {
		return m_bitmapCreator;
	}

	protected ImageHandler( String uri , int cp, BitmapCreator bc)
	{
		m_bitmapCreator = bc;
		m_URI = uri;
		m_cachePolicy = cp;
	}
			
	//为调试设置一个可读的字符串
	private String viewTag;
	/**
	 * 设置用于调试的字符串	 	
	 */
	public void setViewTag( String tag)
	{
		viewTag = tag;		
	}
	
	/**
	 * 返回调试的字符串	 
	 * @return 返回字符串	
	 */
	public String getViewTag(){ return viewTag; }
	
	
	//引用计数, 为0的Handler中的Drawable可被回收。
	int m_refCount = 0;				
	synchronized void addRef()
	{	
		//TaoLog.Logv(TaoLog.IMGPOOL_TAG, "add ref counter " +(m_refCount+1) +" url:" + URI());				
		m_refCount++;				
	}
	
	synchronized void  subRef()	
	{	
		//TaoLog.Logv(TaoLog.IMGPOOL_TAG, "sub counter " + (m_refCount-1) + " ref url:" + URI());
		m_refCount--;					
	}
	
	
	/* 查是否ImageHandler处于回收状态，如是尝试从缓存中恢复	  
	 * @return 返回drawable是否是从回收状态恢复了
	 */
	boolean _ReloadRecyceledIfNeed() //pay attention to not make this sync as it access image pool.
	{
		
		if( isRecyceled() )		
		{
			//从Cache中恢复
			return ImagePool.instance()._loadDrawable( this, URI(), m_cachePolicy,m_bitmapCreator);
		}
		
		return false;		
	}
	
	//是否处于可回收状态
	boolean isRecyclable()
	{
		return (m_refCount <= 0  ); 
	}
	
	/*
	 * 是否是一个有效的BitmapDrawable，Drawable有值，且不在回收状态
	 * 这个函数会引起引用计数增加，专为getImageHandler和getImageHandlerInMem调用的
	 * */
	boolean isValideDrawable()
	{
	  synchronized(this)
	  {
		addRef(); //计数先加一，保证在生成Drawable过程中，不会作为释放对象。
		if( isRecyceled() || null == getDrawable() )		
		{
			subRef();
			return false; 
		}
	  }
	  return true;
	}
	
	/* 尝试回收管理的bitmap	
	 * @return 返回drawable是否是从回收状态恢复了
	 */	
	boolean tryRecycle()
	{	
	  boolean res = false;
	  int size =0;
	  synchronized(this)
	  {
		
		if( isRecyclable() ) 
		{
		
		  if( m_dr != null )
		  {
			TBDrawable td = (TBDrawable) m_dr;
			size = td.bitmapSize();
			res = td.recycle();						
		  }
		  
		}				
	  }
	  
	  if(res)
	  {	
		    //统计回收
			//TaoLog.Logv(TaoLog.IMGPOOL_TAG, "bitmap in ImageHandler recycled  :" + this.m_URI);
		    
			ImagePool.instance().stat.destroyNum++;
			ImagePool.instance().stat.destroySize +=size;
			this.loadState = ImageHandler.LOAD_RECYCLE;				
		 
	  }
	  return res;
	}
	
	
	/* 管理的bitmap的大小	
	 * @return bitmap的大小
	 */	
	int bitmapSize()
	{
		if( m_dr != null )
		 {
			TBDrawable td = (TBDrawable) m_dr;
			return td.bitmapSize();
		 }
		return 0;		
	}
	
	//管理的Drawable引用
	TBDrawable m_dr;
	
	//当前状态
	private int loadState = ImageHandler.NOT_LOADED;
	int getState(){ return loadState; };
	void setState( int s ) 
	{		
		loadState = s;			
	}
			
	/** 是否在Recycle状态	  
	 * @return 返回是否在Recycle状态
	 */
	//check whether the bitmap in this Image handler is Recycled. 
	public synchronized boolean isRecyceled() //sync for m_dr variable
	{
		if( m_dr == null )
		{
			if( this.loadState == ImageHandler.LOAD_RECYCLE  )
			{
				return true;
			} 
			else
				return false;
		}
		else
		{
			Bitmap b = m_dr.getBitmap();
			if( b != null)
			{
				boolean r = b.isRecycled();
				if( r)
				{					
					this.loadState = ImageHandler.LOAD_RECYCLE;
					//TaoLog.Logw(TaoLog.IMGPOOL_TAG, "m_dr is not null for a recycled handler");
				}
				return r;
			}
		}
		return true; //没有bitmap时也应该是回收状态
	}
	
	private String m_URI; //对应的URI，在有选择策略的情况下，这是用户想要的URL，和实际的图片对应的URL可能有差别
	private int m_cachePolicy;

	
	/** 得到ImageHandler管理的Drawable对象
	 * @param converto是一个可选参数，用于将含有的Bitmap做转换时，比如园角。	  
	 * @return Drawable对象
	 */
	public Drawable getDrawable(BitmapConvertor convertor )
	{ 		
		if(m_dr == null)
			return null;
		synchronized(this)
		{
			
			if( loadState == LOADED)
			{
				//转换只做一次，在LOADED状态到CONVERTED状态时
				if(convertor!=null    )
				{
					Bitmap originBitmap = m_dr.getBitmap();										
					Bitmap newBitmap = convertor.convertTo(originBitmap);
					if( null != newBitmap && newBitmap != originBitmap)
					{										
						
						
						TBDrawable newDrawable = new TBDrawable(newBitmap);
						setDrawable(newDrawable,true);
						ImagePool.instance().stat.createNum++;		
						ImagePool.instance().stat.createSize += newDrawable.bitmapSize();
												
						loadState = CONVERTED; 
					}
				}
				
			}										
		}
		return m_dr;
	}
	
	/** 得到ImageHandler管理的Drawable对象 	  
	 * @return Drawable对象
	 */
	public Drawable getDrawable()
	{ 		
		return getDrawable(null);
	}
	
	/** 设置图像的URI	  
	 * @param URI 图像的URI
	 */
	public final void setURI(String URI){m_URI = URI;}
	/** 获得图像的URI	  
	 * @return 图像的URI
	 */	
	public final String URI(){return m_URI;}
	
	/** 获得图像的缓存种类，缓存种类见ImageCache中的常量定义。  
	 * @return 图像的缓存种类
	 */
	public final int getCachePolicy(){return m_cachePolicy;}
	/** 设置图像的缓存种类，缓存种类见ImageCache中的常量定义。	  
	 * @param cp 图像的缓存种类
	 */
	public final void setCachePolicy(int cp){m_cachePolicy = cp;}
		
	//设置Drawable，只在包内使用
	final boolean setDrawable(Drawable drawable, boolean force)
	{
		//TaoLog.Logw(TaoLog.IMGPOOL_TAG ,"trace ih= " + this.viewTag + " , " +  this.hashCode() );
		if( m_dr !=null && m_dr != drawable && m_dr.getBitmap() != ((TBDrawable) drawable).getBitmap())
		{
			Bitmap b = m_dr.getBitmap();
			if( b != null && !b.isRecycled())
			{	
				if( this.isRecyclable() ||force)
				{
					ImagePool.instance().stat.destroyNum ++;
					ImagePool.instance().stat.destroySize += m_dr.bitmapSize();
					m_dr.recycle();
					m_dr.setImageHandler(null);
					
					
				}
				else
				{
					TBDrawable d = (TBDrawable) drawable;
					d.recycle();
					this.printState(false);	
					return false;
				}				
			}
			
			
			
		}
		m_dr = (TBDrawable) drawable;		
		m_dr.setImageHandler(this);
		if( loadState !=  ImageHandler.LOADED)
		{
			setState( ImageHandler.LOADED); 
		}
		else
		{
			//TaoLog.Logw(TaoLog.IMGPOOL_TAG, "already loaded state! " + this.URI());
		}
		return true;
		
	}	
	
	/** 这是一个用于调试的函数，打印出当前ImageHandler类的状态信息	  	 
	 */
	public void printState( boolean bOnlyRefed )
	{
		
	}
	
	/** 这是一个用于调试的函数，打印出当前ImageHandler类的状态信息	  	 
	 */
	public void printState( boolean bOnlyRefed, ImagePoolPrinter printer )
	{
		if( m_refCount > 0 || !bOnlyRefed  ){
			String temp = "ih =" + this + " vt = " + viewTag +  "state =" + this.loadState   + "ref=" + this.m_refCount +",   uri= " + URI();
			if(printer != null)
				printer.printState(temp);
			
		}
	}
	
}
