package com.android.airhelper.cache;

import java.nio.ByteBuffer;

import com.android.airhelper.filecache.FileBufferPool;
import com.android.airhelper.filecache.FileCache;
import com.android.airhelper.filecache.FileDir;

import android.app.Application;
import android.taobao.protostuff.LinkedBuffer;
import android.taobao.protostuff.ProtostuffIOUtil;
import android.taobao.protostuff.RuntimeSchema;

public class Cache {
	private static FileDir persistedCache;
	private static FileBufferPool tmpCache;
	private static String CACHENAME = "cache";
	private static int PERSISTEDCACHE_CAP = 1000;
	private static int TMP_CAP_SDCARD = 20*1024*1024;
	private static int TMP_CAP_PHONE= 5*1024*1024;
	private static int TMP_BLOCK_SIZE_PHONE = 32;
	private static int TMP_BLOCK_SIZE_SDCARD = 128;
	
	public static synchronized void init(Application context){
		//初始化永久缓存
		if(persistedCache == null){
			persistedCache = FileCache.getInsatance(context).getFileDirInstance(CACHENAME, true);
			if(persistedCache == null)
				persistedCache = FileCache.getInsatance(context).getFileDirInstance(CACHENAME, false);
			if(persistedCache != null){
				persistedCache.init(null, null);
				persistedCache.setCapacity(PERSISTEDCACHE_CAP);
			}
		}
		
		//初始化临时缓存
		if(tmpCache == null){
			tmpCache = FileCache.getInsatance(context).getFilePoolInstance(CACHENAME+".dat", true);
			if(tmpCache == null)
				tmpCache = FileCache.getInsatance(context).getFilePoolInstance(CACHENAME+".dat", false);
			if(tmpCache != null){
				if(tmpCache.isInSdcard())
					tmpCache.init(TMP_CAP_SDCARD, TMP_BLOCK_SIZE_SDCARD);
				else
					tmpCache.init(TMP_CAP_PHONE, TMP_BLOCK_SIZE_PHONE);
			}
		}
	}
	
	/*
	 * 释放缓存
	 */
	public static synchronized void destroy(Application context){
		if(persistedCache != null)
			FileCache.getInsatance(context).releaseFileDir(CACHENAME, persistedCache.isInSdcard());
		if(tmpCache != null){
			FileCache.getInsatance(context).releaseFilePool(CACHENAME, tmpCache.isInSdcard());
			tmpCache.clear();
		}
	}
	
	/*
	 * 持久层cache
	 */
	/*
	 * 字节流写入永久缓存
	 */
	public static boolean putPersistedCache(String key,byte[] data){
		if(persistedCache != null){
			ByteBuffer buffer = ByteBuffer.wrap(data);
			return persistedCache.write(key, buffer);
		}
		return false;
	}
	
	/*
	 * 对象写入永久缓存
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean putPersistedCache(String key,Object dataObject,Class dataClass){
		if(persistedCache != null){
			LinkedBuffer tmp = LinkedBuffer.allocate(256);

			//序列化
			long time = System.currentTimeMillis();
			byte[] data = ProtostuffIOUtil.toByteArray(dataObject, RuntimeSchema.getSchema(dataClass),tmp);
			ByteBuffer buffer = ByteBuffer.wrap(data);
			return persistedCache.write(key, buffer);
		}
		return false;
	}
	
	
	public static boolean putPersistedCache(String key,Object obj){
		//默认过期时间为1小时
		long defaultTimeOut = 1 * 60 * 60;
		// long defaultTimeOut = 120;//测试2秒
		return putPersistedCache(key,obj,defaultTimeOut);
	}
	
	
	public static boolean putPersistedCache(String key,Object obj,long timeOut){
		if(persistedCache != null){
			LinkedBuffer tmp = LinkedBuffer.allocate(256);
			CacheDo cache = new CacheDo();
			cache.data = obj;
			cache.expireTime = getCurrentTime()+timeOut*1000;
			long time = System.currentTimeMillis();
			byte[] data = ProtostuffIOUtil.toByteArray(cache, RuntimeSchema.getSchema(CacheDo.class),tmp);
			ByteBuffer buffer = ByteBuffer.wrap(data);
			return persistedCache.write(key, buffer);
		}
		return false;
	}
	
	/*
	 * 删除永久缓存
	 */
	public static boolean delPersistedCache(String key){
		if(persistedCache != null){
			return persistedCache.delete(key);
		}
		return false;
	}
	/*
	 * 获取永久缓存字节流
	 */
	public static byte[] getPersistedByte(String key){
		if(persistedCache != null){
			return persistedCache.read(key);
		}
		return null;
	}
	
	/*
	 * 获取永久缓存对象
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static  Object getPersistedObj(String key,Class dataClass) throws IllegalAccessException {
		if(persistedCache != null){
			Object ret = null;
			try {
				ret = dataClass.newInstance();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				throw e;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			byte[] data = persistedCache.read(key);
			
			if(data != null){
				try{
					long time = System.currentTimeMillis();
					ProtostuffIOUtil.mergeFrom(data,ret, RuntimeSchema.getSchema(dataClass));
				}catch(Exception e){
					e.printStackTrace();
					return null;
				}
				return ret;
			}else
				return null;
		}
		return null;
	}
	
	/*
	 * 获取永久缓存对象
	 */
	
	public static Object getPersistedCache(String key) throws IllegalAccessException {
		if(persistedCache != null){
			CacheDo ret = null;
			try {
				ret = CacheDo.class.newInstance();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				throw e;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte[] data = persistedCache.read(key);
			if(data != null){
				try{
					ProtostuffIOUtil.mergeFrom(data,ret, RuntimeSchema.getSchema(CacheDo.class));
					if(isCacheExpired(ret)){
						return null;
					}
				}catch(Exception e){
					e.printStackTrace();
					return null;
				}
				if (ret != null)
				    return ret.data;
			}else
				return null;
			
		}
		return null;
	}
	
	/*
	 * 清除永久缓存
	 */
	public static boolean clearPersistedCache(){
		if(persistedCache != null){
			return persistedCache.clear();
		}
		return false;
	}
	
	/*
	 * 临时cache
	 */
	/*
	 * 字节流写入临时缓存
	 */
	public static boolean putTmpCache(String key,byte[] data){
		if(tmpCache != null){
			return tmpCache.write(key, data);
		}
		return false;
	}
	/*
	 * 对象写入临时缓存
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean putTmpCache(String key,Object dataObject,Class dataClass){
		if(tmpCache != null){
			LinkedBuffer tmp = LinkedBuffer.allocate(256);

			long time = System.currentTimeMillis();
			byte[] data = ProtostuffIOUtil.toByteArray(dataObject, RuntimeSchema.getSchema(dataClass),tmp);
			return tmpCache.write(key, data);
		}
		return false;
	}
	/*
	 * 删除临时缓存
	 */
	public static boolean deleteTmpCache(String key){
		if(tmpCache != null){
			return tmpCache.erase(key);
		}
		return false;
	}
	/*
	 * 获取临时缓存字节流
	 */
	public static byte[] getTmpByte(String key){
		if(tmpCache != null){
			return tmpCache.read(key);
		}
		return null;
	}
	/*
	 * 获取临时缓存对象
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object getTmpObj(String key,Class dataClass) throws IllegalAccessException {
		if(tmpCache != null){
			Object ret = null;
			try {
				ret = dataClass.newInstance();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				throw e;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte[] data = tmpCache.read(key);
			if(data != null){
				try{
					long time = System.currentTimeMillis();
					ProtostuffIOUtil.mergeFrom(data,ret, RuntimeSchema.getSchema(dataClass));
				}catch(Exception e){
					return null;
				}
				return ret;
			}else
				return null;
			
		}
		return null;
	}
	/*
	 * 清空临时缓存
	 */
	public static boolean clearTmpCache(){
		if(tmpCache != null){
			tmpCache.clear();
			return true;
		}
		return false;
	}
	
	
	public static boolean putTmpCache(String key,Object obj){
		//默认过期时间为1小时
		long defaultTimeOut = 1*60*60;
//		long defaultTimeOut = 120;//测试2秒
		return putTmpCache(key,obj,defaultTimeOut);
	}
	
	
	public static boolean putTmpCache(String key,Object obj,long timeOut){
		if(tmpCache != null){
			LinkedBuffer tmp = LinkedBuffer.allocate(256);
			CacheDo cache = new CacheDo();
//			cache.array = dataArray;
			cache.data = obj;
			cache.expireTime = getCurrentTime()+timeOut*1000;
			byte[] data = ProtostuffIOUtil.toByteArray(cache, RuntimeSchema.getSchema(CacheDo.class),tmp);
			return tmpCache.write(key, data);
		}
		return false;
	}
	
	/*
	 * 获取临时缓存对象
	 */
	
	public static Object getTmpCache(String key) throws IllegalAccessException {
		if(tmpCache != null){
			CacheDo ret = null;
			try {
				ret = CacheDo.class.newInstance();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				throw e;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte[] data = tmpCache.read(key);
			if(data != null){
				try{
					ProtostuffIOUtil.mergeFrom(data,ret, RuntimeSchema.getSchema(CacheDo.class));
					if(isCacheExpired(ret)){
						return null;
					}
				}catch(Exception e){
					return null;
				}
				if (ret != null)
				    return ret.data;
			}else
				return null;
			
		}
		return null;
	}
	
	//TODO 最好用服务器时间，先临时用这个方法
	private static long getCurrentTime(){
		long time = System.currentTimeMillis();
		return time;
	}
	
	private static boolean isCacheExpired(CacheDo obj){
		if(null==obj){
			return true;
		}
		long curTime = getCurrentTime();
		if(curTime>=obj.expireTime){
			return true;
		}else{
			return false;
		}
	}
}
