package com.android.carair.net;

import java.io.FilterInputStream;
import java.io.InputStream;

/**这是用于统计GZIP压缩前API流量大小的类
 */
public class CounterInputStream extends FilterInputStream
{

	int m_count = 0;
	protected CounterInputStream(InputStream in) {
		super(in);
	}
	
	//重写Read，统计读的Count大小
	 public int read(byte[] buffer) throws java.io.IOException
	 {
		 int read = super.read(buffer);
		 m_count += read;
		 return read;
	 }
	 
	//重写Read，统计读的Count大小
	 public int read(byte[] buffer, int offset, int count) throws java.io.IOException
	 {
		 int read =super.read(buffer, offset, count);
		 m_count += read;
		 return read;
	 }
	
}