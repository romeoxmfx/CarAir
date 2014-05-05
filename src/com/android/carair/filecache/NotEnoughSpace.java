package com.android.carair.filecache;

@SuppressWarnings("serial")
public class NotEnoughSpace extends Exception{
	public NotEnoughSpace(String message){
		super(message);
	}
}