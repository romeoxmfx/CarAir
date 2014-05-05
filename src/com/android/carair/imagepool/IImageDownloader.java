package com.android.carair.imagepool;

/**
 * IImageDownloader 接口
 * 定义了图片下载类的接口
 * */
public interface IImageDownloader {
	
	//消息常量定义
	public static final int MSG_STOP_DOWNLOAD = 1012;
	public static final int MSG_START_DOWNLOAD = 1013;

	public static final int MSG_QUIT = 1014;
	public static final int MSG_DL_FAILURE 	= 1015;
	public static final int MSG_DL_FINISHED 	= 1016;
	public static final int MSG_DL_INVALIDURL = 1017;
	public static final int MSG_DL_USER_CANCELED = 1018;
	public static final int MSG_DL_PROGRESS 	= 1019;
	public static final int MSG_DL_FAILURE_NOREPEAT 	= 1020; //图片下载失败，且不应重试（如404 FileNotFound引起的）
	
	/** DownloadNotifier 下载结果通知接口
	 * 
	 */
	public interface DownloadNotifier{
		
		/**
		 * 下载结果通知接口
		 * @param msg 下载结果
		 * @param data 下载数据流
		 * @param result url
		 */
		void notify(int msg, byte[] data, String result);
		
		/**
		 * 下载结果通知接口
		 * @param msg 下载结果
		 * @param data 下载数据流
		 * @param result url
		 */
		public void onProgress( String desc, int size, int total);
	}
	
	/**
	 * 用户请求开始下载，并不真正开始下载，只是发送下载命令到下载线程
	 * 
	 */
	public void startDownload();
	
	/**
	 * 停止下载
	 * 
	 */	
	public void stop();
	

	/**
	 * 销毁ImageDownload 
	 * 
	 */
	public void destroy();
	
	/**
	 * 设置需要下载的URL 
	 * @param originURL : 应用指定要下载的图片的地址
	 * @param dlURL: 正在用于下载的地址，由图片策略决定
	 * @param cachePolicy:保存的cache策略
	 */
	public void setURL( String originURL , String dlURL, int cachePolicy);
}
