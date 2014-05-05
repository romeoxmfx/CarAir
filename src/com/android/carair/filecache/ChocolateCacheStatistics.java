package com.android.carair.filecache;

import com.taobao.cache.CacheStatistics;

public class ChocolateCacheStatistics implements CacheStatistics{

	@Override
	public void hitProportion(boolean hited) {
		com.android.carair.filecache.CacheStatistics.cacheStatistics(hited);
	}

	@Override
	public void readPerformace(long readCost) {
		com.android.carair.filecache.CacheStatistics.cacheReadCostStatistics(readCost);
	}

	@Override
	public void writePerformace(long writeCost, long size) {
		com.android.carair.filecache.CacheStatistics.cacheWriteCostStatistics(writeCost, size);
	}

}
