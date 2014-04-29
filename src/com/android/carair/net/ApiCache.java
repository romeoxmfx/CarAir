package com.android.carair.net;

import java.util.ArrayList;
import java.util.HashMap;
import android.content.Context;

/**
 * ApiCache类 对API的结果的各种策略的Cache支持，当命中Cache时可以减少网络操作的流量和等待时间；支持离线浏览等； 这里Cache管理的是DO对象。Cache提供各种策略。
 * 只在服务端成功返回数据且客户端成功解析出数据对象的情况下，才缓存到Cache中。 服务端可以指定API返回数据的有效时间；客户端也可以更具业务指定短于服务端时间的值。
 */

public class ApiCache {

	// context

	/**
	 * 这里定义Cache的策略的常量，对应ApiProperty变量 m_cacheStorage
	 */
	public static final int API_CACHE_POLICY_DoNotReadFromCacheCachePolicy = 1; // 所读数据不使用缓存
	public static final int API_CACHE_POLICY_DoNotWriteToCacheCachePolicy = 2; // 不对缓存数据进行写操作
	public static final int API_CACHE_POLICY_AskServerIfModifiedWhenStaleCachePolicy = 4; // request会先判断是否存在缓存数据。a,
																							// 如果没有再进行网络请求。
																							// b，如果存在缓存数据，并且数据没有过期，则使用缓存。c，如果存在缓存数据，但已经过期，request会先进行网络请求，判断服务器版本与本地版本是否一样，如果一样，则使用缓存。如果服务器有新版本，会进行网络请求，并更新本地缓存
	public static final int API_CACHE_POLICY_AskServerIfModifiedCachePolicy = 8; // 与API_CACHE_POLICY_AskServerIfModifiedWhenStaleCachePolicy区别，每次请求都会
																					// 去服务器判断是否有更新
	// public static final int API_CACHE_POLICY_OnlyLoadIfNotCachedCachePolicyy = 16; //如果有缓存在本地，不管其过期与否，总会拿来使用
	public static final int API_CACHE_POLICY_FallbackToCacheIfLoadFailsCachePolicy = 32; // 这个选项经常被用来与其它选项组合使用。请求失败时，如果有缓存当网络则返回本地缓存信息（这个在处理异常时非常有用）

	/**
	 * 这里定义Cache的保存策略的常量, 对应ApiProperty对应变量 m_cacheStorage
	 */
	public static final int API_CACHE_STORAGE_PERSIST = 1; // 永久
	public static final int API_CACHE_STORAGE_SESSION = 2; // 程序未退出前有效

	// ApiCache是全局唯一的
	private static ApiCache m_instance = null;

	public static ApiCache getInstance() {
		if (null == m_instance) {
			m_instance = new ApiCache();
		}
		return m_instance;
	}

	private ApiCache() {

	}

	class MetaData {
		MetaData(Object doObject, int storagePolicy, long expirePoint) {
			m_doObject = doObject;
			m_storagePolicy = storagePolicy;
			m_expirePoint = expirePoint;
		}

		long m_expirePoint;
		int m_storagePolicy;

		Object m_doObject;
	}

	// 当前版本是用内存Cache来实现的，临时版本
	private final HashMap<String, MetaData> m_cacheHash = new HashMap<String, MetaData>();
	private final ArrayList<String> m_cacheKeyList = new ArrayList<String>();
	final int MAX_ITEM = 20;

	/**
	 * 初始化ApiCache
	 * 
	 * @param context
	 *            程序的Context，用于初始化
	 * @return 是否初始化成功
	 */
	public boolean init(Context context) {

		return true;
	}

	/**
	 * destroy 释放资源
	 * 
	 * @return 是否成功
	 */
	public boolean destroy() {
		return true;
	}

	/**
	 * 返回指定策略中对应Key的数据
	 * 
	 * @param key
	 *            数据对于的唯一key，这里一般是API的URL
	 * @param storagePolicy
	 *            cache保存策略
	 * @return Object 对应于Key，存在在cache中的DO对象；无数据是返回null
	 */
	public Object getCacheData(String key, int cachePolicy, int storagePolicy) {
		if (null == key)
			return null;

		// 策略判断，不需要从Cache取
		if (API_CACHE_POLICY_DoNotReadFromCacheCachePolicy == (cachePolicy & API_CACHE_POLICY_DoNotReadFromCacheCachePolicy)) {
			return null;
		}
		// 从Cache中读
		synchronized (m_cacheHash) {
			MetaData o = m_cacheHash.get(key);
			if (o != null && o.m_expirePoint > System.currentTimeMillis()) {
				return o.m_doObject;
			}
		}

		return null;
	}

	/**
	 * 把API网络返回的结果加入cache中。这里保存的是成功的API调用中，由ConnectorHelper解析出来的DO。
	 * 
	 * @param key
	 *            数据对于的唯一key，这里一般是API的URL
	 * @param doObject
	 *            保存的对象
	 * @param storagePolicy
	 *            存储策略
	 * @param expireTime
	 *            过期时间
	 */
	public void setCacheData(String key, Object doObject, int cachePolicy, int storagePolicy, int expireTime) {
		if (null == key)
			return;

		if (API_CACHE_POLICY_DoNotWriteToCacheCachePolicy == (cachePolicy & API_CACHE_POLICY_DoNotWriteToCacheCachePolicy)) {
			return;
		}
		// 存入Cache
		MetaData data = new MetaData(doObject, storagePolicy, expireTime + System.currentTimeMillis());
		synchronized (m_cacheHash) {
			m_cacheHash.put(key, data);
			m_cacheKeyList.add(key);
			if (m_cacheHash.size() > MAX_ITEM) {
				String oldkey = m_cacheKeyList.remove(0);
				m_cacheHash.remove(oldkey);
			}
		}
		return;
	}

	/**
	 * 删除某个Cache
	 * 
	 * @param key
	 *            数据对于的唯一key，这里一般是API的URL
	 * @storagePolicy Cache的存储策略
	 * @return 是否成功
	 */
	public boolean deleteCacheData(String key, int storagePolicy) {
		synchronized (m_cacheHash) {
			m_cacheKeyList.remove(key);
			m_cacheHash.remove(key);
		}
		return true;
	}

	/**
	 * 查看指定策略中是否存在数据
	 * 
	 * @param key
	 *            数据对于的唯一key，这里一般是API的URL
	 * @param policy
	 *            cache的保存策略
	 * @return true表示数据存在，否则返回false
	 */

	public boolean hasCacheData(String key, int storagePolicy) {
		synchronized (m_cacheHash) {
			return m_cacheHash.containsKey(key);
		}
	}

	/**
	 * 清除指定策略中的数据
	 * 
	 * @param policy
	 *            cache的保存策略
	 * @return true表示成功
	 */
	public boolean clearCache(int policy) {
		synchronized (m_cacheHash) {
			m_cacheHash.clear();
			m_cacheKeyList.clear();
		}
		return true;
	}
};
