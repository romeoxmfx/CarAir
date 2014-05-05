package com.android.carair.imagepool;


/**
 * 这个接口定义了图片选择策略
 * 对于不同的分辨率的手机和不同的网络状态，一种灵活的图片选择策略，根据这些条件动态的
 * 在流量和用户体验上平衡。而对应用开发者来说，只要提供基准图片就可以，不需要关心这些实现的细节。
 */
public interface IImageQualityStrategy {
	
	/*
     * 以下是策略的常量定义
     */
	
	 /** 
     * 不采用策略
     */	
	public static int STRATEGY_MODE_OFF = 0;

	 /** 
     * 智能模式
     */	

	public static int STRATEGY_MODE_SMART = 1;
	
	 /** 
     * 低彩模式
     */	
	public static int STRATEGY_MODE_LOW = 2; //低彩模式
	/** 
     * 高清模式
     */	
	public static int STRATEGY_MODE_HIGH = 3; //高清模式
	 /** 根据当时的状态决定最终下载的图片地址
     * @param originUrl 应用传入的图片地址     
     * @return 最终下载的图片地址
     */
	public String decideUrl( String originUrl );
	
	 /** 提取URL中的基础url信息，用于在FileCache中做匹配	       
	 * @param originUrl 应用传入的图片地址     
     * @return 最终下载的图片地址
     */
	public String getBaseUrl(String originUrl );
	
	 /** 在从Cache中加载时选择cache中图片的策略
     * @param originPath 应用传入的图片在Cache的地址
     * @param files 以基础URL和匹配规则在Cache中匹配到的列表，在这个列表中选择适合的加载          
     * @return 从Cache中下载的图片的路径
     */	
	public String decideStoragePath(String originPath, String [] files);
	
	/**
	 * 解析图片的索引信息，可以将分辨率写入imageInfo字段，作为索引信息。
	 * @param originUrl	原始图片url
	 * @return  图片索引信息
	 */
	public ImageIndex toCacheIndex(String originUrl);
	
	/**
	 * 获取匹配的Cache图片
	 * @param cacheImages	缓存中的对应图片ImageIndex中mIdentificacion中的所有图片
	 * @return	匹配的图片信息，将从Cache中获取该图片。
	 */
	public ImageIndex hitCache(String identification,  int imageinfo, int[] cacheImageInfos);
	 /** 设置策略模式
     * @param mode 策略模式 
     */		
	public void setStrategyMode( int mode );	
	
	 /** 返回当前的策略模式
     * @return 策略模式
     */	
	public int getStrategyMode();
	
	
	public String onURLtoCacheFileName( String url);
	
	/**
	 * 图片索引信息，用于存储时的索引，支持同时缓存一张图片的多个分辨率
	 * @author bokui
	 *
	 */
	public static class ImageIndex{
		public String mIdentificacion;
		public int mImageInfo;
	}
	
}
