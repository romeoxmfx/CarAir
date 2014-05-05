package com.android.carair.utils;

import android.app.Application;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;


/* We change the Resource.getInteger() in our special rom
 *  @param 0 is dump resource to /mnt/sdcard/resource.txt
 *  @param -1 is get the memory size of resource(current is bitmap)
 */


public class Dump {
	
	public static void startMonitor(Application context){
		if(null==context){
			return ;
		}
		
		Resources rc = context.getResources();
		if(rc==null){
			return ;
		}
		
		try{
			rc.getInteger(0);//the api of device is special (compile by us)
			return;
		}catch(NotFoundException e){
			return ;
		}
	}
	/**
	 * dump the using resource
	 *
	 * @param	context Context
	 * @return	dump success return true,else return false;
	 * 	
	 * 
	*/
	public static boolean dumpResource(Application context)
	{
		if(null==context){
			return false;
		}
		
		Resources rc = context.getResources();
		if(rc==null){
			return false;
		}
		
		try{
			rc.getInteger(-1);//the api of device is special (compile by us)
			return true;
		}catch(NotFoundException e){
			return false;
		}
	}
	
	/**
	 * get the memory size of using resource
	 *
	 * @param	context Context
	 * @return	real size of using resource ,0 is failed;
	 * 	
	 * 
	*/
	public static int getResourceMemorySize(Application context)
	{
		if(null==context){
			return 0;
		}
		
		Resources rc = context.getResources();
		if(rc==null){
			return 0;
		}
		
		try{
			int size = rc.getInteger(-2);
			return size;
		}catch(NotFoundException e){
			return 0;
		}
	}
}
