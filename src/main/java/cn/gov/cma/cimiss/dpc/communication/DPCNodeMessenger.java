package cn.gov.cma.cimiss.dpc.communication;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gov.cma.cimiss.dpc.config.DPCConfig;
import cn.gov.cma.cimiss.dpc.file.DPCFileRecover;

/***
 * 系统通信模块
 * 主节点负响应心跳、从节点负责接收心跳
 * @author zhangbo
 *
 */
public class DPCNodeMessenger extends Thread{
	private static Logger logger = LoggerFactory.getLogger(DPCNodeMessenger.class);
	private static DPCNodeMessenger dpcNodeMessenger = null;
	private DPCNodeMessenger() {
	}
	public static DPCNodeMessenger getInstance(){
		if(dpcNodeMessenger == null)
			dpcNodeMessenger = new DPCNodeMessenger();
		return dpcNodeMessenger;
	}
	@Override
	public void run() {
		super.run();
		logger.info("通信系统启动 " + new Date());
		if(DPCConfig.isMasterNode){
			startMessageReceiver();
		}else{
			startMessageSender();
		}
	}
	
	/***
	 * 从节点定时通过Socket发送心跳数据
	 */
	private void startMessageSender() {
		//TODO 如果发送失败
		//启动恢复
		//DPCFileRecover recover = new DPCFileRecover(null, null);
	}
	/***
	 * 主节点响应Socket心跳数据
	 */
	private void startMessageReceiver() {
		
	}
	
	/***
	 * 关闭通信系统服务
	 */
	public void close(){
		
	}
}

