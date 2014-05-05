package com.android.carair.utils;

public interface Priority {
	public static int PRIORITY_REALTIME = 0;
	public static int PRIORITY_HIGH = PRIORITY_REALTIME+1;
	public static int PRIORITY_NORM = PRIORITY_HIGH+1;
	public static int PRIORITY_IDLE = PRIORITY_NORM+1;
	
	public int getPriority();
}
