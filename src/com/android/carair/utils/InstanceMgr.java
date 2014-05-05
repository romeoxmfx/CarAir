package com.android.carair.utils;

import java.util.HashMap;

/**
 * 抽象类，单例管理器，在同一时间，保证实例为单例
 */
public abstract class InstanceMgr {
	protected HashMap<String,Object[]> InstPool;
	//private PageFactory tbFactory;
	
	public InstanceMgr(){
		//this.database = database;
		InstPool = new HashMap<String,Object[]>();

	}
	
	//获取缓存内Tab表
	private Object mapCache(String url){
		Object[] objects = InstPool.get(url);
		
		if(objects == null)
			return null;

		return objects[0];
		
	}
	
	//添加引用计数
	private void addRef(String url){
		Object[] objects = InstPool.get(url);
		
		if(objects == null)
			return;
		
		objects[1] = Integer.valueOf(((Integer)objects[1]).intValue()+1);
		
	}
	
	//递减引用计数
	protected int decreaseRef(String url){
		Object[] objects = InstPool.get(url);
		
		if(objects == null)
			return -1;
		
		int ref = ((Integer)objects[1]).intValue();
		if(ref == 0)
			return 0;
		else{
			objects[1] = Integer.valueOf(ref-1);
			return ref-1;
		}
	}
	
	/**
	 * 释放实例
	 * @param url	释放实例的标识
	 */
	public synchronized boolean release(String url){
		int result = decreaseRef(url);
		
		if(result == 0){
			InstPool.remove(url);
			return true;
		}else{
			return false;
		}
			
	}
	
	/**
	 * 清空所有实例
	 */
	public synchronized void releaseAll(){
		InstPool.clear();
	}
	
	/**
	 * 获取实例，一般都通过createInstance获取
	 * @param url	获取实例的标识
	 * @return		返回实例对象，如果当前没有该实例则返回NULL
	 */
	public synchronized Object getInstance(String url){
		return mapCache(url);
	}
	
	/**
	 * 创建实例，如果实例已存在则直接返回
	 * @param url		获取实例的标识
	 * @param creater	该实例的创建者
	 * @return
	 */
	public synchronized Object createInstance(String url,Object creater){
		Object ret = mapCache(url);
		if(ret != null){
			addRef(url);
			return ret;
		}

		ret = instance(url,creater);

		if(ret != null)
		{
			Object[] objects = new Object[2];
			objects[0] = ret;
			objects[1] = Integer.valueOf(1);
			InstPool.put(url, objects);
		}
		return ret;
	}
	/**
	 * 创建某个实例，在新创建实例时被调用。在调用createInstance时被动调用
	 * 
	 * @param url		获取实例的标识
	 * @param creator	该实例的创建者
	 * @return
	 */
	protected abstract Object instance(String url,Object creator);

}
