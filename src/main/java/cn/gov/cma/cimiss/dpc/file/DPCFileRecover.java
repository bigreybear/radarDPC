package cn.gov.cma.cimiss.dpc.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gov.cma.cimiss.dpc.config.DPCConfig;
import cn.gov.cma.cimiss.dpc.handle.DecoderDispatcher;
import cn.gov.cma.cimiss.dpc.util.StringUtil;

/***
 * 数据文件修复类
 * @author zhangbo
 *
 */
public class DPCFileRecover extends Thread{
	//Logger
	public static Logger logger = LoggerFactory.getLogger(DPCFileRecover.class);
	//恢复开始时间
	private Long startTime;
	//恢复结束时间
	private Long endTime;
	//恢复文件数
	private int recoverCnt = 0; 
	/***
	 * 字符串类型构造函数
	 * @param startTime yyyy-MM-dd HH:mm:ss
	 * @param endTime
	 */

	private DecoderDispatcher decoderDispatcher = null;

	public DPCFileRecover(String startTime, String endTime, DecoderDispatcher decoderDispatcher){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		try{
			Long start = startTime == null ? null : sdf.parse(startTime).getTime();
			Long end = endTime == null ? null : sdf.parse(endTime).getTime();
			this.startTime = start;
			this.endTime = end;
		}catch (Exception e){
			logger.error("转换时间失败，时间格式为:yyyy-MM-dd_HH:mm:ss");
		}
		this.decoderDispatcher = decoderDispatcher;
	}
	
	@Override
	public void run() {
		super.run();
		//1、从FTP路径获得文件
		//2、检测恢复时间是否符合条件
		//3、调用DecoderDispatcher进行任务分发
		File dir = new File(DPCConfig.ftpFilePath);
		Stack< File > dirStack = new Stack< File >();//存储所有未删除下属文件的目录，用以实现非递归删除
		if ( !dir.isDirectory() )//dir不是合法目录，直接返回
			return;
		dirStack.push( dir );//首先将根目录压入栈，做为迭代起点
		while ( !dirStack.empty() )//当且仅当栈为空时删除操作结束
		{
			File currentDir = dirStack.pop();//弹出栈顶元素，先删除直接下属文件，再将直接子目录压栈
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
					
					if ( willRecover(creationTime) )//判断是否在恢复时效内
					{
						String path = filesAndDirs[ i ].getAbsolutePath();
						//转化为分发器识别的路径 即不包含FTP目录之前的路径
						path = path.substring(path.indexOf(DPCConfig.ftpFilePath) + DPCConfig.ftpFilePath.length());
						this.decoderDispatcher.dispatch( StringUtil.normalizePath(path) );
						logger.info( "文件" + filesAndDirs[ i ].getAbsolutePath() + " 开始恢复!" );
						recoverCnt ++ ;
					}//此文件在恢复时效内，进行恢复
					else//文件不在恢复时效内，继续搜索下一个文件
						continue;
				}
				else//目录，压栈
				{
					dirStack.push( filesAndDirs[ i ] );//最后进入栈的目录被最先删除
				}
			}//逐个处理当前正在被删除目录的文件和子目录
		}//end while
		logger.info("数据恢复完成, 共恢复" + recoverCnt + "个文件");
	}
	/**
	 * 判断指定时间是否在要恢复的时间内
	 * @param time
	 * @return
	 */
	private boolean willRecover(Long time){
		Long tempStartTime = startTime;
		Long tempEndTime = endTime;
		//如果开始时间为null 则使用默认开始时间
		if(tempStartTime == null){
			tempStartTime = System.currentTimeMillis() - DPCConfig.ftpRecoverPeriod * 60L * 60L * 1000L;
		}
		//如果结束时间为null 则使用当前时间
		if(tempEndTime == null){
			tempEndTime = System.currentTimeMillis();
		}
		if(time >= tempStartTime && time <= tempEndTime)
			return true;
		else return false;
	}
}

