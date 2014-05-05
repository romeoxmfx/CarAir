package com.android.carair.filecache;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.os.FileObserver;

/**
 * HighSpeedTmpCache 存取速度优于FileDir。
 * 只提供临时缓存，HighSpeedTmpCahce实例析构则缓存丢失
 * 非进程安全。
 *
 */
public class HighSpeedTmpCache {
	
	private boolean init;
	private String fileName;
	private boolean isInSdcard;
	private RandomAccessFile fs;
	private FileChannel fChannel;
	private HashMap<String,StoredInfo>  storedFile;
	private ReentrantReadWriteLock readWriteLock;
	private FileLock fInfoLock;
	private FileObserver mFileObserver;
	
	protected HighSpeedTmpCache(String fileName,Boolean sdcard){
		this.fileName = fileName;
		isInSdcard = sdcard;
		init = false;
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		//释放资源
		if(fInfoLock != null)
			fInfoLock.release();
		if(fs != null)
			try{
				fs.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		if(fChannel != null)
			try{
				fChannel.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		
		super.finalize();
	}
	
	/*
	 * 获取以此文件名开头的所有文件
	 */
	public String[] filtrFile(String fileNameFilter){
		if(init){
			readWriteLock.readLock().lock();
			try{
				//判断文件名是否包含过滤名
				List<String> list = new ArrayList<String>();
				Object[] fileNames = storedFile.keySet().toArray();
				for(int i=0;i < fileNames.length;i++){
					String tmp = (String) fileNames[i];
					if(tmp != null && tmp.startsWith(fileNameFilter)){
						list.add(tmp);
					}
				}
				if(list.size()>0){
					String[] ret = new String[list.size()];
					return list.toArray(ret);
				}
				
			}finally{
				readWriteLock.readLock().unlock();
			}
		}
		return null;
	}
	
	public synchronized boolean init(){
		if(!init){
			//创建目录
			File cacheFile = new File(fileName);
			cacheFile.getParentFile().mkdirs();
			//创建缓存文件
			if(!cacheFile.exists())
				try {
					cacheFile.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return false;
				}
			//创建存储文件信息库
			storedFile = new HashMap<String,StoredInfo>();
			
//			try {
//				fIn = new FileInputStream(cacheFile.getAbsolutePath());
//				fInChannel = fIn.getChannel();
//			} catch (FileNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//				return false;
//			}
			/*
			 * 打开文件，尝试进程锁
			 */
			try {
				if(fs == null)
					fs = new RandomAccessFile(cacheFile.getAbsolutePath(),"rw");
				if(fChannel == null)
					fChannel = fs.getChannel();
				fInfoLock = fChannel.tryLock();
				if(fInfoLock == null)
					return false;
				if(mFileObserver == null)
					mFileObserver = new InfoFileObserver(cacheFile.getAbsolutePath(),FileObserver.DELETE);
				mFileObserver.startWatching();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
//			try {
//				fOs = new FileOutputStream(cacheFile.getAbsolutePath());
//				fOutChannel = fOs.getChannel();
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return false;
//			}	
			readWriteLock = new ReentrantReadWriteLock();
			init = true;
		}
		return true;
	}
	
	class InfoFileObserver extends FileObserver{
		private String mPath;
		public InfoFileObserver(String path, int mask) {
			super(path, mask);
			mPath = path;
		}

		@Override
		public void onEvent(int event, String path) {
			if(event == FileObserver.DELETE){
				if(mPath != null && mPath.equals(path)){
					//释放资源
					if(fInfoLock != null)
						try {
							fInfoLock.release();
						} catch (IOException e) {
							e.printStackTrace();
						}
					init = false;
					init();
					mFileObserver.stopWatching();
				}
			}
		}
		
	}
	public boolean delete(String fileName){
		if(init){
			readWriteLock.writeLock().lock();
			try{
				//删除记录
				if(storedFile.containsKey(fileName)){//存在该文件
					storedFile.remove(fileName);
					return true;
				}
			}finally{
				readWriteLock.writeLock().unlock();
			}
		}
		return false;
	}
	/*
	 * 线程安全，无进程安全
	 */
	public byte[] read(String fileName){
		if(init){
			readWriteLock.readLock().lock();
			try{
				long time = System.nanoTime();
				if(storedFile.containsKey(fileName)){//存在该文件
					StoredInfo info = storedFile.get(fileName);
					ByteBuffer buffer = ByteBuffer.allocate((int) info.length);;
					
					try {
						//读取文件
						fChannel.read(buffer, info.pos);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
					time = (System.nanoTime() - time)/1000000;
					CacheStatistics.cacheStatistics(true);
					CacheStatistics.cacheReadCostStatistics(time);
					return buffer.array();
				}else{
					CacheStatistics.cacheStatistics(false);
				}
				return null;	
			}finally{
				readWriteLock.readLock().unlock();
			}
		}
		return null;
	}
	
	/*
	 * 线程安全，无进程安全
	 */
	public boolean append(String fileName,ByteBuffer data){
		if(init){
			readWriteLock.writeLock().lock();
			try{
				long time = System.nanoTime();
				//写入文件
				
				StoredInfo info = new StoredInfo();
				try {
					//获取文件锁
					
					data.position(0);
					info.pos = fChannel.position();
					info.length = data.capacity();
					fChannel.write(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				//写入成功，保存信息
				storedFile.put(fileName, info);
				time = (System.nanoTime()-time)/1000000;
				CacheStatistics.cacheWriteCostStatistics(time, data.capacity());
				return true;
			}finally{
				readWriteLock.writeLock().unlock();
			}
		}
		return false;
	}
	
	public boolean clear(){
		if(init){
			readWriteLock.writeLock().lock();
			try{
				long time = System.currentTimeMillis();
				try {
					//清除文件内容
					fChannel.truncate(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				//写入成功，保存信息
				storedFile.clear();
				return true;
			}finally{
				readWriteLock.writeLock().unlock();
			}
		}
		return false;
	}
	
	public boolean isInSdcard(){
		return isInSdcard;
	}
	
	class StoredInfo{
		public long pos;//文件存储位置
		public long length;//文件长度
	}
}
