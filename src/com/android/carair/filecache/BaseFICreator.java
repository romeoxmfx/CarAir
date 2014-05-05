package com.android.carair.filecache;

import java.io.UnsupportedEncodingException;

public class BaseFICreator implements FileInfoCreator{

	@Override
	public FileInfo onParseFileInfo(byte[] info,int offset,int len) {
		// TODO Auto-generated method stub
//		TaoLog.Logd("BaseFICreator", "onParseFileInfo start:"+fileStr);
		//获取文件信息和文件名的分割位置
		//int index = fileStr.indexOf(FileInfoBase.DIVISION);
		String fileStr;
		try {
			fileStr = new String(info,offset,len,"UTF-8");
//			if(info[offset+4] == 0x2d)
			if(fileStr.charAt(13) != FileInfoBase.DIVISION)	//格式错误
				return null;
			else{
				//获取最近读取时间和最近修改时间
				FileInfoBase fileInfo = new FileInfoBase();
				try{
					//获取最近修改时间
					//fileInfo.SetLastModify(Long.parseLong(fileStr.substring(0,13)));	
					fileInfo.SetLastAccess(Long.parseLong(fileStr.substring(0,13)));
				}catch(Exception e){
					e.printStackTrace();
					return null;
				}
				fileInfo.setFileName(new String(fileStr.substring(14)));
				return fileInfo;
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
	}

	@Override
	public FileInfo onUpdateFileInfo(String fileName, FileInfo info,int operation,long fileDirTicker){
		// TODO Auto-generated method stub
		FileInfoBase fileInfo = (FileInfoBase) info;
		
		switch(operation){
			case FileDir.READ:{//读取操作后更新
				fileInfo.SetLastAccess(fileDirTicker);
				break;
			}
			case FileDir.WRITE:{//写入操作后更新
				fileInfo.SetLastAccess(fileDirTicker);
				//fileInfo.SetLastModify(System.currentTimeMillis());
				break;
			}
			case FileDir.DELETE:{
				fileInfo.invalidate();
				break;
			}
			case FileDir.CREATE:{//创建文件后更新
				fileInfo = new FileInfoBase();
				fileInfo.setFileName(fileName);
				fileInfo.SetLastAccess(fileDirTicker);
				//fileInfo.SetLastModify(System.currentTimeMillis());
				
			}
		}
		return fileInfo;
	}

	@Override
	public int getFileInfoMinLength() {
		// TODO Auto-generated method stub
		return 14;
	}

}
