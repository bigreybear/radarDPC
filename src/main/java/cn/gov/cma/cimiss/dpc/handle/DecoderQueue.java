package cn.gov.cma.cimiss.dpc.handle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gov.cma.cimiss.dpc.config.DPCConfig;

public final class DecoderQueue {
	//任务处理线程池
	private ExecutorService threadPool;//该线程池同时处理解码和写入
	private static Logger logger = LoggerFactory.getLogger( DecoderQueue.class );
	
	public DecoderQueue() {//单实例类构造函数
		threadPool = Executors.newFixedThreadPool( DPCConfig.decoderThreadMaxNum );//初始化线程池
	}

	public void push(Decoder decoder, Writer writer, String absoluteFileName ) {
		logger.debug( "文件任务 " + absoluteFileName + " 进入解码队列" );
		DecoderTask decoderTask = new DecoderTask( decoder, writer, absoluteFileName );//构造解码线程
		threadPool.execute( decoderTask );//线程执行开始
	}
	
	public void awaitTermination( long timeout, TimeUnit unit ) {
		try {
			Thread.sleep( 500 );//短暂等待,防止FTP已经接收,但未进入解码队列的任务丢失
			logger.info( "开始等待线程池中所有解码任务结束" );
			threadPool.shutdown();
			if ( threadPool.awaitTermination( timeout, unit ) ) {//true if this executor terminated and false if the timeout elapsed before termination
				logger.info( "线程池中所有解码任务已结束" );
			} else {
				logger.info( "指定时间  " + timeout + " 分钟内仍然存在未结束解码任务, 自动终止所有任务" );
			}
		} catch ( InterruptedException e ) {
			logger.error( "解码线程等待过程被外部中止" );
		}
	}
}

