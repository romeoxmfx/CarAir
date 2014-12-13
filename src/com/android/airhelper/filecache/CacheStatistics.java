package com.android.airhelper.filecache;

import java.io.File;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Environment;

/**
 * 用于cache性能统计，统计采用隔天上传的机制。
 * 统计数据有：缓存命中率，缓存读取性能，缓存写入性能，缓存大小
 * @author bokui
 *
 */
public class CacheStatistics {
	
	//缓存
	private static SharedPreferences statisticsCache;
	private static Editor sEditor;
	
	//统计时间
	private static int sStatisticsDate;
	
	//命中率统计参数
	private static int sTotalReadTimes;
	private static int sHittedTimes;
	
	//读性能统计参数
	private static long sReadTimeCostSum;
	
	//写性能统计参数
	private static int sWriteCacheTimes;
	private static long sWriteByteSum;
	private static long sWriteTimeCostSum;
	
	
	private static Statistics sStatistics = null;
	private static final String CACHESTATISTICS_TOTALTIMES = "TOTAL_TIMES";
	private static final String CACHESTATISTICS_WRITETIMES = "WRITE_TIMES";
	private static final String CACHESTATISTICS_STATDATE = "STAT_DATE";
	
	public static final String CACHESTATISTICS_HITEDRATE = "CACHE_HI";
	public static final String CACHESTATISTICS_READCOST = "READ_COST";
	public static final String CACHESTATISTICS_WRITECOST = "WRITE_COST";
	public static final String CACHESTATISTICS_CACHESIZE = "CACHE_SIZE";
	
	public static boolean IOTMODE = false;
	
	
	/**
	 * 初始化cache统计器
	 * @param statistics	统计器
	 * @param context		Context实例
	 */
	public static synchronized void init(Statistics statistics,Context context){
		//已初始化则直接返回
		if(sStatistics != null)
			return;
		
		statisticsCache = context.getSharedPreferences("cache_statistics", Context.MODE_PRIVATE);
		if(statisticsCache == null)
			return;
		
		sStatistics = statistics;
		sEditor = statisticsCache.edit();
		//命中率统计参数
		sTotalReadTimes = statisticsCache.getInt(CACHESTATISTICS_TOTALTIMES, 0);
		sHittedTimes = statisticsCache.getInt(CACHESTATISTICS_HITEDRATE, 0);
		//读性能统计参数
		sReadTimeCostSum = statisticsCache.getLong(CACHESTATISTICS_READCOST, 0);
		//写性能统计参数
		sWriteCacheTimes = statisticsCache.getInt(CACHESTATISTICS_WRITETIMES, 0);
		sWriteByteSum = statisticsCache.getLong(CACHESTATISTICS_CACHESIZE, 0);
		sWriteTimeCostSum = statisticsCache.getLong(CACHESTATISTICS_WRITECOST, 0);
		//统计的时间戳
		sStatisticsDate = statisticsCache.getInt(CACHESTATISTICS_STATDATE, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
		
		StringBuilder sb = (new StringBuilder()).append(
				Environment.getExternalStorageDirectory().toString())
				.append(File.separator).append("MTL_IOT");
    	
		File file = new File(sb.toString());
    	if(file.exists())
    		IOTMODE = true;
	}
	
	/**
	 * 命中率统计
	 * @param hited 读取缓存是否命中
	 */
	@SuppressLint("NewApi")
	protected static synchronized void cacheStatistics(boolean hited){
		if(sStatistics == null)
			return;
		
		//日期已变，提交数据
		if(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != sStatisticsDate){
			statistic();
		}
		
		//统计数据
		sTotalReadTimes++;
		if(hited){
			//TaoLog.Logd("cache_statistic", "cache hited");
			sHittedTimes++;
		}
		
		//到达一定次数缓存一下数据
		if(sTotalReadTimes % 10 == 0){
			//Editor editor = statisticsCache.edit();
			sEditor.putInt(CACHESTATISTICS_TOTALTIMES, sTotalReadTimes);
			sEditor.putInt(CACHESTATISTICS_HITEDRATE, sHittedTimes);
			if(Build.VERSION.SDK_INT >= 9)
				sEditor.apply();
			else
				sEditor.commit();
		}
		
	}
	
	/**
	 * 缓存读取性能统计
	 * @param readCost	读取时间消耗  单位ms
	 */
	@SuppressLint("NewApi")
	protected static synchronized void cacheReadCostStatistics(long readCost){
		if(IOTMODE)
		if(sStatistics == null)
			return;
		
		//日期已变，提交数据
		if(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != sStatisticsDate){
			statistic();
		}
		//统计数据
		sReadTimeCostSum += readCost;
		
		//到达一定次数缓存一下数据
		if(sHittedTimes % 10 == 0){
			//Editor editor = statisticsCache.edit();
			sEditor.putLong(CACHESTATISTICS_READCOST, sReadTimeCostSum);
			if(Build.VERSION.SDK_INT >= 9)
				sEditor.apply();
			else
				sEditor.commit();
		}
	}
	
	/**
	 * 写缓存统计
	 * @param writeCost	写缓存消耗 单位ms
	 * @param size		写入大小 单位字节
	 */
	@SuppressLint("NewApi")
	protected static synchronized void cacheWriteCostStatistics(long writeCost,long size){
		if(IOTMODE)
		if(sStatistics == null)
			return;
		//日期已变，提交数据
		if(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != sStatisticsDate){
			statistic();
		}
		//统计数据
		sWriteCacheTimes ++;
		sWriteByteSum += size;
		sWriteTimeCostSum += writeCost;
		//到达一定次数缓存一下数据
		if(sWriteCacheTimes % 10 == 0){
			//Editor editor = statisticsCache.edit();
			sEditor.putInt(CACHESTATISTICS_WRITETIMES, sWriteCacheTimes);
			sEditor.putLong(CACHESTATISTICS_WRITECOST, sWriteTimeCostSum);
			sEditor.putLong(CACHESTATISTICS_CACHESIZE, sWriteByteSum);
			if(Build.VERSION.SDK_INT >= 9)
				sEditor.apply();
			else
				sEditor.commit();
		}
	}
	
	/**
	 * 提交统计接口
	 */
	@SuppressLint("NewApi")
	private static void statistic(){
		//提交统计
		CacheStatDo csd = new CacheStatDo();
		
		csd.mHittedTimes = sHittedTimes;
		csd.mTotalTimes = sTotalReadTimes;
		csd.mReadTimeCostSum = sReadTimeCostSum;
		csd.mWriteByteSum = sWriteByteSum;
		csd.mWriteCacheTimes = sWriteCacheTimes;
		csd.mWriteTimeCostSum = sWriteTimeCostSum;
		
		if(sStatistics != null)
			sStatistics.statistic(csd);
		
		//统计清0
		sHittedTimes = 0;
		sTotalReadTimes = 0;
		sReadTimeCostSum = 0;
		sWriteByteSum = 0;
		sWriteCacheTimes = 0;
		sWriteTimeCostSum = 0;
		
		//重新设置统计时间
		sStatisticsDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		
		if(statisticsCache != null){
			//Editor editor = statisticsCache.edit();
			sEditor.putInt(CACHESTATISTICS_TOTALTIMES, 0);
			sEditor.putInt(CACHESTATISTICS_HITEDRATE, 0);
			sEditor.putLong(CACHESTATISTICS_READCOST, 0);
			sEditor.putInt(CACHESTATISTICS_WRITETIMES, 0);
			sEditor.putLong(CACHESTATISTICS_WRITECOST, 0);
			sEditor.putLong(CACHESTATISTICS_CACHESIZE, 0);
			
			sEditor.putInt(CACHESTATISTICS_STATDATE, sStatisticsDate);
			if(Build.VERSION.SDK_INT >= 9)
				sEditor.apply();
			else
				sEditor.commit();
		}
		
		
		
	}
	/**
	 * 统计器，用于数据统计，并上传服务器
	 * @author bokui
	 *
	 */
	public interface Statistics{
		/**
		 * 统计接口
		 * @param attrKey		统计的属性
		 * @param attrValue		统计属性值
		 */
		public void statistic(CacheStatDo data);
	}
	
	/**
	 * cache统计的数据
	 * @author bokui
	 *
	 */
	public static class CacheStatDo{
		/**
		 * 尝试读取缓存的总次数
		 */
		public int mTotalTimes;
		/**
		 * 缓存命中的次数
		 */
		public int mHittedTimes;
		/**
		 * 写缓存的总次数
		 */
		public int mWriteCacheTimes;
		/**
		 * 写缓存的总字节数
		 */
		public long mWriteByteSum;
		/**
		 * 写缓存的总耗时
		 */
		public long mWriteTimeCostSum;
		/**
		 * 读缓存的总耗时
		 */
		public long mReadTimeCostSum;
	}
}
