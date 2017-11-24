## DPC使用说明
### 一. DPC简介
*    DPC是一个基于FTP Server的实时数据处理系统，所处理的数据为通过FTP协议传输的实时数据。DPC集成了Apache FtpServer。用户可通过正则定义对不同ftp的文件的处理方式，处理的方式分为两步：

    1. 解码(Decode)
完成对数据的处理

    2. 写入(Write)
将处理后的数据进行写入

   **因此，数据的处理流程如下：FTP接收->路由->指定解码器解码->指定写入器写入**
   
<p><img src="DPC/blob/master/docs/images/flow-chart1.jpg" width=100% alt="img"></p>
### 二. DPC使用说明
*    DPC框架已经完成了绝大多数工作。用户只需完成路由定义和Decoder和Writer的实现即可。

	**1. 定义FTP Server配置信息(StaticSystemConfig.xml)**
	
	```
	<?xml version="1.0" encoding="UTF-8"?>
	<!-- 全局静态配置 -->
	<root>
		<!-- FTP服务器端口 -->
	    <ftpPort>2221</ftpPort>
	    <!-- FTP服务器用户名 -->
	    <ftpUserName>dpc</ftpUserName>
	    <!-- FTP服务器密码 -->
	    <ftpUserPass>dpcsecret</ftpUserPass>
	    <!-- FTP文件接收相对路径 -->
	    <ftpFilePath>ftp</ftpFilePath>
	    <!-- 解码器线程数 -->
	    <decoderThreadMaxNum>30</decoderThreadMaxNum>
	    <!-- 本地文件定时删除器执行周期,单位小时 -->
	    <cleanerPeriod>12</cleanerPeriod>
	    <!-- 交互系统通信端口 -->
	    <socketPort>8088</socketPort>
	</root>
	```
	
	**2. 定义实现Decoder类**
	继承系统的Decoder类，实现decode方法
	
	```xml
	public class MyDecoder extends Decoder {
	    @Override
	    public void decode(String absoluteFileName, Map<String, Object> context) throws Exception {
	    	//write down your code    
	    }
	}	
	```
	
	**3. 定义实现Writer类**
	继承系统的Decoder类，实现decode方法
	
	```java
	public class MyWriter extends Writer {
	    @Override
	    public void write(Map<String, Object> context){
	        //write down your code    
	    }
	}
	```
	**4. 定义实现数据路由**
	继承系统的Decoder类，实现decode方法
	
	```java
	DecodeRoute decodeRoute = new DecodeRoute();
	//addRoute支撑正则，对于所有类型的数据，采用MyDecoder解码，采用MyWriter写入
   decodeRoute.addRoute(".*", MyDecoder.class, MyWriter.class);
	```
	
	**5. 启动服务**
		
	```java
	//配置读取
	DPCConfig.init();
	
	DecoderDispatcher decoderDispatcher = new DecoderDispatcher(decodeRoute);
	
	//FTP服务器模块启动
	DPCFtpServerFactory dpcFtpServerFactory = new DPCFtpServerFactory();
	DPCFtpServer dpcFtpServer = dpcFtpServerFactory.createDPCFtpServer(decoderDispatcher);
	dpcFtpServer.startServer();
	```
	
### 三. DPC辅助模块
*    DPC框架还包括其他模块  
	**1. 文件定时清除**
	
	对于FTP目录下的数据，如果不清楚，则会不断增加。DPC框架已经集成了对FTP目录下的自动清理。可在配置文件中指定过期删除时间（cleanerPeriod）。
	
	```java
	//临时数据删除模块启动
	DPCFileCleaner dpcFileCleaner = DPCFileCleaner.getInstance();
	dpcFileCleaner.start();
	```
	
	**2. 通信模块**
	DPC框架继承了一个控制台交互系统启动。可在配置文件中指定交互系统通信端口（socketPort）。

	
	```java
	//控制台交互系统启动
	DPCNodeMessenger dpcNodeMessenger = DPCNodeMessenger.getInstance();
	DPCInteractionFactory dpcInteractionFactory = new DPCInteractionFactory(dpcFtpServer, dpcNodeMessenger, dpcFileCleaner);
	DPCInteraction dpcInteraction = dpcInteractionFactory.createDPCInteraction(decoderDispatcher);
	dpcInteraction.start();
	```

