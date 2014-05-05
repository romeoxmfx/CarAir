package com.android.carair.utils;

import java.util.Random;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


public class SafeHandler extends Handler{
	

	static boolean isDebugMode = false;
	static Random random = new Random(System.currentTimeMillis()); 
	static int uploadPercent = 0;
	static public void setDebugMode(boolean mode){
		isDebugMode = mode;
	}
	
	static public void setExceptionUpLoadPercent(int percent){
		uploadPercent = percent;
	}

	//是否销毁
	private boolean isAlive = true;
	
	@Override
	public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
		// TODO Auto-generated method stub
		if(!isAlive)
			return false;
		return super.sendMessageAtTime(msg, uptimeMillis);
	}
	
	public SafeHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SafeHandler(Callback callback) {
		super(callback);
		// TODO Auto-generated constructor stub
	}

	public SafeHandler(Looper looper, Callback callback) {
		super(looper, callback);
		// TODO Auto-generated constructor stub
	}

	public SafeHandler(Looper looper) {
		super(looper);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void dispatchMessage(Message msg) {
		// TODO Auto-generated method stub
		//已销毁  则不处理该消息
		if(!isAlive)
			return;
		
		if(isDebugMode){
			super.dispatchMessage(msg);
		}else{
			try{//捕获异常
				super.dispatchMessage(msg);
			}catch(Exception e){
				
				e.printStackTrace();
				int ran = random.nextInt(100);
				Log.w("System.err", "random:"+ran + " threashold:"+uploadPercent);
			}
		}

		clearMsg(msg);
	}

	public void clearMsg(Message msg){
		msg.what = 0;
		msg.arg1 = 0;
		msg.arg2 = 0;
		msg.obj = null;
		msg.replyTo = null;
		msg.setTarget(null);
	}
	//销毁该handler
	public void destroy(){
		isAlive = false;
	}

}
