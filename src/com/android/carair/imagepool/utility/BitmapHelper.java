package com.android.carair.imagepool.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.android.carair.imagepool.ImageCache;
import com.android.carair.imagepool.ImageHandler;
import com.android.carair.imagepool.ImagePool;
/**
 * 
 *
 */
public class BitmapHelper {
	  public static Bitmap Bytes2Bimap(byte[] b){ 
		 if(b!=null)
		 {
			Bitmap bitmap = null;
			try{
//				if(Build.VERSION.SDK_INT < 17){
//					BitmapFactory.Options options = new BitmapFactory.Options();
//					options.inDither = true;
//					options.inPreferredConfig = null;
//					bitmap = BitmapFactory.decodeByteArray(b, 0, b.length,options);
//				}else{
				bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
//				}
				
			}catch(OutOfMemoryError e){
				e.printStackTrace();
				bitmap = null;
			}finally{

			}
	        return bitmap;
		}else
		{
			return null;
		}
    } 

	public static Bitmap URI2Bimap(String uri) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = true;
			options.inPreferredConfig = null;
			return BitmapFactory.decodeFile(uri,options);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return null;
	} 
	  
	public static byte[] Bitmap2BytesJpeg(Bitmap bm){  
		byte [] a = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		if(bm != null)
			
			try{
				bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);    
				a = baos.toByteArray();
			}catch(OutOfMemoryError v){
				v.printStackTrace();
			}
		
	    try {
	    	if(baos != null)
	    		baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return a;
	} 
	
	public static byte[] Bitmap2BytesPng(Bitmap bm){
		byte [] a = null;
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	    if(bm != null){
	    	try{
		    	bm.compress(Bitmap.CompressFormat.PNG, 100, baos);    
		    	a = baos.toByteArray();
	    	}catch(OutOfMemoryError v){
				v.printStackTrace();
		    }
	    } 

	    try {
	    	if(baos != null)
	    		baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return a;
	} 
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap)
    {
    	return getRoundedCornerBitmap(bitmap,6);
    }
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,float roundPx)
    {
    	if(bitmap == null)
    		return null;
	    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
	        bitmap.getHeight(), bitmap.getConfig());
	    Canvas canvas = new Canvas(output);

	    final int color = 0xff424242;
	    final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	  //  final Rect rectd = new Rect(0, 0, bitmap.getWidth()*1.5, bitmap.getHeight()1.5);
	    final RectF rectF = new RectF(rect);

	  //  canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    
	    canvas.drawBitmap(bitmap, rect, rect, paint);

	    return output;
	}
	
	public static Bitmap drawableToBitmap(Drawable drawable) {   
        
        Bitmap bitmap = Bitmap.createBitmap(   
                                        drawable.getIntrinsicWidth(),   
                                        drawable.getIntrinsicHeight(),  
                                        drawable.getOpacity() == PixelFormat.OPAQUE ? Bitmap.Config.RGB_565  
                                                        : Bitmap.Config.ARGB_8888);   
        Canvas canvas = new Canvas(bitmap);   
        //canvas.setBitmap(bitmap);   
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());   
        drawable.draw(canvas);   
        return bitmap;   
	}
	
	public static Bitmap bitmapToScaleBitmap(Bitmap bm, int newWidth, int newHeight){
		
		if(bm == null)
			return null;
		
	    // 获得图片的宽高
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    
	    // 计算缩放比例
	    float scaleWidth  = (float)newWidth  / Math.max(width,height);
	    float scaleHeight = (float)newHeight / Math.max(width,height);
	    // 取得想要缩放的matrix参数
	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
	    
	    // 得到新的图片
	    Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,true);
	    return newbm;
	}
	
	public static Bitmap bitmapToFixedBitmap(Bitmap bm, int newWidth, int newHeight){
		
		if(bm == null)
			return null;
		
	    // 获得图片的宽高
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    
	    // 计算缩放比例
	    float scaleWidth  = (float)newWidth  / width;
	    float scaleHeight = (float)newHeight / height;
	    // 取得想要缩放的matrix参数
	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
	    
	    // 得到新的图片
	    Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,true);
	    return newbm;
	}
	public static boolean isJpeg(byte[] data){
		if(data != null && data.length > 2){
			//jpeg “0xFFD8”开头,byte为有符号八字节，需要做下转换
			if(data[0] == -1 && data[1] == -40)
				return true;
		}
		return false;
	}
	
	public static boolean isPng(byte[] data){
		if(data != null && data.length > 8){
			//png “0x89 50 4E 47 0D 0A 1A 0A”开头,byte为有符号八字节，需要做下转换
			if(data[0] == -119 && data[1] == 80 && data[2] == 78 && data[3] == 71 
					&& data[4] == 13 && data[5] == 10 && data[6] == 26 && data[7] == 10)
				return true;
		}
		return false;
	}
	
	public static Drawable getPercentImage(Drawable bgDrawable,int fontSize,int backColor,int foreColor,float density,int percent) {
		String url =  "temp:/" + fontSize + backColor + foreColor + density+ percent;
		ImageHandler handler = ImagePool.instance().getImageHandler(url, ImageCache.CACHE_CATEGORY_MRU,null);
		if(handler != null)
			return handler.getDrawable();
	
		Drawable d = bgDrawable;
		if(d == null)
			return null;
		
		int width = d.getIntrinsicWidth();
		int height = d.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width,height,bgDrawable.getOpacity() == PixelFormat.OPAQUE? Config.RGB_565:Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		int fill = (height * percent)/100;
		
		Paint p = new Paint();
		//draw background layer
		p.setColor(backColor);
		canvas.drawRect(0, 0 , width, height, p);
		//draw foreground layer
		p.setColor(foreColor);
	    canvas.drawRect(0, height - fill, width, height, p);
		//draw percent text
		p.setColor(0XFF999999);
		p.setAntiAlias(true);
		p.setTextSize(fontSize);
		String str = percent +"%";
		int offset = (int) (p.measureText(str)/2);
		canvas.drawText(percent +"%",width/2 - offset , height - 12 *density , p);
		//draw background drawable
		p.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
		canvas.drawBitmap(((BitmapDrawable)d).getBitmap(), 0, 0, p);
		//to rounded 
		//Bitmap dest = BitmapHelper.getRoundedCornerBitmap(bitmap);
		
		
		Future<String> path = ImagePool.instance().addBitmap(bitmap, url, ImageCache.CACHE_CATEGORY_MRU,false);
		try {
			path.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		handler = ImagePool.instance().getImageHandler(url, ImageCache.CACHE_CATEGORY_MRU,null);
		if(handler != null){
			((TBDrawable)handler.getDrawable()).setTargetDensity((int)(160*density));
			return handler.getDrawable();
		}
		
		return null;
	}
	
	public static Bitmap toGrayscaleAndMark(Context gContext,Bitmap bmpOriginal,String text) {
        
		int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth(); 
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
      
        //灰度化
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        //文字提示
        paint.reset();  
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        Rect bounds = new Rect();
        paint.setTextSize((int) (14 * scale));
        paint.getTextBounds(text, 0, text.length(), bounds);
        paint.setColor(Color.WHITE);
        
        int x = (bmpGrayscale.getWidth() -bounds.width())/2;
        int y = (bmpGrayscale.getHeight() - bounds.height())/2;
        
        c.drawRect(x,y,x+bounds.width()+5, y+bounds.height()+5, paint);
        paint.setColor(Color.RED);
        // text size in pixels
        c.drawText(text, x, y+bounds.height(), paint);
        return bmpGrayscale;
    }
}
