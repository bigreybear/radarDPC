package cn.gov.cma.cimiss.dpc.handle;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gov.cma.cimiss.dpc.config.DPCConfig;
/***
 * 解码分发器
 * 负责将FTP捕获到的数据进行分发
 * @author zhangbo
 *
 */
public class DecoderDispatcher {
	//Log4j日志
	public static Logger logger = LoggerFactory.getLogger( DecoderDispatcher.class );
	public DecoderQueue decoderQueue = new DecoderQueue();//解码器线程队列
	private DecodeRoute decodeRoute = null;//解码器线程队列

	public DecoderDispatcher(DecodeRoute decodeRoute){
		this.decodeRoute = decodeRoute;
	}
	/**
	 * 解码事件分发器
	 * @param relativeFileName 解码路径
	 * @throws IOException 未找到源文件
	 */
	public void dispatch( String relativeFileName ) {
		if( DPCConfig.decoderSwitch == false ){
			logger.info( "解码器开关关闭," + relativeFileName + "不进行解码" );
			return;
		}

		try {
			Decoder decoder;//该familyName对应的解码器引用
			decoder = selectDecoder( relativeFileName );//动态加载解码器，运用反射
			logger.debug( "数据 " + relativeFileName + "找到解码器 " + decoder );
			Writer writer;//该familyName对应的解码器引用
			writer = selectWriter( relativeFileName );//动态加载解码器，运用反射
			logger.debug( "数据 " + relativeFileName + "找到解码器 " + decoder );
			
			String absoluteFileName = System.getProperty("user.dir") + "/" + DPCConfig.ftpFilePath + relativeFileName;//拼接绝对路径文件名
			decoderQueue.push( decoder, writer, absoluteFileName );//对解码任务进行分发，加入任务队列

		} catch ( Exception e ) {
			return;//已经向日志输出错误，直接返回
		}
		
	}

	public Decoder selectDecoder( String decoderClassNameString ) throws Exception
	{
		return decodeRoute.getDecoder(decoderClassNameString);
	}

	public Writer selectWriter(String decoderClassNameString ) throws Exception
	{
		return decodeRoute.getWriter(decoderClassNameString);
	}


	public DecoderQueue getDecoderQueue(){
		return this.decoderQueue;
	}

}

