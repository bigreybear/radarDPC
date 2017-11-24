package cn.gov.cma.cimiss.dpc.ftpserver;

import java.util.ArrayList;
import java.util.List;

import cn.gov.cma.cimiss.dpc.handle.DecoderDispatcher;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.UserFactory;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import cn.gov.cma.cimiss.dpc.config.DPCConfig;

/***
 * FTP服务器工厂
 * @author zhangbo
 *
 */
public class DPCFtpServerFactory {
	//FTP最大登陆数
	private int maxLogins = 100;
	//是否允许匿名登陆
	private boolean anonymousLoginEnabled = false;
	//用户信息保存路径
	private String userPropertiesPath = "user.properties";
	//用户列表
	private List<User> userList = new ArrayList<User>();
	//FTP事件处理器
	//FTP监听端口
	private int ftpPort = 2221;


	public DPCFtpServerFactory() {
		
	}
	public DPCFtpServer createDPCFtpServer(DecoderDispatcher decoderDispatcher ){
		//从Config中读取通用配置数据
		if(DPCConfig.ftpPort != null){
			this.ftpPort = DPCConfig.ftpPort;
		}
		if(DPCConfig.ftpUserName != null && DPCConfig.ftpUserPass != null && DPCConfig.ftpFilePath != null){
			UserFactory userFactory = new UserFactory();
			userFactory.setName(DPCConfig.ftpUserName);
			userFactory.setPassword(DPCConfig.ftpUserPass);
			userFactory.setHomeDirectory(DPCConfig.ftpFilePath);
			List<Authority> authorities = new ArrayList<Authority>();
			authorities.add(new WritePermission());
			userFactory.setAuthorities(authorities);
			userList.add(userFactory.createUser());
		}
		DPCFtpServer dpcFtpServer = new DPCFtpServer(maxLogins, anonymousLoginEnabled, 
				userPropertiesPath, userList, ftpPort, decoderDispatcher);
		return dpcFtpServer;
	}
	
	//GET & SET
	public int getMaxLogins() {
		return maxLogins;
	}
	public void setMaxLogins(int maxLogins) {
		this.maxLogins = maxLogins;
	}
	public boolean isAnonymousLoginEnabled() {
		return anonymousLoginEnabled;
	}
	public void setAnonymousLoginEnabled(boolean anonymousLoginEnabled) {
		this.anonymousLoginEnabled = anonymousLoginEnabled;
	}
	public String getUserPropertiesPath() {
		return userPropertiesPath;
	}
	public void setUserPropertiesPath(String userPropertiesPath) {
		this.userPropertiesPath = userPropertiesPath;
	}
	public List<User> getUserList() {
		return userList;
	}
	public void setUserList(List<User> userList) {
		this.userList = userList;
	}
}

