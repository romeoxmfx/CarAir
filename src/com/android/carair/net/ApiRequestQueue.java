package com.android.carair.net;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * ApiRequestQueue 类 基于FutureTask和线程池实现的排队机制
 */
class ApiRequestQueue {
	public static final int DEFAULT_CONCURRENT_SIZE = 4;
	ExecutorService m_ExecPool = null; // 这是用于并发的Pool
	ExecutorService m_ExecSinglePool = null; // 这是用于排队的的Pool
	boolean mbInited = false;
	boolean mbDestroy = false;
	int mSize = DEFAULT_CONCURRENT_SIZE;

	/**
	 * ApiRequestQueue 构造函数
	 * 
	 */
	public ApiRequestQueue() {
	}

	private synchronized void _InitExecPool() {
		if (mbInited)
			return;

		mbInited = true;
		mbDestroy = false;

		if (null == m_ExecPool) {
			m_ExecPool = new ThreadPoolExecutor(mSize, 1000, 0L, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<Runnable>());

			m_bConMode = true;
		}

		if (null == m_ExecSinglePool) {
			m_ExecSinglePool = new ThreadPoolExecutor(1, 1000, 0L, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<Runnable>());

		}

	}

	// 用于与FutureTask一起工作的内部类
	private class ApiExecutor implements Callable<ApiResult> {

		ApiConnector mApiConn;
		ApiProperty mProperty;

		public ApiExecutor(ApiConnector connector, ApiProperty property) {
			mApiConn = connector;
			mProperty = property;
		}

		@Override
		public ApiResult call() throws Exception {

			Object cacheData = ApiCache.getInstance().getCacheData(mProperty.getCacheKey(), mProperty.getCachePolicy(),
					mProperty.getCacheStoragePolicy());
			if (cacheData != null && cacheData.getClass() == ApiResult.class) {
				// 从Api的Cache获得结果
				// ApiResult res = new ApiResult( HttpStatus.SC_OK,"SUCCESS:SUCCESS",null);
				// res.data = cacheData;
				mApiConn.notifyDataArrive((ApiResult) cacheData);
				return (ApiResult) cacheData;
			}

			ApiResult res = mApiConn.syncConnect();
			mApiConn.notifyDataArrive(res);
			return res;
		}

	}

	/**
	 * 请求加入队列
	 * 
	 * @param connector
	 *            ApiConnector对象
	 * @return Future对象
	 */
	Future<ApiResult> addRequest(ApiConnector connector, ApiProperty property) {

		_InitExecPool();
		if (m_bConMode) {
			return m_ExecPool.submit(new ApiExecutor(connector, property));
		}
		else {
			return m_ExecSinglePool.submit(new ApiExecutor(connector, property));
		}
	}

	boolean m_bConMode;

	// 切换到并发和窜行
	void setConcurrentMode(boolean bConMode) {

		if (bConMode == m_bConMode)
			return;

		m_bConMode = bConMode;

	}

	/**
	 * setConcurrentConnLimit 设置并发的连接数
	 * 
	 * @param size
	 *            并发数 注：必须是在初始化阶段调用，不能在已经提交任务
	 */
	void setConcurrentConnLimit(int size) {
		if (null != m_ExecPool)
		mSize = size;
	}

	/**
	 * destroy 清理资源
	 */
	synchronized void destroy() {
		if (mbDestroy)
			return;

		mbDestroy = true;
		mbInited = false;

		try {
			if (null != m_ExecPool) {
				m_ExecPool.shutdown();
				m_ExecPool = null;
			}
			if (null != m_ExecSinglePool) {
				m_ExecSinglePool.shutdown();
				m_ExecSinglePool = null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}