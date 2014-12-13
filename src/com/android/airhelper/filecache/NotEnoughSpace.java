package com.android.airhelper.filecache;

@SuppressWarnings("serial")
public class NotEnoughSpace extends Exception{
	public NotEnoughSpace(String message){
		super(message);
	}
}