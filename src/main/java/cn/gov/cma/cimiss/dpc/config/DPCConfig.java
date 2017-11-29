package cn.gov.cma.cimiss.dpc.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gov.cma.cimiss.dpc.util.StringUtil;

/***
 * 全局配置类
 * @author zhangbo
 *
 *
 */
public class DPCConfig {
	
	/**
	 * 静态通用设置 仅在重启后变更生效
	 */
	public static final String STATIC_SYSTEM_CONFIG_FILENAME = "conf/StaticSystemConfig.xml";//静态系统配置路径，仅在重启系统后生效
	public static final String DYNAMIC_SYSTEM_CONFIG_FILENAME = "conf/DynamicSystemConfig.xml";//动态系统配置路径,reload后生效
	public static final String LOG4J_PROPERTIES_FILENAME = "conf/log4j.properties";//Log4j配置文件路径
	/**
	 * FTP服务器相关配置
	 */
	public static Integer ftpPort = 2221;//FTP监听端口
	public static String ftpUserName = "dpc";//FTP用户名
	public static String ftpUserPass = "dpcsecret";//FTP用户密码
	public static String ftpFilePath = "ftp";//FTP文件接收相对路径
    public static String compactSrcDir = "";//压缩FTP源文件的目录
	
	/**
	 * 解码器相关配置
	 */
	public static int decoderThreadMaxNum = 30;//解码线程  = 写入线程数

	public static int cleanerPeriod = 12;//本地文件定时删除器执行周期 单位:小时
	public static int socketPort = 8088; //交互模块Socket系统端口
	
	/**
	 * 动态通用配置reload后立即生效
	 */
	public static int ftpClearPeriod = 48;//FTP文件超时时间  单位:小时
	public static int middleResultPathClearPeriod = 48;//中间结果删除时间 单位:小时
	public static int ensembleCacheClearPeriod = 12;//集合预报分成员结果缓存时间 单位:小时
	public static boolean isMasterNode = true;//是否是主节点 
	public static int ftpRecoverPeriod = 1;//FTP文件默认恢复时间 单位：小时
	public static boolean decoderSwitch = true; //解码开关，当关闭时不进行解码及之后的操作
	public static boolean writeSwitch = true;//写入开关，当关闭时不进行真实写入 
	public static boolean instantWriteSwitch = true;//立即写入模式,true为打开，false为关闭
	public static long threadWaitTime = 30;//关系系统时解码线程最长等待时间(单位:分钟)
	
	/**
	 * 其他变量
	 */
	public static Logger logger = LoggerFactory.getLogger( DPCConfig.class );//Log4j日志
	public static SAXBuilder builder = new SAXBuilder();//XML解析器


	public static void init() {
		PropertyConfigurator.configure( LOG4J_PROPERTIES_FILENAME );//初始化log4j配置信息
		readStaticSystemConfig();//从XML中读取静态系统配置信息
		reload();//重新读取系统加数据全部的配置信息
	}
	
	private static void readStaticSystemConfig() {
		try {
			Document doc = builder.build( new File( STATIC_SYSTEM_CONFIG_FILENAME ) );//打开静态系统配置文件
			Element root = doc.getRootElement();//根元素<root>
			
			ftpPort = Integer.parseInt( root.getChild( "ftpPort" ).getValue() );//FTP服务器端口
			logger.info( "更新 ftpPort -> " + ftpPort );
			
			String ftpUserNameTemp = root.getChildText( "ftpUserName" );//FTP用户名
			if ( !isEmpty( ftpUserNameTemp ) ) {
				ftpUserName = ftpUserNameTemp;
				logger.info( "更新 ftpUserName -> " + ftpUserName );
			}
			
			String ftpUserPassTemp = root.getChildText( "ftpUserPass" );//FTP密码
			if ( !isEmpty( ftpUserPassTemp ) ) {
				ftpUserPass = ftpUserPassTemp;
				logger.info( "更新 ftpUserPass -> " + ftpUserPass );
			}
			
			String ftpFilePathTemp = root.getChildText( "ftpFilePath" );//FTP文件接收相对路径
			if ( !isEmpty( ftpFilePathTemp ) ) {
				ftpFilePath = StringUtil.normalizePath( ftpFilePathTemp );
				logger.info( "更新 ftpFilePath -> " + ftpFilePath );
			}
			
			decoderThreadMaxNum = Integer.parseInt( root.getChild( "decoderThreadMaxNum" ).getValue() );//解码器线程数
			logger.info( "更新 decoderThreadMaxNum -> " + decoderThreadMaxNum );

			cleanerPeriod = Integer.parseInt( root.getChild( "cleanerPeriod" ).getValue() );//本地文件定时删除器执行周期单位小时
			logger.info( "更新 cleanerPeriod -> " + cleanerPeriod );
			
			socketPort = Integer.parseInt( root.getChild( "socketPort" ).getValue() );//交互系统SOCKET通信端口
			logger.info( "更新 socketPort -> " + socketPort );

			compactSrcDir = root.getChildText("compactSrcDir");
			logger.info( "更新compact source directory -> " + compactSrcDir);

		} catch ( JDOMException e ) {
			logger.error( "读取静态配置文件" + STATIC_SYSTEM_CONFIG_FILENAME + "错误!" );
		} catch ( IOException e ) {
			logger.error( "找不到静态配置文件" + STATIC_SYSTEM_CONFIG_FILENAME );
		} catch ( Exception e ) {
			logger.error( "读取静态配置文件出现其他异常，使用系统默认值" );
		}
	}

	public static void reload() {
		readDynamicSystemConfig();//从XML中读取系统系统动态配置信息
	}
	

	/**
	 * 读取全局变量
	 */
	private static void readDynamicSystemConfig() {
		try {
			Document doc = builder.build( new File( DYNAMIC_SYSTEM_CONFIG_FILENAME ) );
			Element root = doc.getRootElement();//根元素<root>
			
			ftpClearPeriod = Integer.parseInt( root.getChild( "ftpClearPeriod" ).getValue() );//FTP服务器文件清除超时时间,单位小时
			logger.info( "更新 ftpClearPeriod -> " + ftpClearPeriod );
			
			middleResultPathClearPeriod = Integer.parseInt( root.getChild( "middleResultPathClearPeriod" ).getValue() );//中间结果文件删除超时时间,单位小时
			logger.info( "更新 middleResultPathClearPeriod -> " + middleResultPathClearPeriod );
			
			ensembleCacheClearPeriod = Integer.parseInt( root.getChild( "ensembleCacheClearPeriod" ).getValue() );//集合预报分成员结果缓存时间,单位小时
			logger.info( "更新 ensembleCacheClearPeriod -> " + ensembleCacheClearPeriod );
			
			isMasterNode = Boolean.parseBoolean( root.getChild( "isMasterNode" ).getValue() );//是否是主节点
			logger.info( "更新 isMasterNode -> " + isMasterNode );
			
			ftpRecoverPeriod = Integer.parseInt( root.getChild("ftpRecoverPeriod").getValue() );//FTP目录恢复时间
			logger.info( "更新ftpRecoverPeriod -> " + ftpRecoverPeriod);
			
			decoderSwitch = Boolean.parseBoolean( root.getChild( "decoderSwitch" ).getValue() );//解码开关
			logger.info( "更新decoderSwitch -> " + decoderSwitch);
			
			writeSwitch = Boolean.parseBoolean( root.getChild( "writeSwitch" ).getValue() );//写入开关
			logger.info( "更新writeSwitch -> " + writeSwitch);
			
			instantWriteSwitch = Boolean.parseBoolean( root.getChild( "instantWriteSwitch" ).getValue() );//立即写入开关
			logger.info( "更新instantWriteSwitch -> " + instantWriteSwitch );
			
			threadWaitTime = Long.parseLong( root.getChild( "threadWaitTime" ).getValue() );//关闭系统时解码线程最长等待时间(单位:分钟)
			logger.info( "更新threadWaitTime -> " + threadWaitTime );
			
		} catch ( JDOMException e ) {
			logger.error( "读取全局配置文件" + DYNAMIC_SYSTEM_CONFIG_FILENAME + "错误!" );
		} catch ( IOException e ) {
			logger.error( "找不到全局配置文件" + DYNAMIC_SYSTEM_CONFIG_FILENAME );
		} catch ( Exception e ) {
			logger.error( "读取动态配置文件出现其他异常，使用系统默认值" );
		}
	}

	
	/**
	 * 判断字符串是否为空
	 * @return true是 false否
	 */
	private static boolean isEmpty( String s ) {
		return s == null || s.isEmpty();
	}


}

