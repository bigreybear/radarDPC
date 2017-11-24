package cn.gov.cma.cimiss.dpc.interaction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cn.gov.cma.cimiss.dpc.handle.DecoderDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gov.cma.cimiss.dpc.communication.DPCNodeMessenger;
import cn.gov.cma.cimiss.dpc.config.DPCConfig;
import cn.gov.cma.cimiss.dpc.file.DPCFileCleaner;
import cn.gov.cma.cimiss.dpc.file.DPCFileRecover;
import cn.gov.cma.cimiss.dpc.ftpserver.DPCFtpServer;

/***
 * 控制台交互模块
 * @author zhangbo
 *
 */
public class DPCInteraction extends Thread{
	private static Logger logger = LoggerFactory.getLogger(DPCInteraction.class);
	//FTP服务器实例
	private DPCFtpServer dpcFtpServer = null;
	//通信模块实例
	private DPCNodeMessenger dpcNodeMessenger = null;
	//文件模块实例
	private DPCFileCleaner dpcFileCleaner = null;
	//Socket实例
	private ServerSocket serverSocket = null;
	//同步锁，保证同时只有一个关闭任务
	private  Object syncObj = new Object();

	private DecoderDispatcher decoderDispatcher = null;
	
	public DPCInteraction(DPCFtpServer dpcFtpServer,
			DPCNodeMessenger dpcNodeMessenger, DPCFileCleaner dpcFileCleaner,DecoderDispatcher decoderDispatcher) {
		this.dpcFtpServer = dpcFtpServer;
		this.dpcNodeMessenger = dpcNodeMessenger;
		this.dpcFileCleaner = dpcFileCleaner;
		this.decoderDispatcher = decoderDispatcher;
	}
	@Override
	public void run() {
		super.run();
		logger.info("交互系统启动 " + new Date());
		
		Runtime.getRuntime().addShutdownHook(new RuntimeHook());
		try {
			serverSocket = new ServerSocket(DPCConfig.socketPort);
		} catch (IOException e) {
			logger.error("启动交互系统失败，请检查" + DPCConfig.socketPort + "端口");
			return;
		}
		boolean running = true;
		while(running){
			try {
				Socket socket = serverSocket.accept();
				logger.info("交互系统获得连接,来自" + socket.getInetAddress());
				new InteractionServer(socket).start();
			} catch (Exception e) {
				running = false;
				logger.error("交互系统Socket连接异常！");
			}
		}
	}
	class RuntimeHook extends Thread{
		@Override
		public void run() {
			synchronized(syncObj) {
				logger.info("开始关闭系统");
				//关闭FTP服务器
				dpcFtpServer.close();
				//停止文件处理
				dpcFileCleaner.close();
				//停止通信系统
				dpcNodeMessenger.close();
				//等待任务队列结束
				decoderDispatcher.getDecoderQueue().awaitTermination(DPCConfig.threadWaitTime, TimeUnit.MINUTES);
				logger.info("系统关闭");
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	class InteractionServer extends Thread{
		private Socket socket;
		BufferedReader br = null;  
        PrintWriter pw = null;  
		public InteractionServer(Socket socket) {
			this.socket = socket;
			try{
		        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
	            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
	        }catch (IOException e){
	        	logger.error("获得Socket输入输出流异常");
	        	return;
	        }
		}
		@Override
		public void run() {
			while (true){
				try {
					String commond = br.readLine();
					System.out.println("响应来自 " + socket.getInetAddress() + "的请求 :" + commond);
					String argv[] = commond.split(" ");
					//获得帮助
					if(commond.startsWith("-help")){
						printHelpInfo();
					}
					//重新载入配置文件
					else if(commond.startsWith("-reload")){
						doReload(argv);
					}
					//系统关闭 （FTP停止、等待队列处理完成、写日志）
					else if(commond.startsWith("-stop")){
						doStop(argv);
						break;
					}
					//手动数据恢复
					else if(commond.startsWith("-recover")){
						doRecover(argv);
					}
					//输出集群状态
					else if(commond.startsWith("-status")){
						printStatus(argv);
					}else if(commond.startsWith("-exit")){
						break;
					}else {
						printHelpInfo();
					}
				}catch (Exception e){
					logger.error("读入输入流异常, 或用户直接退出, 系统退出连接");
					break;
				}
			}
			try {
	            br.close();  
	            pw.close();  
	            socket.close();  
			}catch (Exception e){
				logger.error("Socket接口关闭失败");
			}
		}
		/***
		 * 输出集群状态
		 * @param argv
		 */
		private void printStatus(String[] argv) {
			//TODO
			pw.println("获得集群状态！");  
            pw.flush();  
		}
		/***
		 * 手动进行恢复
		 * @param argv [1] 开始时间  开始时间为空则恢复默认时间数据
		 * @param argv [2] 结束时间 结束时间为空则为当前时间数据
		 */
		private void doRecover(String[] argv) {
			//读取参数转化为时间
			String start = argv.length >=2 ? argv[1] : null;
			String end = argv.length >=3 ? argv[2] : null;
			DPCFileRecover dpcFileRecover = new DPCFileRecover(start, end, decoderDispatcher);
			dpcFileRecover.start();
			pw.println("数据恢复开始！");  
            pw.flush();  
		}
		/***
		 * 关闭集群
		 * @param argv
		 */
		private void doStop(String[] argv) {
			new RuntimeHook().run();
			pw.println("集群关闭!");  
            pw.flush();  
		}
		/***
		 * 重新加载配置文件
		 * @param argv
		 */
		private void doReload(String[] argv) {
			DPCConfig.reload();
			pw.println("数据重新加载完成!");  
            pw.flush(); 
		}
		/***
		 * 输出帮助信息
		 */
		private void printHelpInfo() {
			pw.println("获取帮助信息");  
            pw.flush(); 
		}
	}
}

