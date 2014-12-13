package com.android.airhelper.filecache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileAccesser {
	
	/*
	 * 是否存在该文件
	 */
	public static boolean hasFile(String fileName){
		
		File file = new File(fileName);
		if(file.exists())
			return true;
		else 
			return false;
	}
	
	/*
	 * 读取文件
	 * 获取共享锁读取，同一个文件读取操作可以并发执行——进程安全
	 */
	public static byte[] read(String fileName){
		long time = System.currentTimeMillis();
		FileInputStream fileInput = null;
		FileChannel channel = null;
		//FileLock lock = null;
		try {
			fileInput = new FileInputStream(fileName);
			channel = fileInput.getChannel();
			//获取读取共享锁
//			try{
//				lock = channel.lock(0, channel.size(), true);
//			}catch(Exception e){
//				
//			}
			ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
			//读取文件
			channel.read(buffer);
			return buffer.array();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			//释放资源
			
//			if(lock != null)
//				try {
//					lock.release();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			
			if(fileInput != null)
				try {
					fileInput.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			if(channel != null)
				try {
					channel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		}
	}
	
	/*
	 * 读取文件
	 * 获取互斥锁写入，屏蔽任何同文件操作——进程安全
	 */
	public static boolean write(String fileName,ByteBuffer data)throws NotEnoughSpace{
		long time = System.currentTimeMillis();
		FileOutputStream fileOutput = null;
		//FileLock lock = null;
		FileChannel channel = null;
		File file = null;
		try {
			file = new File(fileName);
			if(!file.exists()){
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			fileOutput = new FileOutputStream(fileName);
			//获取写入通道
			channel = fileOutput.getChannel();
			
			//获取互斥锁
//			try{
//				lock = channel.lock();
//			}catch(Exception e){
//				
//			}
			
			//写入数据
			data.position(0);
			channel.write(data);
			channel.force(true);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			String msg = e.getMessage();
			if(null!=msg){
				if(msg.contains("ENOSPC")){
					throw new NotEnoughSpace("not enouth space in flash");
				}
			}
			/*
			 * 写入异常则删除文件
			 */
			if(file != null)
				file.delete();
			e.printStackTrace();
			return false;
		} catch(Exception e){
			e.printStackTrace();
			/*
			 * 写入异常则删除文件
			 */
			if(file != null)
				file.delete();
			return false;
		}finally{
		
			
			//释放资源
//			if(lock != null)
//				try {
//					lock.release();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			
			if(fileOutput != null)
				try {
					fileOutput.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			if(channel != null)
				try {
					channel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
		
	}
	
	/*
	 * 删除文件
	 */
	public static boolean delete(String fileName){
		File file = new File(fileName);
		return file.delete();
	}
	
	
}
