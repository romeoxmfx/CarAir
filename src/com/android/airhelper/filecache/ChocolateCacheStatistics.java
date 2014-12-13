package com.android.airhelper.filecache;

import com.taobao.cache.CacheStatistics;

public class ChocolateCacheStatistics implements CacheStatistics{

	@Override
	public void hitProportion(boolean hited) {
		com.android.airhelper.filecache.CacheStatistics.cacheStatistics(hited);
	}

	@Override
	public void readPerformace(long readCost) {
		com.android.airhelper.filecache.CacheStatistics.cacheReadCostStatistics(readCost);
	}

	@Override
	public void writePerformace(long writeCost, long size) {
		com.android.airhelper.filecache.CacheStatistics.cacheWriteCostStatistics(writeCost, size);
	}

}
