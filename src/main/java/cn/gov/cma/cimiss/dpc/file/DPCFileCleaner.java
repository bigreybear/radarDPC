package cn.gov.cma.cimiss.dpc.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import cn.gov.cma.cimiss.dpc.example.MyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gov.cma.cimiss.dpc.config.DPCConfig;
import cn.gov.cma.cimiss.dpc.util.StringUtil;

/***
 * 数据清理器
 * 负责对FTP服务器目录数据、归档数据进行定时清理
 * @author zhangbo
 *
 */
public final class DPCFileCleaner extends Thread {
	private static Logger logger = LoggerFactory.getLogger( DPCFileCleaner.class );
	private Timer timer;//文件删除定时器
	private static DPCFileCleaner dpcFileCleaner = null;//单实例类
	private static MyWriter mw = new MyWriter();
	
	private DPCFileCleaner() {
		timer = new Timer();
	}
	
	public static DPCFileCleaner getInstance(){
		if ( dpcFileCleaner == null ) {
			dpcFileCleaner = new DPCFileCleaner();
		}
		return dpcFileCleaner;
	}
	
	@Override
	public void run() {
		//1.定时清除FTP目录数据
		//2.定时清除中间序列化结果数据
		long delay = 10 * 1000;//延迟10秒钟启动删除，参数含义:delay in milliseconds before task is to be executed.
//		long period = DPCConfig.cleanerPeriod * 60 * 60 * 1000;//time in milliseconds between successive task executions.
		long period = 15 * 1000;
		timer.schedule( new CleanerTimerTask(), delay, period );//启动定时器任务
	}
	/***
	 * 停止数据清除服务
	 */
	public void close() {
		logger.info( "停止文件清除定时器和当前文件清除任务" );
		timer.cancel();//Terminates this timer, discarding any currently scheduled tasks.
	}
	
	protected void finalize() 
	{
		close();
	}
	
	public static void deleteFilesFromDirectory( String dirName )
	{
		//非递归删除dirName目录下所有符合条件的文件
		long timeoutPeriod = 0;//记录当前目录的超时时间，用毫秒表示
		
		dirName = StringUtil.normalizePath( dirName );//先将路径转换为合法路径格式
		File dir = new File( dirName );//构建File对象
		Stack< File > dirStack = new Stack< File >();//存储所有未删除下属文件的目录，用以实现非递归删除
		if ( !dir.isDirectory() )//dir不是合法目录，直接返回
			return;
		dirStack.push( dir );//首先将根目录压入栈，做为迭代起点
		while ( !dirStack.empty() )//当且仅当栈为空时删除操作结束
		{
			File currentDir = dirStack.pop();//弹出栈顶元素，先删除直接下属文件，再将直接子目录压栈
			timeoutPeriod = getTimeoutPeriodByPath( StringUtil.normalizePath( currentDir.getAbsolutePath() ) );//得到当前正在删除目录的超时毫秒数
			
			File filesAndDirs[] = currentDir.listFiles();//得到所有当前正在被删除目录的文件和子目录列表
			for ( int i = filesAndDirs.length - 1; i >= 0 ; i-- )//逐个处理当前正在被删除目录的文件和子目录
			{
				if ( filesAndDirs[ i ].isFile() )//文件，判断是否删除
				{
					Path nioFile = filesAndDirs[ i ].toPath();//构建java.nio.file.Path对象
					long creationTime = 0;//利用新式IO读取文件的创建时间
					try {
						BasicFileAttributes attrs = Files.readAttributes( nioFile, BasicFileAttributes.class );
						creationTime = attrs.creationTime().toMillis();//得到文件创建时间的毫秒表示
					} catch ( IOException e1 ) {
						logger.error( "读取文件创建时间属性错误: " + filesAndDirs[ i ].getAbsolutePath() );
					}
					
					if ( System.currentTimeMillis() - creationTime > timeoutPeriod )//此文件已超时，需要删除
					{
						try {
							if ( filesAndDirs[ i ].getName().compareToIgnoreCase( "dpc.log" ) != 0 )//避免删除日志目录时删除了当前的日志文件
							{
							    mw.wirteByFileName(nioFile.toString());
								Files.delete( nioFile );//使用Java 1.7的新类库删除文件，该方法可以根据各种情况抛出异常
								logger.info( "文件" + filesAndDirs[ i ].getAbsolutePath() + "到达生命周期，进行删除!" );
							}
						} catch ( IOException e ) {
							logger.error( "删除文件错误: " + filesAndDirs[ i ].getAbsolutePath() );
						}
					}//此文件已超时，需要删除
					else//文件尚未超时，继续搜索下一个文件
						continue;
				}
				else//目录，压栈
				{
					dirStack.push( filesAndDirs[ i ] );//最后进入栈的目录被最先删除
				}
			}//逐个处理当前正在被删除目录的文件和子目录
		}//end while
	}//end method deleteFilesFromDirectory
	
	private static long getTimeoutPeriodByPath( String dirName )
	{
		//传入以ftp目录，middleResult目录或log开头的目录，返回该目录对应的文件超时毫秒数
		long timeoutPeriod = 0;//记录当前目录的超时时间，用毫秒表示

		return timeoutPeriod;
	}
	
	private static void deleteFilesFromLogDirectory()
	{
		File dir = new File( "log" );//构建File对象
		if ( !dir.isDirectory() )//dir不是合法目录，直接返回
			return;
		
		File files[] = dir.listFiles();//得到所有当前正在被删除目录的文件
		for ( int i = files.length - 1; i >= 0 ; i-- )//逐个处理当前正在被删除目录的文件
		{
			if ( files[ i ].isFile() )//文件，判断是否删除
			{
				String fileName = files[ i ].getName();//得到文件名,dpc.log, dpc.log.1等等
				if ( fileName.compareToIgnoreCase( "dpc.log" ) == 0 )//不删除当前日志文件
					continue;
				if ( fileName.startsWith( "dpc.log" ) )//进一步判断是第几个日志文件
				{
					try {
						int logFileId = Integer.parseInt( fileName.substring( fileName.lastIndexOf( "." ) + 1 ) );
						if ( logFileId <= 5 )//只保留5个历史文件
							continue;
					} catch ( NumberFormatException exception ) {
						//有转换异常说明该文件也需要被删除
					}
				}//进一步判断是第几个日志文件
				
				Path nioFile = files[ i ].toPath();//构建java.nio.file.Path对象
				try {
					Files.delete( nioFile );//使用Java 1.7的新类库删除文件，该方法可以根据各种情况抛出异常
					logger.debug( "文件" + files[ i ].getAbsolutePath() + "到达生命周期，进行删除!" );
				} catch ( IOException e ) {
					logger.error( "删除文件错误: " + files[ i ].getAbsolutePath() );
				}
			}//文件，判断是否删除
			else//万一有子目录则删除子目录下的全部文件
				deleteFilesFromDirectory( files[ i ].getAbsolutePath() );
		}//逐个处理当前正在被删除目录的文件
	}
	
	private class CleanerTimerTask extends TimerTask {//内部类

		@Override
		public void run() {
			logger.info( "文件清除器开始工作 ：" + new Date() );
			
			File ftpPath = new File( DPCConfig.ftpFilePath );//首先删除FTP接收目录的旧文件
			deleteFilesFromDirectory( StringUtil.normalizePath( ftpPath.getAbsolutePath() ) );//非递归删除根目录下的所有旧文件

			logger.info( "文件清除器结束工作：" + new Date() );
		}
	}//end inner Class CleanerTimerTask
	
}//end class DPCFileCleaner

