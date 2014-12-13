package com.android.airhelper.filecache;

public interface FileInfoCreator {
	public FileInfo onParseFileInfo(byte[] info,int offset,int len);
	/**
	 * 
	 * @param fileName		保存的文件名
	 * @param info			文件信息结构体，如果文件新建（FileDir.CREATE），则传入位空。
	 * @param operation		操作类型
	 * 						FileDir.READ：读
	 * 						FileDir.WRITE：写
	 * 						FileDir.DELETE：删
	 * 						FileDir.CREATE：新建
	 * @param fileDirTicker	FileDir内部提供的计数器，用于简单的单调增加计数。可用于记录文件的最近访问
	 * @return
	 */
	public FileInfo onUpdateFileInfo(String fileName, FileInfo info,int operation,long fileDirTicker);
	//获取文件信息的长度，不包含文件名
	public int getFileInfoMinLength();
}
