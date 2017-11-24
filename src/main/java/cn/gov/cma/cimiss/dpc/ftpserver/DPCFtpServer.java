package cn.gov.cma.cimiss.dpc.ftpserver;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.gov.cma.cimiss.dpc.handle.DecoderDispatcher;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.listener.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * FTP服务器类
 * 建议有工厂类配置相关参数
 * @author zhangbo
 *
 */
public class DPCFtpServer {
	
	//FTP最大登陆数
	private int maxLogins;
	//是否允许匿名登陆
	private boolean anonymousLoginEnabled;
	//用户信息保存路径
	private String userPropertiesPath;
	//用户列表
	private List<User> userList;
	//FTP事件处理器
	private Ftplet ftplet;
	//FTPServier
	private FtpServer server;
	//FTPLet 名称
	private String ftpLetName = "DPCFtplet";
	//FTP监听端口
	private int ftpPort = 2221;
	//Log4j日志
	public Logger logger = LoggerFactory.getLogger(DPCFtpServer.class);
	private DecoderDispatcher decoderDispatcher = null;

	public DPCFtpServer(int maxLogins, boolean anonymousLoginEnabled,
			String userPropertiesPath, List<User> userList, int ftpPort,DecoderDispatcher decoderDispatcher) {
		this.maxLogins = maxLogins;
		this.anonymousLoginEnabled = anonymousLoginEnabled;
		this.userPropertiesPath = userPropertiesPath;
		this.userList = userList;
		this.ftpPort = ftpPort;
		this.decoderDispatcher = decoderDispatcher;
		initConfig();
	}
	
	/***
	 * 初始化FTP配置
	 */
	private void initConfig() {
		/***
		 * 服务器配置
		 * 包括失败重试、最大登陆人数等信息
		 * 详见：
		 * http://grepcode.com/file/repo1.maven.org/maven2/org.apache.ftpserver/ftpserver-core/1.0.6/org/apache/ftpserver/ConnectionConfigFactory.java#ConnectionConfigFactory
		 */
		ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory(); 
		connectionConfigFactory.setMaxLogins( maxLogins );
		connectionConfigFactory.setAnonymousLoginEnabled( anonymousLoginEnabled );
		ConnectionConfig connectionConfig = connectionConfigFactory.createConnectionConfig();
		
		/***
		 * 用户相关配置
		 * 包括用户名称、密码、用户权限等配置
		 * 详见：http://grepcode.com/file/repo1.maven.org/maven2/org.apache.ftpserver/ftpserver-core/1.0.6/org/apache/ftpserver/usermanager/PropertiesUserManagerFactory.java#PropertiesUserManagerFactory
		 */
		File file = new File( userPropertiesPath );
		if( !file.exists() )
			try {
				file.createNewFile();
			} catch ( IOException exception ) {
				logger.error("创建用户属性文件失败 " + file.getAbsolutePath());
				exception.printStackTrace();
			}
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		//保存用户信息的本地文件
		userManagerFactory.setFile( file );
		
		UserManager userManager = userManagerFactory.createUserManager();
		//将userList中的用户保存
		for (User user : userList){
			try {
				userManager.save(user);
			} catch (FtpException e) {
				logger.error("保存用户失败");
			}
		}
		/***
		 * 添加ftplets
		 * 对FTP事件进行捕获并处理
		 * 详见：http://grepcode.com/file/repo1.maven.org/maven2/org.apache.ftpserver/ftplet-api/1.0.6/org/apache/ftpserver/ftplet/DefaultFtplet.java#DefaultFtplet
		 */
		Map< String, Ftplet > ftplets = new HashMap< String, Ftplet >();
		ftplet = new DPCFtplet(decoderDispatcher);
		ftplets.put( ftpLetName , ftplet );
		FtpServerFactory serverFactory = new FtpServerFactory();
		serverFactory.setUserManager( userManager );
		serverFactory.setConnectionConfig( connectionConfig );
		serverFactory.setFtplets( ftplets );
		
		/***
		 * 设置ftp的监听端口
		 */
		ListenerFactory factory = new ListenerFactory();
		factory.setPort(ftpPort);
		serverFactory.addListener("default", factory.createListener());
		server = serverFactory.createServer();
	}
	/***
	 * 启动FTPServer服务
	 */
	public void startServer() {
		try {
			logger.info("启动FTP服务器 " + new Date());
			server.start();
		} catch ( FtpException exception ) {
			exception.printStackTrace();
		}
	}
	/***
	 * 停止FTPServer服务
	 */
	public void close() {
		logger.info( "关闭FTP服务器,停止接收新数据,已接收数据继续解码至完成写入" + new Date() );
		server.stop();
	}
}

