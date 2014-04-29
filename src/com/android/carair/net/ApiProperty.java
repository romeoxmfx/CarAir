package com.android.carair.net;

import java.io.OutputStream;
import java.util.Map;

/**	 
 * ApiProperty 
 * ApiProperty，用于属性的设置
 */
public class ApiProperty
{		
	//缺省值常量定义
//	public static final int DEFAULT_TIMEOUT_STEP = 5000;
	public static final boolean DEFAULT_AUTO_REDIRECT = true;				
//	public static int DEFAULT_CONNECT_TIMEOUT = 5000;
//	public static int DEFAULT_READ_TIMEOUT = 5000;
	

	public static final int DEFAULT_RETRYTIMES =3;
	public static final int DEFAULT_CLIENT_EXPIRETIME = 365*24*3600; //缺省365天，单位秒,
	private static final int DEFAULT_DOWN_MAX_SIZE = 3*1024*1024; //3M,对API调用够用了，对下载文件自己设置
	private static final int DEFAULT_STEP_PERCENT = 10; //控制通知进度的频度，每次完成总长度的多少
	
	//connect属性
	/**	 
	 * m_redirectAuto 是否自动重定向		 
	 */
	protected boolean m_redirectAuto;
	
	/**	 
	 * m_connectTimeout 连接超时时间		 
	 */
	//protected int m_connectTimeout;
	
	/**	 
	 * m_readTimeout 读数据的超时时间	 
	 */
	//protected int m_readTimeout;
	
	/**	 
	 * m_priority API的优先级 
	 */
	protected int m_priority;	
	
	/**	 
	 * m_retryTime 重试次数 
	 */
	protected int m_retryTime;
	
	protected boolean m_bTop = false;
	/**	 
	 * m_postData 如果是Post操作，则这里是POST的数据 
	 */
	protected byte[] m_postData;
	
	/**	 
	 * m_bPost 是否是一个Post操作的API 
	 */
	protected boolean m_bPost;
	
	/**	 
	 * m_getData 如果是Get操作，则这里是Get的数据 
	 */
	protected String m_getData;
	
	/**	 
	 * m_connHeaders 加入到Connection的Header 
	 */
	protected Map<String, String> m_connHeaders;
	
	/**	 
	 * m_cacheStorage 该API的Cache存储策略：PERSIST或者程序退出前有效 
	 */
	protected int m_cacheStorage;
	
	/**	 
	 * m_cacheStorage 该API的Cache策略 
	 */
	protected int m_cachePolicy;
	
	protected String m_cacheKey; //API Cache的Key值。
	/**	 
	 * expireTime 客户端设置的超时时间 
	 */		
	int expireTime;
	
	/**	 
	 *  开始下载的位置，用于断点续传 
	 */		
	long m_startPos;
			
	/**	 
	 *  数据边下边输出 
	 *  一般API结果应该是true
	 *  文件下载应该是false
	 */				
	OutputStream m_outStream;
	
	/**这是下载的最大值限制
	 * 
	 * */
	int m_downMaxSize; 
	
	/**这是下载的最大值限制
	 * 
	 * */
	int m_ProgressStep; //每完成20分之一通知
	
	//重定向终点
	protected String m_finalRedirectHost; 
	
	public ApiProperty()
	{
		m_redirectAuto = DEFAULT_AUTO_REDIRECT;
		//m_connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		//m_readTimeout = DEFAULT_READ_TIMEOUT;
		m_priority = ApiRequestMgr.PRIORITY_IMM;			
		m_retryTime = DEFAULT_RETRYTIMES;
		m_bTop = false;
		m_bPost = false;
		m_postData = null;
		m_connHeaders =null;
		m_cacheStorage = ApiCache.API_CACHE_STORAGE_PERSIST;
		m_cachePolicy = ApiCache.API_CACHE_POLICY_DoNotReadFromCacheCachePolicy|ApiCache.API_CACHE_POLICY_DoNotWriteToCacheCachePolicy; //缺省关闭Cache，因为现在Cache并没有实现			
		expireTime = DEFAULT_CLIENT_EXPIRETIME;
		m_startPos = 0;
		m_outStream = null;
		m_downMaxSize = DEFAULT_DOWN_MAX_SIZE;
		m_ProgressStep = DEFAULT_STEP_PERCENT;
		m_finalRedirectHost = null;
		
	}
	
	public boolean isTop() {
		return m_bTop;
	}

	public void setTop(boolean m_bTop) {
		this.m_bTop = m_bTop;
	}
	
	public boolean getTop() {
		return this.m_bTop;
	}
	
	public static boolean isDefaultAutoRedirect() {
		return DEFAULT_AUTO_REDIRECT;
	}

	public long getStartPos() {
		return m_startPos;
	}

	public void setStartPos(long m_startPos) {
		this.m_startPos = m_startPos;
	}

	public void setFollowRedirects(boolean auto) {
		m_redirectAuto = auto;
	}
	public boolean getFollowRedirects() {
		return m_redirectAuto;
	}	
	
//	public void setConnectTimeOut(int timeout){
//		m_connectTimeout = DEFAULT_CONNECT_TIMEOUT;
//	}
//	
//	public void setReadTimeout(int timeout){
//		m_readTimeout = timeout;
//	}
	
	public void setPriority(int p)
	{
		m_priority = p;
	}
	
	public int getPriority()
	{
		return m_priority;
	}
	
	public void setCacheStoragePolicy( int policy )
	{
		m_cacheStorage = policy;
	}
	public int getCachePolicy()
	{
		return m_cachePolicy;
	}
	public void setCachePolicy( int policy)
	{
		m_cachePolicy = policy;
	}
	
	
	public int getCacheStoragePolicy()
	{
		return m_cacheStorage;
	}
	
	public String getCacheKey()
	{
		return m_cacheKey;
	}
	
	public void setCacheKey(String cacheKey )
	{
		m_cacheKey = cacheKey;
	}
	public void setRetryTimes( int times)
	{
		m_retryTime = times;
	}
	public int getRetryTimes()
	{
		return m_retryTime;
	}
	
	public void setConnectionHeader( Map<String, String> headers )
	{
		m_connHeaders =headers;
	}
	
	public Map<String, String>  getConnectionHeader( )
	{
		return m_connHeaders;
	}
	
	public byte[] getPostData()
	{
		return m_postData;
	}
	
	public void setPostData( byte [] postData)
	{
		m_postData = postData;
		setPost(true);
	}
	
	public void setPost( boolean bPost )
	{
		m_bPost = bPost;
	}
	
	public String getGetData()
	{
		return m_getData == null?"":m_getData;
	}
	
	public void setGetData(String data)
	{
		m_getData = data;
	}

	public String geFinalRedirectHost()
	{
		return m_finalRedirectHost;
	}

	public void setFinalRedirectHost(String finalRedirectHost)
	{
		this.m_finalRedirectHost = finalRedirectHost;
	}
}
