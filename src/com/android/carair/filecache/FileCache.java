package com.android.carair.filecache;

import java.io.File;

import com.android.carair.common.SDKConfig;
import com.android.carair.utils.InstanceMgr;
import com.android.carair.utils.MemoryMgr;

import android.app.Application;
import android.os.Environment;

public class FileCache extends InstanceMgr{

	private String sdBaseUrl = null;
	private String phoneBaseUrl = null;
	private static FileCache fileCache;
	private String TMPCACHE = "tmpcache";
	private String FILEDIR = "filedir";
	private String FILEPOOL = "filepool";
	
	protected FileCache(Application context){	
		
	}
	
	private void intPath(Application context){
		if(MemoryMgr.checkSDCard()){
			
			if(SDKConfig.getInstance().getGlobalSaveFileRootDir() != null && !"".equals(SDKConfig.getInstance().getGlobalSaveFileRootDir())) {
				sdBaseUrl = Environment.getExternalStorageDirectory().toString()+"/"+SDKConfig.getInstance().getGlobalSaveFileRootDir()+"/"+context.getPackageName();
			} else {
				sdBaseUrl = Environment.getExternalStorageDirectory().toString()+"/"+context.getPackageName();
			}
			
		}
		if(context.getFilesDir() != null)
			phoneBaseUrl = context.getFilesDir().getAbsolutePath();
	}
	/**
	 * 获取文件缓存实例，用于获取不同的文件缓存
	 * @param context	Application Context
	 * @return	FileCache实例
	 */
	public synchronized static FileCache getInsatance(Application context){
		if(fileCache == null)
			fileCache = new FileCache(context);
		fileCache.intPath(context);
		return fileCache;
	}
	
	/**
	 * 销毁文件缓存实例，释放所有文件缓存
	 */
	public synchronized static void destroy(){
		if(fileCache != null){
			fileCache.releaseAll();
			fileCache = null;
		}
	}
	
	/**
	 * 获取文件池缓存实例，文件池缓存实例采用但文件方式缓存，类似内存池形式分配缓存空间。
	 * @param url		缓存标识，文件池缓存将用改url作为存储路径
	 * @param sdcard	是否存储在sdcard
	 * @return
	 */
	public FileBufferPool getFilePoolInstance(String url,boolean sdcard){
		if(sdcard){//存储介质：sdcard
			if(sdBaseUrl != null){//sdcard存在
				//构造完整路径
				url = new File(sdBaseUrl,url).getAbsolutePath();
				//添加filedir标识
				url = FILEPOOL+url;
				return (FileBufferPool) super.createInstance(url, Boolean.valueOf(sdcard));
			}else{
				return null;
			}
		}else{
			if(phoneBaseUrl == null)
				return null;
			//构造完整路径
			url = new File(phoneBaseUrl,url).getAbsolutePath();
			//添加filedir标识
			url = FILEPOOL+url;
			return (FileBufferPool) super.createInstance(url, Boolean.valueOf(sdcard));
		}
	}
	
	/**
	 * 释放文件池缓存
	 * @param url	缓存标识
	 * @param sdcard	是否存储在sdcard
	 */
	public void releaseFilePool(String url,boolean sdcard){
		release(url,sdcard,FILEPOOL);
	}
	
	/**
	 * 获取FileDir实例
	 * @param url	缓存标识
	 * @param sdcard	是否存储在sdcard
	 */
	public FileDir getFileDirInstance(String url,boolean sdcard){
		if(sdcard){//存储介质：sdcard
			if(sdBaseUrl != null){//sdcard存在
				//构造完整路径
				url = new File(sdBaseUrl,url).getAbsolutePath();
				//添加filedir标识
				url = FILEDIR+url;
				return (FileDir) super.createInstance(url, Boolean.valueOf(sdcard));
			}else{
				return null;
			}
		}else{
			if(phoneBaseUrl == null)
				return null;
			//构造完整路径
			url = new File(phoneBaseUrl,url).getAbsolutePath();
			//添加filedir标识
			url = FILEDIR+url;
			return (FileDir) super.createInstance(url, Boolean.valueOf(sdcard));
		}
		
	}
	/**
	 * 释放FileDir实例
	 * @param url	缓存标识
	 * @param sdcard	是否存储在sdcard
	 */
	public void releaseFileDir(String url,boolean sdcard){
		release(url,sdcard,FILEDIR);
	}
	
	
	/**
	 * 获取HighSpeedTmpCache实例
	 * @param url	缓存标识
	 * @param sdcard	是否存储在sdcard
	 */
	public HighSpeedTmpCache getTmpCacheInstance(String url,boolean sdcard){

		if(sdcard){//存储介质：sdcard
			if(sdBaseUrl != null){//sdcard存在
				//构造完整路径
				url = new File(sdBaseUrl,url).getAbsolutePath();
				//添加HighSpeedTmpCache标识
				url = TMPCACHE+url;
				return (HighSpeedTmpCache) super.createInstance(url, Boolean.valueOf(sdcard));
			}else{
				return null;
			}
		}else{
			if(phoneBaseUrl == null)
				return null;
			url = new File(phoneBaseUrl,url).getAbsolutePath();
			url = TMPCACHE+url;
			return (HighSpeedTmpCache) super.createInstance(url, Boolean.valueOf(sdcard));
		}
	}
	
	/**
	 * 释放HighSpeedTmpCache实例
	 * @param url	缓存标识
	 * @param sdcard	是否存储在sdcard
	 */
	public void releaseTmpCache(String url,boolean sdcard){
		release(url,sdcard,TMPCACHE);
	}
	
	private void release(String fileName,boolean sdcard,String type){

		if(sdcard){//存储介质：sdcard
			if(sdBaseUrl != null){//sdcard存在
				//构造完整路径
				fileName = new File(sdBaseUrl,fileName).getAbsolutePath();
				//添加HighSpeedTmpCache标识
				fileName = type+fileName;
				release(fileName);
				
			}
		}else{
			fileName = new File(phoneBaseUrl,fileName).getAbsolutePath();
			fileName = type+fileName;
			release(fileName);
		}
	}
	@Override
	protected Object instance(String url, Object creator) {
		// TODO Auto-generated method stub
		if(url.indexOf(TMPCACHE) == 0){
			return new HighSpeedTmpCache(new String(url.substring(TMPCACHE.length())),(Boolean) creator);
		}else if(url.indexOf(FILEDIR) == 0)
			return new FileDir(new String(url.substring(FILEDIR.length())),(Boolean) creator);
		else
			return new FileBufferPool(new String(url.substring(FILEPOOL.length())),(Boolean) creator);
	}

}
