package cn.gov.cma.cimiss.dpc.test;

import cn.gov.cma.cimiss.dpc.communication.DPCNodeMessenger;
import cn.gov.cma.cimiss.dpc.config.DPCConfig;
import cn.gov.cma.cimiss.dpc.handle.DecodeRoute;
import cn.gov.cma.cimiss.dpc.handle.DecoderDispatcher;
import cn.gov.cma.cimiss.dpc.file.DPCFileCleaner;
import cn.gov.cma.cimiss.dpc.ftpserver.DPCFtpServer;
import cn.gov.cma.cimiss.dpc.ftpserver.DPCFtpServerFactory;
import cn.gov.cma.cimiss.dpc.interaction.DPCInteraction;
import cn.gov.cma.cimiss.dpc.interaction.DPCInteractionFactory;
import cn.gov.cma.cimiss.dpc.handle.GeneralWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * 导入系统入口类
 * @author dyf
 *
 */


public class DPCMain {

	public static void main( String args[] )
	{
		//配置读取
		DPCConfig.init();

        DecodeRoute decodeRoute = new DecodeRoute();
        decodeRoute.addRoute(".*", MyDecoder.class, GeneralWriter.class);
		DecoderDispatcher decoderDispatcher = new DecoderDispatcher(decodeRoute);
		
		//FTP服务器模块启动
		DPCFtpServerFactory dpcFtpServerFactory = new DPCFtpServerFactory();
		DPCFtpServer dpcFtpServer = dpcFtpServerFactory.createDPCFtpServer(decoderDispatcher);

		dpcFtpServer.startServer();
		
		//临时数据删除模块启动
		DPCFileCleaner dpcFileCleaner = DPCFileCleaner.getInstance();
		dpcFileCleaner.start();
		
		//控制台交互系统启动
		DPCNodeMessenger dpcNodeMessenger = DPCNodeMessenger.getInstance();
		DPCInteractionFactory dpcInteractionFactory = new DPCInteractionFactory(dpcFtpServer, dpcNodeMessenger, dpcFileCleaner);
		DPCInteraction dpcInteraction = dpcInteractionFactory.createDPCInteraction(decoderDispatcher);
		dpcInteraction.start();
	}//end method test
	
}//end class DPCMain

