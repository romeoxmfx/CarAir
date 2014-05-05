package com.android.carair.imagepool;

/**
*图片获取回调接口，由用户实现并提供，把图片获取结果通过调用该接口通知用户 
*/
public interface ImageListener
{		
	//执行结果的常量定义
	public static int OK = 0; //成功
	public static int FAIL = -1; //失败
	public static int FAIL_NO_REPEAT = -2; //失败,且不应该在重试
	/**
	 * feedImage Image下载完毕后的回调，一般调用者收到成功通知后，通过ImagePool.getImageHandler函数获得对应的Handler。
	 * @param res 执行结果
	 * @param URL 图片对应的URL
	 * @param index 图片在ImageGroup中的索引值
	 */
	public void feedImage( int res ,String URL , int index);
	
	/**
	 * 下载进度通知
	 * @param desc 说明
	 * @param size 下载字节
	 * @param total 总字节
	 */
	void onProgress( String desc, int size, int total,String url,int index);

}