package cn.gov.cma.cimiss.dpc.example;

import cn.gov.cma.cimiss.dpc.communication.DPCNodeMessenger;
import cn.gov.cma.cimiss.dpc.config.DPCConfig;
import cn.gov.cma.cimiss.dpc.file.DPCFileCleaner;
import cn.gov.cma.cimiss.dpc.ftpserver.DPCFtpServer;
import cn.gov.cma.cimiss.dpc.ftpserver.DPCFtpServerFactory;
import cn.gov.cma.cimiss.dpc.handle.DecodeRoute;
import cn.gov.cma.cimiss.dpc.handle.DecoderDispatcher;
import cn.gov.cma.cimiss.dpc.interaction.DPCInteraction;
import cn.gov.cma.cimiss.dpc.interaction.DPCInteractionFactory;

/**
 * Created by dyf on 20/06/2017.
 */
public class Server {
    public static void main(String[] args){
        //配置读取
        DPCConfig.init();

        DecodeRoute decodeRoute = new DecodeRoute();
        decodeRoute.addRoute(".*", MyDecoder.class, MyWriter.class);
        DecoderDispatcher decoderDispatcher = new DecoderDispatcher(decodeRoute);

        //FTP服务器模块启动
        DPCFtpServerFactory dpcFtpServerFactory = new DPCFtpServerFactory();
        DPCFtpServer dpcFtpServer = dpcFtpServerFactory.createDPCFtpServer(decoderDispatcher);

        dpcFtpServer.startServer();

        //临时数据删除模块启动
        DPCFileCleaner dpcFileCleaner = DPCFileCleaner.getInstance();
        dpcFileCleaner.start();

        //控制台交互系统启动
//        DPCNodeMessenger dpcNodeMessenger = DPCNodeMessenger.getInstance();
//        DPCInteractionFactory dpcInteractionFactory = new DPCInteractionFactory(dpcFtpServer, dpcNodeMessenger, dpcFileCleaner);
//        DPCInteraction dpcInteraction = dpcInteractionFactory.createDPCInteraction(decoderDispatcher);
//        dpcInteraction.start();
    }
}
