package com.android.airhelper.filecache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import android.os.FileObserver;
import android.os.Looper;


/**
 * 用于管理文件存储，FileDir对应一个文件目录。
 * FileDir线程安全，但非进程安全。同时只能一个进程拥有改FileDir。
 * 原始文件名：用户要求存储的文件名
 * 存储文件名：在原始文件名前加入文件信息   格式为：最近访问时间+"-"+原始文件名
 * FileInfo中存储为原始文件名
 */
public class FileDir {
	public static String FILEINFO = "tbsdk_android_finfo.dat"; //存储文件信息文件名
	public static final int READ = 1;
	public static final int WRITE = 2;
	public static final int DELETE = 3;
	public static final int CREATE = 4;
		
	private boolean sdcard;										//是否存在sdcard
	private String baseDirPath;									//缓存基目录
	private boolean isInit;										//是否初始化
	private PriorityBlockingQueue<FileInfo>  sortedStoredFile;  //排序存储列表
	private HashMap<String,FileInfo>  storedFile;		//存储列表
	private RandomAccessFile fInfoOs;
	private FileChannel fInfoChannel;
	private FileDirListener listener;
	private FileInfoCreator creator;
	private ReentrantLock lock;
	private boolean isNoSpaceClear = false;
	private int maxCapacity = 100;
	private FileLock fInfoLock;
	private long currentTime;
	private long mainThreadId;
	private FileObserver mFileObserver;
	protected FileDir(String url,Boolean sdcard){
		this.sdcard = sdcard.booleanValue();
		baseDirPath = url;
		isInit = false;
		mainThreadId = Looper.getMainLooper().getThread().getId();
	}

	public void enableNoSpaceClear(boolean enable){
		isNoSpaceClear = enable;
	}
	/*
	 * 设置目录最大容量
	 */
	public void setCapacity(int maxCapacity){
		if(!isInit)
			return;
		this.maxCapacity = maxCapacity;
		
		/*
		 * 文件数超出上限
		 */
		if(sortedStoredFile.size() > maxCapacity)
			onFileOverflow(sortedStoredFile);
	}
	
	public boolean isEmpty(){
		if(!isInit)
			return true;
		else
			return sortedStoredFile.size() == 0;
	}
	@Override
	protected void finalize() throws Throwable {
		//释放资源
		if(fInfoLock != null)
			fInfoLock.release();
		if(fInfoOs != null)
			try{
				fInfoOs.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		if(fInfoChannel != null)
			try{
				fInfoChannel.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		
		super.finalize();
	}
	
	/*
	 * 初始化
	 * comparator用于存储溢出时的排序
	 */
	public synchronized boolean init(Comparator<FileInfo> comparator,FileInfoCreator creator){
		do{//用于后续初始化失败的资源释放。
			if(!isInit){
				//创建文件信息存储通道
				File infoFile = new File(baseDirPath,FILEINFO);
				//创建目录
				new File(baseDirPath).mkdirs();
				
				if(!infoFile.exists())//创建缓存文件信息存储文件
					try {
						infoFile.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return false;
					}
				
				/*
				 * 初始化文件，尝试进程锁。
				 */
				try {
					if(fInfoOs == null)
						fInfoOs = new RandomAccessFile(infoFile.getAbsolutePath(),"rw");
					if(fInfoChannel == null)
						fInfoChannel = fInfoOs.getChannel();
					fInfoLock = fInfoChannel.tryLock();
					if(fInfoLock == null)
						return false;
					if(mFileObserver == null)
						mFileObserver = new InfoFileObserver(infoFile.getAbsolutePath(),FileObserver.DELETE);
					mFileObserver.startWatching();
				}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			
				long time = System.currentTimeMillis();
				
				//创建文件信息存储
				if(comparator != null)
					sortedStoredFile = new PriorityBlockingQueue<FileInfo>(100,comparator);
				else
					sortedStoredFile = new PriorityBlockingQueue<FileInfo>();
				storedFile = new HashMap<String,FileInfo>();
				
				//创建文件信息创建者
				if(creator == null)
					this.creator = new BaseFICreator();
				else
					this.creator = creator;
				
				//获取基目录下的所有文件
				if(!collectFiles(infoFile.getAbsolutePath()))
					break;//初始化失败，释放资源
				
				
				if(lock == null)
					lock = new ReentrantLock();
				isInit = true;		
			}
			return true;
		}while(false);
		
		//初始化失败，释放锁
		if(fInfoLock != null)
			try {
				fInfoLock.release();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		return false;
	}
	
	class InfoFileObserver extends FileObserver{
		private String mPath;
		public InfoFileObserver(String path, int mask) {
			super(path, mask);
			mPath = path;
		}

		@SuppressWarnings("unchecked")
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
					isInit = false;
					if(sortedStoredFile != null)
						init((Comparator<FileInfo>) sortedStoredFile.comparator(),creator);
					mFileObserver.stopWatching();
				}
			}
		}
		
	}
	public void setListener(FileDirListener listener){
		this.listener = listener;
	}
	/* 
	 * 是否存储在sdcard
	 */
	public boolean isInSdcard(){
		return sdcard;
	}
	
	/*
	 * 获取存储路径
	 */
	public String getDirPath(){
		return baseDirPath;
	}
	
	public void hidenMediaFile(){
		File nomedia = new File(baseDirPath,".nomedia");
		try {
			nomedia.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * 获取以此文件名开头的所有文件
	 */
	public String[] filtrFile(String fileNameFilter){
		if(isInit){
			lock.lock();
			try{
				//判断文件名是否包含过滤名
				List<String> list = new ArrayList<String>();
				Object[] fileNames = storedFile.keySet().toArray();
				for(int i=0;i < fileNames.length;i++){
					String tmp = (String) fileNames[i];
					if(tmp.length()>baseDirPath.length()+1){
						tmp = tmp.substring(baseDirPath.length()+1);
						if(tmp != null && tmp.startsWith(fileNameFilter)){
							list.add(tmp);
						}
					}
				}
				if(list.size()>0){
					String[] ret = new String[list.size()];
					return list.toArray(ret);
				}
				
			}finally{
				lock.unlock();
			}
		}
		return null;
	}
	/*
	 * 获取文件对应的文件信息
	 */
	public FileInfo getFileInfo(String fileName){
		if(isInit){
			lock.lock();
			try{
				File file = new File(baseDirPath,fileName);
				
				//是否存在该文件
				if(storedFile.containsKey(file.getAbsolutePath())){
					if(!file.exists()){//文件已丢失
						FileInfo fileInfo = storedFile.get(file.getAbsolutePath());
						storedFile.remove(file.getAbsolutePath());
						sortedStoredFile.remove(fileInfo);
						return null;
					}
					return storedFile.get(file.getAbsolutePath());
					
				}else
					return null;
			}finally{
				lock.unlock();
			}
		}
		return null;
	}
	
	/*
	 * 获取文件内容
	 * 获取完成后将修改存储文件名，以更新文件信息
	 * 线程安全
	 */
	public byte[] read(String fileName){
		if(Thread.currentThread().getId() == mainThreadId){
			//throw new RuntimeException("read file in main thread");
		}
		if(isInit){
			//对文件信息做锁操作
			//读文件在锁外
			lock.lock();
			
			FileInfo fileInfo = null;
			long time = System.nanoTime();
			try{
				File file = new File(baseDirPath,fileName);
				//是否存在该文件
				if(storedFile.containsKey(file.getAbsolutePath())){
					//获取文件信息
					fileInfo = storedFile.get(file.getAbsolutePath());
					//修改文件信息及文件存储名
					sortedStoredFile.remove(fileInfo);
					
					fileInfo = creator.onUpdateFileInfo(fileInfo.getFileName(),fileInfo, READ,currentTime++);
					refreshTimeTicker(currentTime);
					storedFile.put(fileInfo.getFileName(), fileInfo);
					sortedStoredFile.put(fileInfo);
					refreshFileInfo(fileInfo);
				}
			}finally{
				lock.unlock();
			}
			//读取文件
			byte[] ret = null;
			if(fileInfo != null){
				ret = FileAccesser.read(fileInfo.getFileName());
				time = (System.nanoTime()-time)/1000000;
				CacheStatistics.cacheStatistics(true);
				CacheStatistics.cacheReadCostStatistics(time);
				
			}else{
				CacheStatistics.cacheStatistics(false);
			}
			
			
			return ret;
		}
		return null;
	}
	
	
	
	/*
	 * 写入文件内容
	 * 线程安全
	 */
	public boolean write(String fileName,ByteBuffer data){
		if(Thread.currentThread().getId() == mainThreadId){
			//throw new RuntimeException("write file in main thread");
		}
		if(isInit){
			File file = new File(baseDirPath,fileName);
			//写入文件
			long time = System.nanoTime();
			boolean ret = false;
			try {
				ret = FileAccesser.write(file.getAbsolutePath(), data);
			} catch (NotEnoughSpace e) {
				//空间满则清除已有文件
				e.printStackTrace();
				if(isNoSpaceClear){
					clear();
					try {
						ret = FileAccesser.write(file.getAbsolutePath(), data);
					} catch (NotEnoughSpace e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			if(ret){//写入成功
				lock.lock();
				//创建文件信息
				FileInfo fileInfo = null;
				try{
					//是否存在该文件
					if(storedFile.containsKey(file.getAbsolutePath())){
						//获取文件信息
						fileInfo = storedFile.get(file.getAbsolutePath());
						
						//修改文件信息
						sortedStoredFile.remove(fileInfo);
						fileInfo = creator.onUpdateFileInfo(fileInfo.getFileName(),fileInfo, WRITE,currentTime++);
						refreshTimeTicker(currentTime);
						storedFile.put(fileInfo.getFileName(), fileInfo);
						sortedStoredFile.put(fileInfo);
						
						refreshFileInfo(fileInfo);
						fileInfo = null;
						
						return true;
						
						
					}else{
						fileInfo = creator.onUpdateFileInfo(file.getAbsolutePath(),null, CREATE,currentTime++);
						refreshTimeTicker(currentTime);
						try {
							//检查channel是否关闭，关闭则重新打开  modify by bokui 2013-5-9
							if(!fInfoChannel.isOpen())
								fInfoChannel = fInfoOs.getChannel();
							fileInfo.setFileInfoPos(fInfoChannel.size());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						//fileInfo.setFileName(file.getAbsolutePath());
						storedFile.put(fileInfo.getFileName(), fileInfo);
						if(fileInfo != null)
							sortedStoredFile.put(fileInfo);	
						refreshFileInfo(fileInfo);
						return true;
					}
							
				
				}finally{
					lock.unlock();
					time = (System.nanoTime()-time)/1000000;
					CacheStatistics.cacheWriteCostStatistics(time, data.capacity());
					/*
					 * 文件数超出上限
					 */
					if(sortedStoredFile.size() >= maxCapacity)
						onFileOverflow(sortedStoredFile);
						
				}
			}
			
				
		}
		return false;
	}
	/*
	 *  删除文件
	 *  线程安全
	 */
	public boolean delete(String fileName){
		if(Thread.currentThread().getId() == mainThreadId){
			//throw new RuntimeException("delete file in main thread");
		}
		if(isInit){
			//readWriteLock.writeLock().lock();
			//try{
			File file = new File(this.baseDirPath,fileName);
			return deleteFile(file.getAbsolutePath());
//			}finally{
//				readWriteLock.writeLock().unlock();
//			}
			
		}
		return false;
	}
	
	private boolean deleteFile(String fileName){
		long time = System.currentTimeMillis();
		File file = new File(fileName);
		//删除文件
		boolean ret = FileAccesser.delete(fileName);
		if(ret){
			//删除空目录   直到基目录
			File parent = file.getParentFile();
			if(!parent.getAbsolutePath().equals(baseDirPath))
				while(parent.delete()){
					parent = parent.getParentFile();
					if(parent.getAbsolutePath().equals(baseDirPath))
						break;
				}
		}
		//修改信息
		lock.lock();
		try{
			//是否存在该文件
			if(storedFile.containsKey(fileName)){
					
				//删除文件
				if(ret || !file.exists()){
	
					//删除文件信息
					FileInfo fileInfo = storedFile.get(fileName);
					storedFile.remove(file.getAbsolutePath());
					sortedStoredFile.remove(fileInfo);
					fileInfo = creator.onUpdateFileInfo(fileInfo.getFileName(),fileInfo, DELETE,0);
					refreshFileInfo(fileInfo);
					
					
					return true;
				}
				
			}
		}finally{
			lock.unlock();
		}
		return ret;
	}
	
	/*
	 * 清空缓存
	 */
	public boolean clear(){
		if(Thread.currentThread().getId() == mainThreadId){
			//throw new RuntimeException("clear file in main thread");
		}
		if(isInit){
			boolean ret = true;
			//获取要删除的文件列表
			lock.lock();
			FileInfo[] deleteFiles;
			try{
				deleteFiles = sortedStoredFile.toArray(new FileInfo[sortedStoredFile.size()]);
			}finally{
				lock.unlock();
			}
			if(deleteFiles != null)
				//循环删除文件
				for(int i = 0;i < deleteFiles.length;i++)
					ret &= deleteFile(deleteFiles[i].getFileName());

			return ret;
		}
		return false;
	}
	
	/*
	 * 存储超出上限
	 *
	 */
	private void onFileOverflow(PriorityBlockingQueue<FileInfo> sortedStoredFile){
		ArrayList<FileInfo> buffer = new ArrayList<FileInfo>();
		if(listener == null){//未设置监听
			lock.lock();
			try{
				//获取该删除的文件列表
				//在锁内完成
				int size = sortedStoredFile.size();
				size -= maxCapacity-1;
				while(size > 0){//拿出该删除的文件
					FileInfo info = sortedStoredFile.poll();
					if(info != null)
						buffer.add(info);
					size--;
				}
				sortedStoredFile.addAll(buffer);
			}finally{
				lock.unlock();
			}
			//删除多余的文件
			FileInfo[] deleteFiles = buffer.toArray(new FileInfo[buffer.size()]);
			for(int i=0;i<deleteFiles.length;i++){//循环删除多余文件
				//FileInfo tmp = buffer.get();
				deleteFile(deleteFiles[i].getFileName());
			}
							
		}else{
			lock.lock();
			try{
				//获取该删除的文件列表
				//在锁内完成
				int size = sortedStoredFile.size();
				size -= maxCapacity-1;
				while(size > 0){//拿出该删除的文件
					FileInfo info = sortedStoredFile.poll();
					if(info != null)
						buffer.add(info);
					size--;
				}
				sortedStoredFile.addAll(buffer);
			}finally{
				lock.unlock();
			}
			//删除多余的文件
			FileInfo[] deleteFiles = buffer.toArray(new FileInfo[buffer.size()]);
			for(int i=0;i<deleteFiles.length;i++){//循环删除多余文件
				//FileInfo tmp = buffer.get();
				if(!listener.onFileOverflow(deleteFiles[i].getFileName())){//未处理
					deleteFile(deleteFiles[i].getFileName());
				}else{
					lock.lock();
					try{
						storedFile.remove(deleteFiles[i].getFileName());
						sortedStoredFile.remove(deleteFiles[i]);
						deleteFiles[i] = creator.onUpdateFileInfo(deleteFiles[i].getFileName(),deleteFiles[i], DELETE,0);
						refreshFileInfo(deleteFiles[i]);
					}finally{
						lock.unlock();
					}
				}
			}
		}
	}
	
	private void refreshTimeTicker(long currentTime){
		//写入数据
		ByteBuffer buffer;	
		buffer = ByteBuffer.wrap(Long.toString(currentTime).getBytes());
		try {
			//lock = fInfoChannel.lock();
			buffer.position(0);
			//检查channel是否关闭，关闭则重新打开  modify by bokui 2013-5-9
			if(!fInfoChannel.isOpen())
				fInfoChannel = fInfoOs.getChannel();
			fInfoChannel.write(buffer,0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * 文件信息写入永久存储
	 */
	private void refreshFileInfo(FileInfo fileInfo){
		long time = System.currentTimeMillis();
		//写入数据
		ByteBuffer buffer;
		
		//文件信息后加入分割付'\n'
		byte[] infoByte = fileInfo.composeFileInfoStr();
		if(infoByte == null)
			return;
		buffer = ByteBuffer.allocate(infoByte.length+1);
		
		buffer.put(infoByte);
		buffer.put((byte) 0x0a);
		
		//FileLock lock = null;
		//更新文件信息
		try {
			//lock = fInfoChannel.lock();
			buffer.position(0);
			//检查channel是否关闭，关闭则重新打开  modify by bokui 2013-5-9
			if(!fInfoChannel.isOpen())
				fInfoChannel = fInfoOs.getChannel();
			fInfoChannel.write(buffer,fileInfo.getFileInfoPos());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		} finally{
//			if(lock != null)
//				try {
//					lock.release();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		}
	}
	
	/*
	 * 获取目录内文件列表
	 * 返回目录内文件夹列表
	 */
	private boolean collectFiles(String infoPath){
		//读取文件信息
		long time = System.currentTimeMillis();
		ByteBuffer buffer = null;
		byte[] infoByte = null;
		try {
			//检查channel是否关闭，关闭则重新打开  modify by bokui 2013-5-9
			if(!fInfoChannel.isOpen())
				fInfoChannel = fInfoOs.getChannel();
			buffer = ByteBuffer.allocate((int) fInfoChannel.size());
			fInfoChannel.read(buffer);
			infoByte = buffer.array();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//读取文件
		
		//byte[] infoByte = FileAccesser.read(infoPath);
		boolean reWrite = false;
		time = System.currentTimeMillis();
		if(infoByte != null){
			int offset = 0;
			int newLineMark = 0;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			//起先使用System.currentTime获取时间，由于系统原因，出现了时间不准确问题，所以改用自己记录时间。
			//存在时间标识,时间标识采用string存储，便于查阅及调试
			//时间采用13位计数，为了适配以前的fileinfo
			if(infoByte.length > 13 && infoByte[13] == 0x0a){
				String currentTimeStr = new String(infoByte,0,13);
				try{
					currentTime = Long.parseLong(currentTimeStr);
				}catch(Exception e){
					currentTime = 2000000000000l;
					e.printStackTrace();
				}
				bos.write(infoByte,0,14);
				offset = 14;
				newLineMark = 14;
			}else{
				//由于目前有一部分采用了System.currentTime，所以讲初始时间设为2000000000000
				currentTime = 2000000000000l;
				try {
					bos.write("2000000000000".getBytes());
					bos.write(0x0a);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(infoByte.length == 0){
					reWrite = true;
				}
					
			}
			int pathLength = creator.getFileInfoMinLength();
			try {
				pathLength += baseDirPath.getBytes("utf-8").length;
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			offset = pathLength;
			for(;offset<infoByte.length;offset++){
				if(infoByte[offset] == 0x0a){//切割文件信息
					
					//解析文件信息
					
					//String infoStr = new String(infoByte,newLineMark,offset-newLineMark,"UTF-8");
					FileInfo fileInfo = creator.onParseFileInfo(infoByte,newLineMark,offset-newLineMark);
					
					if(fileInfo != null){//解析成功
						long fileInfoTime = fileInfo.getFileInfoTime(currentTime);		
						if(fileInfoTime > currentTime){
							currentTime = fileInfoTime;
						}
						//当异常出现时候，storeFile里面可能会有多份同一个文件的信息，因此需要忽略之后的文件信息
						if(!storedFile.containsKey(fileInfo.getFileName())){
							//保存文件信息保存的偏移量
							fileInfo.setFileInfoPos(bos.size());
							sortedStoredFile.add(fileInfo);
							storedFile.put(fileInfo.getFileName(), fileInfo);
							//组装数据，文件全部解析完毕后，再一次性更新数据文件，剔除之前错误，删除，无用的数据
							bos.write(infoByte, newLineMark, offset-newLineMark+1);
						}else{
							//当异常出现时候，storeFile里面可能会有多份同一个文件的信息，因此需要忽略之后的文件信息,重新复写
							reWrite = true;
						}
						
					}else{
						//有删除的文件  需要覆写
						reWrite = true;
					}
					
					newLineMark = offset+1;	
					offset += pathLength;
					
				}
			}
			time = System.currentTimeMillis();
			//File infoFile = new File(baseDirPath,FILEINFO);
			
			
			
			if(reWrite){
				//覆盖被删除的文件信息
				//FileLock lock = null;
				try {
//					try{
//						lock = fInfoChannel.lock();
//					}catch(Exception e){
//						
//					}
					fInfoChannel.truncate(0);
					fInfoChannel.position(0);
					buffer = ByteBuffer.wrap(bos.toByteArray());
					buffer.position(0);
					fInfoChannel.write(buffer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				}finally{
//					if(lock != null)
//						try {
//							lock.release();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//				}
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return true;
		}else
			return false;
			
	}
	
	
	
	
}
