
package com.android.carair.imagepool;

import com.android.carair.net.ApiResult;
import com.android.carair.net.AsyncDataListener;
import com.android.carair.net.ErrorConstant;

import android.app.Application;

/**
 * 
 * 	ImageDownloader
 * 放入ImagePool框架中，简化原有功能
 * 1. 提供从网络中一次下载一张图片的功能（不再支持队列和缓存获取）
 * 2. 提供下载重试功能和次数设置
 * 3. 提供下载控制：开始，暂停，取消
 * 4. 下载完成的图片不做任何处理的以字节流传给ImagePool
 * 
 * Phase Two by Steve.	2011.09.24
 * 		1, support generic ArrayList<String> URL input;
 * 		2, support retry for downloading failure case;
 * 		3, internal thread support.
 *		4, load image from cache directly
 * 
 * @Change 
 * 			Fix bug for substring crash if image url length is smaller than SUB_STRING_STARTINDEX + SUB_STRING_OFFSET. 
 * @change 
 * 关于临界区的设置
 * 1. 因为下载的时候可以被用户取消，所以HttpURLConnection可以在下载线程进行接受的时候被用户线程所取消，它不能在临界区内被互斥访问，应可以被线程共享
 * 
 * 注意：不要直接使用该类，请使用@ImageGroup来获取图片服务
 */

public class ImageDownloader implements IImageDownloader, AsyncDataListener{




    //-------------------------------+生命周期控制+--------------------------------------------------------------------------
	//生命周期常量定义
	/*
	private static final int STATE_PROCESS = 1;
	private static final int STATE_IDLE = 2;
	private static final int STATE_USER_STOP = 3;
	private static final int STATE_DOWNLOADING = 4;
	
	private int mState = STATE_IDLE;
	*/

    /** 构造函数
     * @param receiver Download回调消息的处理者
     * @param context context对象
     * */
	public ImageDownloader(DownloadNotifier receiver,  Application context )
	{	
		this.mReceiver = receiver;
	}
	
	/**
	 * 下载请求到网络层队列
	 * 
	 */
	public void startDownload()
	{
//		TaoLog.Logi(TaoLog.IMGPOOL_TAG, "ImageDownload, startDownload() ");
		long start = System.nanoTime();
				

//		if(!isAvailableURL(m_URL)){
//			if( mReceiver != null )
//				mReceiver.notify(MSG_DL_INVALIDURL, null, m_URL);
//	
//
//		}else{
			
			doDownload();
			
//		}
		
		//时间
	}
	
	/**
	 * 停止下载
	 * 
	 */	
	public void stop(){
		
	}
	
	/*
	 * destroy the instance before set null
	 * The thread will be stopped there.
	 * 
	 * */
	
	/**
	 * 销毁ImageDownload 
	 * 
	 */
	public void destroy()
	{
		stop();	
	}
	
	
    //*******************************-生命周期控制-**************************************************************************
	
    //-------------------------------+URL+----------------------------------------------------------------------------------
    private String m_URL; //实际下载的URL
    private String m_originURL; //应用指定要下载的图片的地址
    
    /**
	 * 设置需要下载的URL 
	 * @param originURL : 应用指定要下载的图片的地址
	 * @param dlURL: 正在用于下载的地址，由图片策略决定
	 * @param cachePolicy:保存的cache策略
	 */
    public final void setURL( String originURL , String dlURL, int cachePolicy)
    {
    	
    	if((m_URL == dlURL)||((m_URL != null) && m_URL.equals(dlURL)))
    		return;    	
    	stop();
    	
    	synchronized(this)
    	{
	    	m_URL = dlURL;
	    	m_originURL = originURL;
	    	
    	}
    	
    }
    
	/**
	 * 判断是否有效的url
	 * @param url
	 * @return 是否有效
	 */
	private static boolean isAvailableURL( String url )
	{
		boolean bValid = false;
		if( url == null )
			return false;
		
		if( url.concat("http://") != null )
			bValid = true;
	
		if( ImagePool.m_picPattern != null)
		{
			//对两个域名做特殊处理
			if(!ImagePool.m_picPattern.matcher(url).matches() || url.contains("a.tbcdn") || url .contains("b.tbcdn")){
				//非cdn图片，提示
			}
			else{
				if(false==url.contains(".webp")//非webp格式
						&&false==url.contains("x")//没有尺寸
						){
				}
			}
		}
		return bValid;
	}
 		
	/**
	 * 执行下载过程
	 * @return
	 */
	private boolean doDownload()
	{
		
		
		try{
									
			return false; 		
		
		}catch(Exception e){
			e.printStackTrace();
			finishDownload( new ApiResult(ErrorConstant.API_RESULT_FAILED) , null); //trig the download failed!
		}
		
		
		return true;
	}

	
	
	private DownloadNotifier mReceiver = null;
		
	
	/**
	 * 下载结束处理
	 */
	@SuppressWarnings("deprecation")
	private boolean finishDownload(ApiResult res, byte[] buffer)
	{

				
		if( mReceiver == null )
		{
		}
		

		//Send out 
		if( mReceiver != null )
		{
			
			
			String originURI =this.m_originURL;
			
			if(( buffer == null || buffer.length == 0) ){
				
				if( res.resultCode >= 400 ) //400以上的response code 重试也会出错
				{
					mReceiver.notify(MSG_DL_FAILURE_NOREPEAT, buffer, originURI);
					return true;
				}
				switch( res.resultCode )
				{
//					case ErrorConstant.API_RESULT_SPDY_ERROR: 
					case ErrorConstant.API_RESULT_NETWORK_ERROR:
					case ErrorConstant.API_RESULT_REDIRECT_MANY:
					case ErrorConstant.API_RESULT_TOO_LARGE_RESPOSE:						
						mReceiver.notify(MSG_DL_FAILURE_NOREPEAT, buffer, originURI);
						break;
					default:
						mReceiver.notify(MSG_DL_FAILURE, buffer, originURI);
						break;
				};				
			}else{
				mReceiver.notify(MSG_DL_FINISHED, buffer, originURI);
			}
		}		
		return true;
	}
	//******************************-回调通知-******************************************************************************

	@Override
	public void onProgress(String desc, int size, int total) {
		// TODO Auto-generated method stub
		mReceiver.onProgress(desc, size, total);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onDataArrive(ApiResult res) {
		// TODO Auto-generated method stub
		
			
		byte[] buffer = (byte[]) res.getBytedata();
		if( buffer == null || buffer.length <= 0 )
		
		 
		//下载完成，通知
		finishDownload( res ,buffer);
		
	}

}
