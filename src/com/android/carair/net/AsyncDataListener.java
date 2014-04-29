package com.android.carair.net;

	//--异步调用
	//异步调用完成时的回调
	/**
	 * AsyncDataListener 用于异步调用的回调接口的定义
	 */	
	public interface AsyncDataListener{
		
		/**
		 * onProgress 通知POST数据的进度
		 * @param desc 描述
		 * @param size 当前大小
		 * @param total 总大小
		 * 
		 */	
		public void onProgress( String desc, int size, int total);
		
		/**
		 * onDataArrive API调用完成
		 * @param res 调用的结果
		 */
		public void onDataArrive(ApiResult res);
	}