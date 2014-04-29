package com.android.carair.net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.HttpStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.taobao.munion.utils.MuLogUtil;
import com.taobao.munion.utils.SafeHandler;

/**
 * ApiRequestMgr ApiRequest包的对外接口类，提供如下功能 a. 提供同步和异步的执行网络请求的方式 b.
 * 支持排队队列，控制并发连接个数 c. 上传文件，包括进度通知 d. 支持API的客户端Cache e.
 * 支持失败重试 f. 支持重定向 g. 支持GZIP压缩 h. DNS获取优化 i. 数据统计
 */
public class ApiRequestMgr implements Callback
{

	// 优先级的常量定义

	public static final int PRIORITY_IMM = 1; // 马上执行
	public static final int PRIORITY_NOR = 2; // 在线程池中执行
	public static final int PRIORITY_BG = 3; // 在单任务队列中，窜行执行

	// --singleton
	private static ApiRequestMgr m_instance = null;
	Context m_Context;
	private boolean mIsRegister = false;

	SafeHandler handler = new SafeHandler(Looper.getMainLooper(), this);

	public static ApiRequestMgr getInstance()
	{
		if (null == m_instance)
		{
			m_instance = new ApiRequestMgr();
		}
		return m_instance;
	}

	private ApiRequestMgr()
	{
		mSingleRequestQueue.setConcurrentConnLimit(1);

	}

	boolean isNetworkAvailable(Context context)
	{
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		if (networkInfo != null)
		{
			return networkInfo.isAvailable();
		}
		return false;
	}
	
	Context getContext()
	{
		return m_Context;
	}

	// 根据当前的网络连接调整并发状态
	public void UpdateNetworkStatus()
	{
		if (m_Context == null)
		{
			return;
		}
		ConnectivityManager connManager = (ConnectivityManager) m_Context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();
		if (info != null)
		{
			if (info.getType() == ConnectivityManager.TYPE_MOBILE)
			{
				// 2G网络
				// 电信2G是 NETWORK_TYPE_CDMA
				// 移动2G卡 2 NETWORK_TYPE_EDGE
				// 联通的2G 1 NETWORK_TYPE_GPRS
				if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS || info.getSubtype() == TelephonyManager.NETWORK_TYPE_CDMA
						|| info.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE)
				{
					mRequestQueue.setConcurrentMode(false);
					return;
				}
				else
				{
					// >3G
					mRequestQueue.setConcurrentMode(true);
					return;
				}
			}
			else if (info.getType() == ConnectivityManager.TYPE_WIFI)
			{
				mRequestQueue.setConcurrentMode(true);
				return;
			}
		}
		mRequestQueue.setConcurrentMode(false);
	}

	private BroadcastReceiver networkStateReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub

			// 不论连上还是断掉都是发这个通知的,此时检查网络状态
			UpdateNetworkStatus();
		}
	};

	/**
	 * 设置连接的超时时间，当超时发生时，下次时间就会增加一个Step，直到Max为止
	 * 
	 * @param min
	 *                是起始的超时时间，单位毫秒
	 * @param step
	 *                每次增加的毫秒数
	 * @param max
	 *                最大的毫秒数
	 */
	public void setTimeout(int min, int step, int max)
	{
		m_curTimeout = m_minTimeout = min;
		m_stepTimeout = step;
		m_maxTimeout = max;

	}

	static int MOBILE_DEFAULT_START_TIMEOUT = 5000;
	static int MOBILE_DEFAULT_MAX_TIMEOUT = 11000;
	int m_minTimeout = MOBILE_DEFAULT_START_TIMEOUT;
	int m_curTimeout = MOBILE_DEFAULT_START_TIMEOUT;
	int m_stepTimeout = 3000;
	int m_maxTimeout = MOBILE_DEFAULT_MAX_TIMEOUT;

	int m_baseTime = 30000; // 2G如果有飞流这样的代理的话，链接要慢很多，增加一个Base时间，Wifi 15s ,2G
				// 30s

	int getTimeout()
	{
		return m_curTimeout + m_baseTime;
	}

	void onConnectSucc(long time)
	{
		m_curTimeout = MOBILE_DEFAULT_START_TIMEOUT;
	}

	void onTimeout()
	{
		if (m_curTimeout < m_maxTimeout) m_curTimeout += m_stepTimeout;
		else m_curTimeout = m_maxTimeout;
	}

	/**
	 * 初始化ApiRequest模块，在使用前调用
	 */
	public void init(Context context)
	{
		m_Context = context;

		// 监听网络变化
		try
		{
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			context.registerReceiver(networkStateReceiver, filter);
			mIsRegister = true;
			UpdateNetworkStatus();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 释放ApiRequest模块的资源，在退出程序时调用
	 */
	public void destroy()
	{
		try
		{
			if (m_Context != null && mIsRegister)
			{

				m_Context.unregisterReceiver(networkStateReceiver);
				mIsRegister = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// --同步调用--
	/**
	 * 同步连接（不带Cache支持）
	 * 
	 * @param requestUrl
	 *                API请求的URL地址
	 * @param property
	 *                连接用到的属性
	 * @return ApiResult 包括结果值，描述，数据流和服务端提供的过期时间
	 */
	public ApiResult syncConnect(String requestUrl, ApiProperty property)
	{

		ApiConnector conn = new ApiConnector(requestUrl, property);
		// 检查同时连接限制
		ApiProperty prop = conn.getApiProperty();
		switch (prop.getPriority())
		{
			case PRIORITY_BG:
			{
				Future<ApiResult> ft = mSingleRequestQueue.addRequest(conn, property);
				if (null == ft)
				{
					return new ApiResult(ErrorConstant.STSTEM_ERROR, "mSingleRequestQueue return null", null);
				}
				ApiResult response;
				try
				{
					response = ft.get();
					return response;
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					// Adv.onCaughException(e);
					return new ApiResult(ErrorConstant.API_RESULT_UNKNOWN, e.getMessage(), null);
				}
				catch (ExecutionException e)
				{
					e.printStackTrace();
					// Adv.onCaughException(e);
					return new ApiResult(ErrorConstant.API_RESULT_UNKNOWN, e.getMessage(), null);
				}
			}

			case PRIORITY_NOR:
			{
				Future<ApiResult> ft = mRequestQueue.addRequest(conn, property);
				ApiResult response;
				try
				{
					response = ft.get();
					return response;
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					// Adv.onCaughException(e);
					return new ApiResult(ErrorConstant.API_RESULT_UNKNOWN, e.getMessage(), null);
				}
				catch (ExecutionException e)
				{
					e.printStackTrace();
					// Adv.onCaughException(e);
					return new ApiResult(ErrorConstant.API_RESULT_UNKNOWN, e.getMessage(), null);
				}
			}

			case PRIORITY_IMM:
				return conn.syncConnect();
		}
		if (prop.getPriority() != PRIORITY_IMM)
		{
			Future<ApiResult> ft = mRequestQueue.addRequest(conn, property);
			ApiResult response;
			try
			{
				response = ft.get();
				return response;
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				// Adv.onCaughException(e);
				return new ApiResult(ErrorConstant.API_RESULT_UNKNOWN, e.getMessage(), null);
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
				// Adv.onCaughException(e);
				return new ApiResult(ErrorConstant.API_RESULT_UNKNOWN, e.getMessage(), null);
			}
		}
		else return conn.syncConnect();
	}

	/**
	 * 同步连接使用缺省的参数（不带Cache支持）
	 * 
	 * @param requestUrl
	 *                API请求的URL地址
	 * @return ApiResult 包括结果值，描述，数据流和服务端提供的过期时间
	 */
	public ApiResult syncConnect(String requestUrl)
	{
		return syncConnect(requestUrl, null);
	}

	/**
	 * 同步连接。与ConnectorHelper配合使用的，带Cache支持。
	 * 
	 * @param ch
	 *                调用者提供的ConnectorHelper的实现，包括获得API地址和解析返回数据流功能
	 * @return ApiResult 包括结果值，描述，数据流和服务端提供的过期时间
	 *         注：提供这个接口的调用方式兼容历史的ConnectorHelper机制
	 */
	public Object syncConnect(ConnectorHelper ch, ApiProperty property)
	{
		if (property == null) property = new ApiProperty();
		String url = ch.getApiUrl();
		ApiResult res = syncConnect(url, property);
		if (!res.isSuccess())
		{
			return ch.syncPaser(new byte[0]);
		}

		Object obj = ch.syncPaser(res.bytedata);

		// 数据流解析后置为null；
		res.bytedata = null;
		return obj;
	}

	/**
	 * asyncConnect 异步调用,不带Cache支持
	 * 
	 * @param requestUrl
	 *                API调用的URL
	 * @param callback
	 *                完成时的回调
	 * @param property
	 *                连接的属性
	 * @return 返回用于cancel的标识
	 */
	public ApiID asyncConnect(String requestUrl, AsyncDataListener callback, ApiProperty property)
	{
		if (property == null) property = new ApiProperty();

		ApiConnector conn = new ApiConnector(requestUrl, property);
		conn.setDataListener(callback);
		Future<ApiResult> ft = mRequestQueue.addRequest(conn, property);
		return new ApiID(ft, conn);
	}

	class CallbackObject
	{
		public AsyncDataListener callback;
		public ApiResult result;
	}

	/**
	 * 异步回调
	 * 
	 * @author xuanjue.hk
	 * @date 2012-12-21
	 */
	class CacheSuccessedAsyncCallback implements Runnable
	{
		private CallbackObject data;

		public CacheSuccessedAsyncCallback(CallbackObject data)
		{
			this.data = data;
		}

		@Override
		public void run()
		{
			Message msg = Message.obtain();
			msg.obj = data;
			handler.sendMessage(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler.Callback#handleMessage(android.os.Message)
	 */
	@Override
	public boolean handleMessage(Message arg0)
	{
		CallbackObject object = (CallbackObject) arg0.obj;
		if (object != null && object.callback != null)
		{
			object.callback.onDataArrive(object.result);
			return true;
		}
		return true;
	}

	/**
	 * asyncConnect 异步调用,使用缺省参数,不带Cache支持
	 * 
	 * @param requestUrl
	 *                API调用的URL
	 * @param callback
	 *                完成时的回调
	 * @return 返回用于cancel的标识
	 */
	public ApiID asyncConnect(String requestUrl, AsyncDataListener callback)
	{
		return asyncConnect(requestUrl, callback, null);
	}

	/**
	 * CHDataListener辅助类 用于ConnectorHelper方式的异步调用
	 */
	private class CHDataListener implements AsyncDataListener
	{
		AsyncDataListener mListener;
		ConnectorHelper mCh;
		ApiProperty mProperty;

		CHDataListener(AsyncDataListener listener, ConnectorHelper ch, ApiProperty property)
		{
			mListener = listener;
			mCh = ch;

			mProperty = property;
		}

		@Override
		public void onProgress(String desc, int size, int total)
		{
			if (null != mListener) mListener.onProgress(desc, size, total);

		}

		@Override
		public void onDataArrive(ApiResult res)
		{

			if (res.isSuccess() && mCh != null)
			{

				ApiResult obj = (ApiResult) mCh.syncPaser(res.bytedata); // 必须是返回ApiResult的

				if (obj != null)
				{
					// 成功则保存结果
					int expireTime = res.expireTime < mProperty.expireTime ? res.expireTime : mProperty.expireTime;
					ApiCache.getInstance().setCacheData(mProperty.getCacheKey(), obj, mProperty.getCachePolicy(),
							mProperty.getCacheStoragePolicy(), expireTime);
				}
				// 数据流解析后置为null；
				res.bytedata = null;
				res = obj;
			}
			if (null != mListener) mListener.onDataArrive(res);
		}

	}

	/**
	 * asyncConnect Api的异步调用,带Cache支持
	 * 
	 * @param ConnectorHelper
	 *                API调用的ConnectorHelper类
	 * @param callback
	 *                完成时的回调
	 * @param property
	 *                连接的属性
	 * @return 返回用于cancel的标识
	 */
	public ApiID asyncConnect(ConnectorHelper ch, AsyncDataListener callback, ApiProperty property)
	{
		if (property == null) property = new ApiProperty();
		ApiConnector conn = new ApiConnector(ch.getApiUrl(), property);
		conn.setDataListener(new CHDataListener(callback, ch, property));
		Future<ApiResult> ft = mRequestQueue.addRequest(conn, property);
		return new ApiID(ft, conn);
	}

	/**
	 * downloadFile 下载文件到指定路径
	 * 
	 * @param requestUrl
	 *                文件的URL
	 * @param callback
	 *                完成和进度的回调
	 * @param storagePath
	 *                本地文件
	 * @return 返回用于cancel的标识 注：这是一个异步函数,下载是续传式的，即从当前文件的末尾开始真假。
	 */

	private class AsyncDAsyncDataLstProxy implements AsyncDataListener
	{
		AsyncDAsyncDataLstProxy(AsyncDataListener lst, OutputStream ostream)
		{
			m_lst = lst;
			m_ostream = ostream;
		}

		AsyncDataListener m_lst;
		OutputStream m_ostream;

		@Override
		public void onProgress(String desc, int size, int totoal)
		{

			if (null != m_lst) m_lst.onProgress(desc, size, totoal);
		}

		@Override
		public void onDataArrive(ApiResult res)
		{

			if (res.resultCode == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE)
			{
				// 应该是已经完成了
				res = new ApiResult(HttpStatus.SC_OK, "success", null);
			}

			if (m_ostream != null)
			{
				try
				{
					m_ostream.flush();
					m_ostream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (null != m_lst) m_lst.onDataArrive(res);
		}

	}

	public ApiID downloadFile(String requestUrl, AsyncDataListener callback, String storagePath) throws FileNotFoundException
	{
		ApiProperty prop = new ApiProperty();
		prop.setPriority(PRIORITY_BG);
		File f = new File(storagePath);
		prop.m_startPos = f.length();
		prop.m_downMaxSize = 500 * 1024 * 1024;
		prop.m_outStream = new FileOutputStream(f, true);
		prop.m_ProgressStep = 20;
		return asyncConnect(requestUrl, new AsyncDAsyncDataLstProxy(callback, prop.m_outStream), prop);
	}

	public ApiID downloadImage(String requestUrl, AsyncDataListener callback) throws FileNotFoundException
	{

		// 2G network
		ApiProperty prop = new ApiProperty();
		prop.setPriority(PRIORITY_NOR);
		prop.m_downMaxSize = 1024 * 1024;
		prop.m_ProgressStep = 10;
		return asyncConnect(requestUrl, callback, prop);
	}

	/**
	 * downloadFile 下载文件到指定路径
	 * 
	 * @param requestUrl
	 *                服务端目标URL
	 * @param callback
	 *                完成和进度的回调
	 * @param storagePath
	 *                本地文件路径
	 * @return 返回用于cancel的标识 注：这是一个异步函数
	 */
	public ApiID uploadFile(String requestUrl, AsyncDataListener callback, String storagePath)
	{
		// TODO:post a file
		// ApiProperty prop = new ApiProperty();
		// prop.setCachePolicy(PRIORITY_LOW);
		// return asyncConnect(requestUrl,callback,prop);
		return null; // upload is unimplemented
	}

	/**
	 * cancelConnect 取消异步的连接操作
	 * 
	 * @param o
	 *                异步调用返回的标识
	 * @return 是否取消成功
	 */
	public boolean cancelConnect(ApiID id)
	{
		ApiConnector conn = id.m_conn;
		if (null != conn)
		{
			conn.cancel();
		}
		Future<ApiResult> ft = (Future<ApiResult>) id.m_future;
		if (null != ft) return ft.cancel(true);
		return false;
	}

	// 链接相关统计信息的回调
	/**
	 * setStautsListener 设置API完成情况的回调接口
	 * 
	 * @param listener
	 *                回调 注：所有ApiConnector完成后调用的回调接口，一般通过实现这个接口来做数据统计
	 */
	public static void setStatusListener(ApiConnectorStatusListener listener)
	{
		ApiConnector.setStatusListener(listener);
	}

	ApiRequestQueue mRequestQueue = new ApiRequestQueue(); // 优先级为PRIORITY_NOR的执行队列，这是一个在2G下单线程顺序执行，WIFI下并发执行的队列
	ApiRequestQueue mSingleRequestQueue = new ApiRequestQueue(); // 优先级为PRIORITY_BG的执行队列，这是一个单线程顺序执行的队列

	/**
	 * setConcurrentConnLimit 设置并发的连接数
	 * 
	 * @param size
	 *                并发数
	 */
	public void setConcurrentConnLimit(int size)
	{
		mRequestQueue.setConcurrentConnLimit(size);
	}

	/*
	 * ApiStatics 做连接相关的统计的内部类
	 */
	class ApiStatics
	{
		int successNum = 0;
		int failNum = 0;

		void onSuccess(int url, int size, int time)
		{

		}

		void onFail(int url, String desc, int time)
		{

		}
	}

}
