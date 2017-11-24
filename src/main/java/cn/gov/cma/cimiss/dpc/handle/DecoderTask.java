package cn.gov.cma.cimiss.dpc.handle;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * 具体执行解码任务的线程
 * @author zhangbo
 *
 */
public class DecoderTask extends Thread{
	private Decoder decoder;//该任务具体的解码器类型
	private Writer writer;//该任务具体的解码器类型
	private String absoluteFileName;//该任务需要解码的绝对文件名

	private static Logger logger = LoggerFactory.getLogger( DecoderTask.class );
	public DecoderTask( Decoder decoder,Writer writer, String absoluteFileName ) {
		this.decoder = decoder;
		this.writer = writer;
		this.absoluteFileName = absoluteFileName;
	}

	@Override
	public void run() {
		super.run();
		long startTime = System.currentTimeMillis();//解码开始时间
		logger.info( "任务 " + absoluteFileName + " 开始解码  " + new Date() );
		Map<String, Object> context = new HashMap<String, Object>();
		try {
			decoder.decode( absoluteFileName, context);//调用具体解码器开始解码
            long endTime = System.currentTimeMillis();
			logger.info( "任务 " + absoluteFileName + " 结束解码  " + "用时 " + ( endTime - startTime ) + " ms" );
			startTime = System.currentTimeMillis();//写入开始时间
			writer.write(context); //解码完成，将解码后的一系列结果写入存储
			endTime = System.currentTimeMillis();//写入结束时间
			logger.info( "任务 " + absoluteFileName + " 完成写入  " + "用时 " + ( endTime - startTime ) + " ms" );
		} catch ( Exception e ) {
			logger.error( "任务 " + absoluteFileName + " 解码异常  " + e.getMessage() );
		}finally {
			context.clear();
		}
	}
}
