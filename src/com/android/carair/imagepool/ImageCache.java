package com.android.carair.imagepool;

import java.io.File;
import java.nio.ByteBuffer;

import android.app.Application;
import android.content.Context;

import com.android.airhelper.filecache.FileCache;
import com.android.airhelper.filecache.FileDir;
import com.android.airhelper.filecache.HighSpeedTmpCache;
import com.android.carair.imagepool.IImageQualityStrategy.ImageIndex;
import com.android.carair.imagepool.utility.TBDrawable;
import com.taobao.cache.Cache;
import com.taobao.cache.ChocolateCache.CacheObject;

/**
 * 图片缓存类
 * 
 * 1. 提供在本地保存已下载的图片功能
 * 2. 根据uri在缓存中寻找并加载图片为图片资源句柄
 * 3. 提供不同的保存策略
 */

public class ImageCache {
	
	/** 缓存种类常量CACHE_CATEGORY_NONE ，代表退出后清除
	 * */
    public static final int CACHE_CATEGORY_NONE = 0;
    
    /** 缓存种类常量CACHE_CATEGORY_PERSIST ，代表本地 永久存储，在未达到上限前，不自动删除的本地文件系统缓存。
     *  应用负责删除
	 * */
    public static final int CACHE_CATEGORY_PERSIST = 1; //不自动删除的本地文件系统缓存
    
    /** 缓存种类常量CACHE_CATEGORY_PERSIST_REPLACE ，代表本地 存储,在设置新的图片是自动清除Cache中老图片中的数据
     *  应用负责删除
	 * */
    public static final int CACHE_CATEGORY_PERSIST_AUTOREPLACE = 4; //不自动删除的本地文件系统缓存

    
    /** 缓存种类常量CACHE_CATEGORY_MRU ，代表以Most Recently Used策略替换
	 * */   
    public static final int CACHE_CATEGORY_MRU = 2; //cache Most Recently Used
    
    /** 缓存种类常量CACHE_CATEGORY_NOREPLACE_PERSIST ，本地永久存储，和CACHE_CATEGORY_PERSIST相比，这个需要
     * 应用自己来负责Delete，不会因为积累而被清理
  	 * */ 
    public static final int CACHE_CATEGORY_NOREPLACE_PERSIST = 3; //本地永久存储，和CACHE_CATEGORY_PERSIST相比
    
    
    
    /**
     * 图片缓存的目录名
     */
    private String TEMP_CACHE_FILE_NAME = "tempImage.dat"; //客户端退出后就可以清空的图片
    private String MRU_CACHE_FOLDER_NAME = "mru_images";              //不自动删除，只有当空间限制的时候才根据策略删除
    private String PERSIST_CACHE_FOLDER_NAME = "persist_images";          //不删除，只有用户要求的时候才删除
    private String NOREPLACE_PERSIST_CACHE_FOLDER_NAME = "nr_persist_images";          
    
    
    //private HighSpeedTmpCache m_fileCache; //高速缓存
    //private FileDir m_cache_MRU; // MRU缓存 - FileDir是线程安全的
    private FileDir m_cache_persist; //永久缓存，100个上限替换
    //private FileDir m_cache_noreplace_persist; //不替换的永久缓存，用于启动画面等
    private Context m_context;
    private IImageQualityStrategy m_strategy; //当前使用的图片下载策略

    //在线程中异步初始化，初始化操作比较耗时
    private void _asyncInit() {
    	    	
        //m_fileCache = FileCache.getInsatance((Application) m_context.getApplicationContext()).getTmpCacheInstance(TEMP_CACHE_FILE_NAME, true);
        //m_cache_MRU = FileCache.getInsatance((Application) m_context.getApplicationContext()).getFileDirInstance(MRU_CACHE_FOLDER_NAME, true);
        m_cache_persist = FileCache.getInsatance((Application) m_context.getApplicationContext()).getFileDirInstance(PERSIST_CACHE_FOLDER_NAME, true);
        //m_cache_noreplace_persist = FileCache.getInsatance((Application) m_context.getApplicationContext()).getFileDirInstance(NOREPLACE_PERSIST_CACHE_FOLDER_NAME, true);
                                
//        if(m_cache_MRU!=null)
//        {
//        	m_cache_MRU.enableNoSpaceClear(true);
//        }
        
        if(m_cache_persist!=null)
        {
        	m_cache_persist.enableNoSpaceClear(true);
        }
        class StartWorker implements Runnable {
            @Override
            public void run() {
                try {
                    _Init();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        ;
        Thread t = new Thread(new StartWorker());
        t.start();

    }

    private boolean m_bInit = false; //indicate if the cache is already start the init

    //初始化，费时，在StartWorker线程中调用
    private void _Init() {
    	//初始化各Cache
        m_bInit = true;
//        if (m_fileCache == null) {
//            m_fileCache = FileCache.getInsatance((Application)
//                    m_context.getApplicationContext())
//                    .getTmpCacheInstance(TEMP_CACHE_FILE_NAME, false);
//        }//no sdCard

//        if (null != m_fileCache)
//            m_fileCache.init();

//        if (m_cache_MRU == null) {//no sdCard
//            m_cache_MRU = FileCache.getInsatance((Application)
//                    m_context.getApplicationContext())
//                    .getFileDirInstance(MRU_CACHE_FOLDER_NAME, false);
//        }
//        if (null != m_cache_MRU)
//            m_cache_MRU.init(null, null);

        if (m_cache_persist == null)//no sdCard
        {
            m_cache_persist = FileCache.getInsatance((Application)
                    m_context.getApplicationContext())
                    .getFileDirInstance(PERSIST_CACHE_FOLDER_NAME, false);
        }
                
        if (null != m_cache_persist) {
            m_cache_persist.init(null, null);
            m_cache_persist.setCapacity(250);
        }
        
//        if (m_cache_noreplace_persist == null)//no sdCard
//        {
//        	m_cache_noreplace_persist = FileCache.getInsatance((Application)
//                    m_context.getApplicationContext())
//                    .getFileDirInstance(NOREPLACE_PERSIST_CACHE_FOLDER_NAME, false);
//        }
//                
//        if (null != m_cache_noreplace_persist) {
//        	m_cache_noreplace_persist.init(null, null);            
//        }

        Cache.read(null);
    }

    /** ImageCache的构造函数
     * @param context 设置Context
     */
    public ImageCache(Context context ) {
        m_context = context;        
        _asyncInit();
    }

    /** ImageCache的release操作
     */
    
    public void release() {
    	if( m_context != null )
    	{
//    		if( m_fileCache!=null)
//    			FileCache.getInsatance((Application) m_context.getApplicationContext()).releaseTmpCache(TEMP_CACHE_FILE_NAME,m_fileCache.isInSdcard());
    		
    		
    		if(m_cache_persist!=null)
    			FileCache.getInsatance((Application) m_context.getApplicationContext()).releaseFileDir(PERSIST_CACHE_FOLDER_NAME, m_cache_persist.isInSdcard());
    		
//    		if(m_cache_MRU!=null)
//    			FileCache.getInsatance((Application) m_context.getApplicationContext()).releaseFileDir(MRU_CACHE_FOLDER_NAME, m_cache_MRU.isInSdcard());
//    		
//    		if(m_cache_noreplace_persist!=null)
//    			FileCache.getInsatance((Application) m_context.getApplicationContext()).releaseFileDir(NOREPLACE_PERSIST_CACHE_FOLDER_NAME, m_cache_noreplace_persist.isInSdcard());
    	}
    	Cache.close();
    
    }
	
  
    

    /** 根据图片URL和缓存种类获得图片Drawable
     * @param url 图片的url与图片的大小是对应的；在有选择策略的时候，和真正的图片也是一致的
     * @param type 缓存类别
     * @return 图片的TBDrawable对象
     */
    public TBDrawable getDrawalbe(String url, int type) {
        if (!m_bInit) {
            return null;
        }

        if (url == null)
            return null;
        
        String name = URLtoFileName(url);        
        byte[] bs = null;
        ImageIndex ii = null;
        String key = null;
        int category = 0;
    	if(m_strategy != null){
    		ii = m_strategy.toCacheIndex(url);
    	}
    	
    	if(ii != null && ii.mIdentificacion != null){
    		int[] categorys = Cache.hasCategorys(ii.mIdentificacion);
    		ImageIndex hited = m_strategy.hitCache(ii.mIdentificacion,ii.mImageInfo, categorys);
    		if(hited != null){
    			key = hited.mIdentificacion;
    			category = hited.mImageInfo;
    		}
    	}else{
    		key = name;
    	}
    	if(key != null){
			CacheObject co = Cache.read(key, category);
			if(co != null)
				bs = co.mData;
		}
    	/*
        switch (type) {
            case CACHE_CATEGORY_NONE:
                if (null != m_fileCache) {
                    m_fileCache.init();
                    if(m_strategy!=null)
                    {                    	
                        String[] files = m_fileCache.filtrFile( m_strategy.getBaseUrl(name) );                   
                    	name = m_strategy.decideStoragePath( name, files);
                    }
               
                    
                    bs = m_fileCache.read(name);
                }
                break;

            case CACHE_CATEGORY_PERSIST:
            case CACHE_CATEGORY_PERSIST_AUTOREPLACE:
                if (m_cache_persist != null) {
                    //这里Call init是保证m_cache_persist的Init已经完成
                    m_cache_persist.init(null, null);
                    
                    
                    if(m_strategy!=null)
                    {                    	
                        String[] files = m_cache_persist.filtrFile( m_strategy.getBaseUrl(name) );                   
                    	name = m_strategy.decideStoragePath( name, files);
                    }
               
                    bs = m_cache_persist.read(name);
                    //TaoLog.Logv(TaoLog.TAOBAO_TAG, "image_path:" + m_cache_persist.getDirPath());
                }
                break;
            case CACHE_CATEGORY_NOREPLACE_PERSIST:
                if (m_cache_noreplace_persist != null) {
                	m_cache_noreplace_persist.init(null, null);
                    
                    
                    if(m_strategy!=null)
                    {                    	
                        String[] files = m_cache_noreplace_persist.filtrFile( m_strategy.getBaseUrl(name) );                   
                    	name = m_strategy.decideStoragePath( name, files);
                    }
               
                    bs = m_cache_noreplace_persist.read(name);

                }
                break;   
            case CACHE_CATEGORY_MRU:
                if (null != m_cache_MRU) {
                    m_cache_MRU.init(null, null);                   
                    if(m_strategy!=null)
                    {                    	
                        String[] files = m_cache_MRU.filtrFile( m_strategy.getBaseUrl(name) );                   
                    	name = m_strategy.decideStoragePath( name, files);
                    }
                    bs = m_cache_MRU.read(name);
                }
                break;
        }
        */
    	
    	if(bs == null){
    		if (m_cache_persist != null) {
                //这里Call init是保证m_cache_persist的Init已经完成
                m_cache_persist.init(null, null);
                
                
                if(m_strategy!=null)
                {                    	
                    String[] files = m_cache_persist.filtrFile( m_strategy.getBaseUrl(name) );                   
                	name = m_strategy.decideStoragePath( name, files);
                }
           
                bs = m_cache_persist.read(name);
                //TaoLog.Logv(TaoLog.TAOBAO_TAG, "image_path:" + m_cache_persist.getDirPath());
            }
    	}
    	//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ImageCache read "+url+" bs "+bs);
		TBDrawable ret = ImagePool._createTBDrawable(bs, url);
		if(bs != null && ret == null )
		{
			//从FileCache中的数据不能够生成Bitmap，则删除这个Cache
			deleteFile(url, type);
		}
						
		
		//判断若为webp格式的图片转换为jpeg格式存cache
		/*if (url.endsWith(".webp") && ret != null && !BitmapHelper.isJpeg(bs) && ImagePool.instance().m_IC != null) {
			byte[] data = BitmapHelper.Bitmap2BytesPng(ret.getBitmap());
			ImagePool.instance().m_IC.saveData(url, data, type);
		}*/
		return ret;
        //TaoLog.Loge(TaoLog.IMGPOOL_TAG, "ImageCache getDrawalbe:" + name + " ,bs  " + bs);
        
        
    }

   

    /** 保存ImageHandler中的图片数据到指定类别的缓存中
     * @param url 含有图片数据的url， 图片的url与图片的大小是对应的；在有选择策略的时候，和真正的图片也是一致的
     * @data  压缩的图片数据
     * @param saveType 缓存类别
     * @return 保存是否成功
     */
    public boolean saveData(String url, byte [] data, int saveType) {
    	
    	
        if (data == null || url == null)
            return true;
        String name = URLtoFileName(url);
        boolean ret = false;                
        
        if (data != null) {
        	ImageIndex ii = null;
        	if(m_strategy != null){
        		ii = m_strategy.toCacheIndex(url);
        	}
            switch (saveType) {
                case CACHE_CATEGORY_NONE:
//                    if (null != m_fileCache) {
//                        ret = m_fileCache.append(name, ByteBuffer.wrap(data));
//                        
//                    }
                	if(ii != null && ii.mIdentificacion != null)
            			ret = Cache.write(ii.mIdentificacion,ii.mImageInfo, data,null, Cache.TRAVELERS_CACHE);
                	else
                		ret = Cache.write(name,data, Cache.TRAVELERS_CACHE);	
                    break;
                case CACHE_CATEGORY_PERSIST:
                case CACHE_CATEGORY_PERSIST_AUTOREPLACE:
//                    if (null != m_cache_persist) {
//                        m_cache_persist.init(null, null);
//                        ret = m_cache_persist.write(name, ByteBuffer.wrap(data));                        
//                        /**
//                         * 在图片bash 路径目录创建.nomedia 隐藏多媒体文件
//                         */
//                        m_cache_persist.hidenMediaFile();
//                    }
//                    break;
                    
                case CACHE_CATEGORY_NOREPLACE_PERSIST:
//                    if (null != m_cache_noreplace_persist) {
//                    	m_cache_noreplace_persist.init(null, null);
//                        ret = m_cache_noreplace_persist.write(name, ByteBuffer.wrap(data));
//                        /**
//                         * 在图片bash 路径目录创建.nomedia 隐藏多媒体文件
//                         */
//                        m_cache_noreplace_persist.hidenMediaFile();
//                    }
//                    break;
                case CACHE_CATEGORY_MRU:
//                    if (null != m_cache_MRU) {
//                        ret = m_cache_MRU.write(name, ByteBuffer.wrap(data));
//                        m_cache_MRU.hidenMediaFile();
//                    }
                	if(ii != null && ii.mIdentificacion != null)
                		ret = Cache.write(ii.mIdentificacion,ii.mImageInfo, data,null, Cache.INHABITANTS_CACHE);
                	else
                		ret = Cache.write(name,data, Cache.INHABITANTS_CACHE);
                    break;
            }
        }
        //TaoLog.Loge(TaoLog.IMGPOOL_TAG, "ImageCache write:" + name + " , ret" + ret);
//        if(ret)
    	//TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ImageCache.save file to cache " + saveType + " ret: " + ret + " url " + url);
        return ret;
        
    }
    
    /**从缓存中删除URL对应的图片数据文件。只有永久保存的文件需要用户手动删除，其他的都由缓存自动管理，不需要主动调用删除     
     * @param url 图片的URL
     * @param type 图片的缓存类别
     */
    public void deleteFile(String url, int type) {
        String name = URLtoFileName(url);        
        switch (type) {
            case CACHE_CATEGORY_PERSIST:
            case CACHE_CATEGORY_PERSIST_AUTOREPLACE:
            case CACHE_CATEGORY_NOREPLACE_PERSIST:
            case CACHE_CATEGORY_NONE:
                if (null != m_cache_persist) {                    
                    m_cache_persist.init(null, null);
                    m_cache_persist.delete(name);
                    //TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ImageCache.delete file from persist cache, with ret: " + ret + " url " + name);
                }
                break;
            
//                if (null != m_cache_noreplace_persist) {                    
//                    m_cache_noreplace_persist.init(null, null);
//                    m_cache_noreplace_persist.delete(name);
//                    //TaoLog.Logd(TaoLog.IMGPOOL_TAG, "ImageCache.delete file from persist cache, with ret: " + ret + " url " + name);
//                }
//                break;
//           
//            	if( null != m_fileCache)
//            	{            		
//            		m_fileCache.init();
//            		m_fileCache.delete(name);
//            	}
//            	break;
        }
        //TaoLog.Loge(TaoLog.IMGPOOL_TAG, "ImageCache deleteFile:" + name );

    }

    /** 清理指定Cache     
     * @param policy 需要清理的Cache类型     
     */
    public void clearCache(int policy)
    {
    	switch(policy)
    	{
    	case CACHE_CATEGORY_NONE:
//    		 if(m_fileCache != null)
//             {
//             	m_fileCache.clear();
//             }
//            break;

        case CACHE_CATEGORY_PERSIST:
        case CACHE_CATEGORY_PERSIST_AUTOREPLACE:
        case CACHE_CATEGORY_MRU:
        case CACHE_CATEGORY_NOREPLACE_PERSIST:
            if(m_cache_persist != null)
            {
            	m_cache_persist.clear();
            }
            break;
        
//            if (null != m_cache_MRU) {
//                m_cache_MRU.clear();
//            }
//            break;
//       
//            if (null != m_cache_noreplace_persist) {
//            	m_cache_noreplace_persist.clear();
//            }
//            break;
    	}
    	
    }
    //设置图片下载策略
    void setImageQualityStrategy(IImageQualityStrategy s)
    {
    	m_strategy =s;
    }
    
    /** 从URL中提取映射到缓存中File名
     * @param URL
     * @return
     */
    private String URLtoFileName(String URL) {
        //去掉http://和域名的url
    	
    	
        if (URL == null)
            return null;

    	if( null != m_strategy)
    		return m_strategy.onURLtoCacheFileName(URL);
    	
    	//原来缺省的获得方法
        int offset = URL.lastIndexOf('/');
        if (offset != -1 && offset < URL.length()) {
            return new String(URL.substring(offset+1));
        } else {
            return URL;
        }
    }

    /** 根据URL获取指定缓存种类中的路径名
     * @param URL
     * @return
     */
    
    String URLtoPersistPath(String URL, int cache_type) {
        String fullpath = "";
        switch (cache_type) {
            case CACHE_CATEGORY_NONE:
                break;
            case CACHE_CATEGORY_MRU:
            case CACHE_CATEGORY_PERSIST:
            case CACHE_CATEGORY_PERSIST_AUTOREPLACE:
            case CACHE_CATEGORY_NOREPLACE_PERSIST:
                if (null != m_cache_persist) {
                    fullpath = m_cache_persist.getDirPath() + "/" + URLtoFileName(URL);
                }
                break;
            
//                if (null != m_cache_MRU) {
//                    fullpath = m_cache_MRU.getDirPath() + "/" + URLtoFileName(URL);
//                }
//                break;
//            
//                if (null != m_cache_noreplace_persist) {
//                    fullpath = m_cache_noreplace_persist.getDirPath() + "/" + URLtoFileName(URL);
//                }
//                break;
        }
        return fullpath;
    }    
    
    void saveFile(String URL, int cache_type,byte[] data){
        switch (cache_type) {
            case CACHE_CATEGORY_NONE:
                break;
            case CACHE_CATEGORY_MRU:
            case CACHE_CATEGORY_PERSIST:
            case CACHE_CATEGORY_PERSIST_AUTOREPLACE:
            case CACHE_CATEGORY_NOREPLACE_PERSIST:
                if (null != m_cache_persist) {
                	m_cache_persist.init(null, null);
                	String name = URLtoFileName(URL);
                	m_cache_persist.write(name, ByteBuffer.wrap(data));
                }
                break;
            
//                if (null != m_cache_MRU) {
//                    fullpath = m_cache_MRU.getDirPath() + "/" + URLtoFileName(URL);
//                }
//                break;
//            
//                if (null != m_cache_noreplace_persist) {
//                    fullpath = m_cache_noreplace_persist.getDirPath() + "/" + URLtoFileName(URL);
//                }
//                break;
        }
    	
    }
}
