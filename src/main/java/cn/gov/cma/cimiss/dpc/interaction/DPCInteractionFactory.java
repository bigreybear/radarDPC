package cn.gov.cma.cimiss.dpc.interaction;

import cn.gov.cma.cimiss.dpc.communication.DPCNodeMessenger;
import cn.gov.cma.cimiss.dpc.handle.DecoderDispatcher;
import cn.gov.cma.cimiss.dpc.file.DPCFileCleaner;
import cn.gov.cma.cimiss.dpc.ftpserver.DPCFtpServer;

public class DPCInteractionFactory {
	//FTP服务器实例
	private DPCFtpServer dpcFtpServer = null;
	//通信模块实例
	private DPCNodeMessenger dpcNodeMessenger = null;
	//文件模块实例
	private DPCFileCleaner dpcFileCleaner = null;
	
	public DPCInteractionFactory() {
	}
	public DPCInteractionFactory(DPCFtpServer dpcFtpServer, DPCNodeMessenger dpcNodeMessenger
			, DPCFileCleaner dpcFileCleaner){
		this.dpcFtpServer = dpcFtpServer;
		this.dpcNodeMessenger = dpcNodeMessenger;
		this.dpcFileCleaner = dpcFileCleaner;
	}
	public DPCInteraction createDPCInteraction(DecoderDispatcher decoderDispatcher){
		return new DPCInteraction(this.dpcFtpServer, this.dpcNodeMessenger, 
				this.dpcFileCleaner, decoderDispatcher);
	}
	public DPCFtpServer getDpcFtpServer() {
		return dpcFtpServer;
	}
	public void setDpcFtpServer(DPCFtpServer dpcFtpServer) {
		this.dpcFtpServer = dpcFtpServer;
	}
	public DPCNodeMessenger getDpcNodeMessenger() {
		return dpcNodeMessenger;
	}
	public void setDpcNodeMessenger(DPCNodeMessenger dpcNodeMessenger) {
		this.dpcNodeMessenger = dpcNodeMessenger;
	}
	public DPCFileCleaner getDpcFileCleaner() {
		return dpcFileCleaner;
	}
	public void setDpcFileCleaner(DPCFileCleaner dpcFileCleaner) {
		this.dpcFileCleaner = dpcFileCleaner;
	}
	
}

