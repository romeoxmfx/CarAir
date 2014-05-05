package com.android.carair.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.android.carair.threadpool2.SingleTask;
import com.android.carair.utils.Priority;


/**
 * DNSResolver
 * 提供Host到IP的解析服务。一是解析的预执行；二是在ApiConnector需要DNS时，
 * 查询本地是否Ready，本地没有则负责用DNS去解析。	 
 * 2012-12-12
 * 注：DNS可以预先取，但是不在替换IP，因为不要破坏系统的DNS机制
 */
class DNSResolver {
	
	
	static class DNSStat
	{
		long m_times = 0; //一共成功连接的次数
		long m_failtimes = 0; //一共连接失败的次数
		
		long m_dnsTime =0; //花费在成功的dns上的总时间
		 
				
		
		void reset()
		{
			m_times = 0; //一共成功连接的次数
			m_failtimes = 0; //连接失败的次数
			m_dnsTime = 0;
			
		}
		
		String report()
		{
			String res =  "" + m_times + "," + m_failtimes + ",";
			try
			{				
				if( m_times > 0)					
					res = res + m_dnsTime/(m_times);
				else 
					res = res + "0";				
				
			}
			catch(Exception e)
			{
				
			}
			reset();
			return res;

		}
	};
	
	static DNSStat mDNSStat = new DNSStat();
	
	
	private static HashMap<String, String> url_Map = new HashMap<String, String>();  //已经获得的HOST-IP对应表
	private static HashMap<String, String> url2ip_Map = new HashMap<String, String>(); //正在获取中的
		
	private static final boolean m_bDisabled = true; //把域名替换到IP的过程禁止
	/**
	 * 预先处理List中的Host到IP的解析
	 * @param hosts 主站名列表
	 */
	public static void translateHost2ip(List<String> hosts){
		
				
		int size = hosts.size();
		for(int i = 0;i<size;i++)
			translateHost2ip(hosts.get(i));
	}

	/**
	 * 预先处理Host到IP的解析
	 * @param host 单个主站
	 */

	public static void translateHost2ip(String host){
			
		if(host == null || host.length() <= 0 )
			return;
		
		synchronized(url_Map){
			if (url_Map.containsKey(host)) {
				//已存在解析成功的记录
				return;
			}						
		}	
		
		synchronized(url2ip_Map)
		{
			if(!url2ip_Map.containsKey(host)){
				//没有解析成功和正在解析的记录
				//启动解析线程
				url2ip_Map.put(host, host);												
			}
			else
			{
				return ;
			}
		}
		
		FutureTask<String> ft = new FutureTask<String>( new Host2IP(host));		
		Thread td = new Thread(ft);		
		new SingleTask(td,Priority.PRIORITY_IDLE).start();
	}
	
	//处理HOST到IP
	private static String _doResolve(String host)
	{
		String result = "";
		try 
		{	
			long t = System.nanoTime();
			//解析域名
			InetAddress ia = InetAddress.getByName(host);
			if (ia != null) {
				result = ia.getHostAddress();
				synchronized(mDNSStat)
				{
					mDNSStat.m_times += (    (System.nanoTime()-t )/1000000 );
					mDNSStat.m_dnsTime ++;
				}
			}
		}
		catch(UnknownHostException ue)			
		{
			synchronized(mDNSStat)
			{				
				mDNSStat.m_failtimes ++;
			}
			
			ue.printStackTrace();
			result = "";


		} catch (Exception e) {
			//YTS.onCaughException(e);
			
			synchronized(mDNSStat)
			{				
				mDNSStat.m_failtimes ++;
			}
			
			//YTS.onCaughException(e);
			e.printStackTrace();
			result = "";
		} finally {
			synchronized(url_Map){
				//加入域名缓存前先判断有效性				
				if (result != null && result.length() > 0)
					url_Map.put(host, result);
												
			}
			synchronized(url2ip_Map)
			{
				// 解析失败与否都移除出解析中缓存				 
				url2ip_Map.remove(host);
			}
		}
		return result;
	}
	
	

	//Callable的实现类，与FutureTask配合
	static class Host2IP implements Callable<String>
	{
		String mHost;
		Host2IP(String host)
		{
			mHost = host;
		}
		
		public String call() throws Exception {
			return _doResolve(mHost);
		}
	}
	
	
	/**
	 * 同步获得HOST对应的IP
	 * @param host 单个主站
	 * @paarm timeout 超时
	 * @return 成功时返回IP地址，否则返回“”
	 */
	public static String fetchHostIP(String host,long timeout)
	{
		if(m_bDisabled)
			return host;
		
		if(host == null || host.length() <= 0 )
			return "";
		
		String ip = "";
		
		//查看本地是否已经存在
		synchronized(url_Map)
		{
			if (url_Map.containsKey(host)) {
				return url_Map.get(host);
			}
		}	
		
	
		
		//启动线程解析
		FutureTask<String> ft = new FutureTask<String>( new Host2IP(host));		
		Thread td = new Thread(ft);		
		new SingleTask(td,Priority.PRIORITY_IDLE).start();
		try
		{
			//等待在FutureTask上
			if( timeout > 0)
				ip = ft.get(timeout,TimeUnit.MILLISECONDS);
			else
				ip = ft.get();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
		}
		return ip;									
	}
	

	/**
	 * 清除HOST对应记录
	 * @param host 主站
	 */	
	public static  void removeHost(String host)
	{
		if(host == null || host.length() <= 0 )
			return;
		synchronized(url_Map)
		{
			url_Map.remove(host);
		}		
	}
	
	/**
	 * 清除所有DNS记录
	 * 一般是程序退出是调用	 
	 */	
	public static void cleanDNSCache() {
		synchronized(url_Map){
			url_Map.clear();			
		}
		
		synchronized(url_Map){
			url2ip_Map.clear();		
		}	
	}
}
