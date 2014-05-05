package com.android.carair.imagepool;


public abstract interface ImagePoolPrinter {
	
	public static final String IMAGE_COMPRESSION = "Image_Compression";
	public static final String IMAGE_HOST = "Image_Host";
	
	public abstract void printState(String paramString);
	
	public abstract void printExt(String[] paramString);
}