package cn.gov.cma.cimiss.dpc.util;

import cn.gov.cma.cimiss.dpc.config.DPCConfig;

/***
 * 字符串工具类
 * @author zhangbo
 *
 */
public class StringUtil {
	/**
	 * 得到计算时间
	 * @param start
	 * @param end
	 */
	public static String toUseTime(long start, long end){
		return (end-start) + "ms";
	}
	/**
	 * 对路径进行标准化
	 */
	public static String normalizePath( String path )
	{
		//将"\"替换为"/"，将连续多个"/"替换为一个"/"，将一个或空格替换"_",为去掉尾部的"/"
		if ( path == null )//健壮性
			return "";
		
		path = path.replaceAll( "\\\\+", "/" );//将一个或多个\替换为一个/
		path = path.replaceAll( "/{2,}", "/" );//将2个或2个以上的/替换为一个/
		path = path.replaceAll( "\\s+", "_" );//将1个或多个空格替换为下划线
		if ( path.endsWith( "." ) )//如果结尾是"."，则去掉，因为File.getParentFile().getAbsolutePath()以"."结尾
			path = path.substring( 0, path.length() - 1 );//去掉字符串结尾的"/"
		if ( path.endsWith( "/" ) )//如果结尾是"/"，执行至此只可能有一个结尾/
			path = path.substring( 0, path.length() - 1 );//去掉字符串结尾的"/"
		return path;
	}
	
	public static String getDataPathFromFtpPath( String absolutePath ) {
		//传入包含FTP目录字符串的路径返回该路径包含的familyName信息
		String ftpFilePath = normalizePath( DPCConfig.ftpFilePath );//得到FTP路径的规范化表示
		String normalizedPath = normalizePath( absolutePath );//得到传入参数的规范化表示
		int startIndex = normalizedPath.indexOf( ftpFilePath );
		if ( startIndex == -1 )//不是FTP目录中的文件
			return "";
		else
			return normalizedPath.substring( startIndex  + ftpFilePath.length() + 1 );//得到familyName的起始索引，跳过FTP路径结尾后的第1个/
	}
	
	public static String getFamilyNameFromFtpPath( String absolutePath ) {
		//传入包含FTP目录字符串的路径返回该路径包含的familyName信息
		String ftpFilePath = normalizePath( DPCConfig.ftpFilePath );//得到FTP路径的规范化表示
		String normalizedPath = normalizePath( absolutePath );//得到传入参数的规范化表示
		
		if ( normalizedPath.endsWith( ftpFilePath ) )//传入的就是ftpFilePath
			return "";
		
		int startIndex = normalizedPath.indexOf( ftpFilePath );
		if ( startIndex == -1 )//不是FTP目录中的文件
			return "";
		startIndex += ftpFilePath.length() + 1;//得到familyName的起始索引，跳过FTP路径结尾后的第1个/
		int endIndex = normalizedPath.indexOf( "/", startIndex );//截取后面/之前的部分，即familyName
		if ( endIndex != -1 )//familyName不是最后一级目录
			return normalizedPath.substring( startIndex, endIndex );//得到familyName
		else//familyName是最后一级目录
			return normalizedPath.substring( startIndex );//得到familyName
	}

}
