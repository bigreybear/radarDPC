package cn.gov.cma.cimiss.dpc.ftpserver;

import java.io.IOException;

import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gov.cma.cimiss.dpc.handle.DecoderDispatcher;

public class DPCFtplet extends DefaultFtplet {
	
	//Log4j日志
	public static Logger logger = LoggerFactory.getLogger(DPCFtplet.class);
	private DecoderDispatcher decoderDispatcher = null;

	public DPCFtplet(DecoderDispatcher decoderDispatcher){
		this.decoderDispatcher = decoderDispatcher;
	}

	@Override
	public FtpletResult onRenameEnd(FtpSession session, FtpRequest request)
			throws FtpException, IOException {
		logger.debug( "FtpLet onRenameEnd: " + request.getCommand() + " Argument: " + request.getArgument() );
		return onUploadEnd(session, request);
	}
	
	public FtpletResult onUploadEnd( FtpSession session, FtpRequest request )
			throws FtpException, IOException {
		
		FtpletResult ftpletResult = super.onUploadEnd( session, request );
		String dir = session.getFileSystemView().getWorkingDirectory().getAbsolutePath();//得到ftp目录下一级目录开始的绝对路径
		dir = dir.replaceAll( "\\\\+", "/" );//将一个或多个\替换为一个/
		dir = dir.replaceAll( "/{2,}", "/" );//将2个或2个以上的/替换为一个/
		if ( !dir.endsWith( "/" ) )//如果结尾不是"/"，则拼接一个/,执行至此只可能有一个结尾/
			dir += "/";
		logger.debug( "FTP 当前路径 dir " + dir );
		String relativeFileName = dir + request.getArgument();//拼接完整的相对路径文件名，相对于FTP根目录
		relativeFileName = relativeFileName.replaceAll( "\\\\+", "/" );//将一个或多个\替换为一个/
		relativeFileName = relativeFileName.replaceAll( "/{2,}", "/" );//将2个或2个以上的/替换为一个/
		if( !relativeFileName.startsWith( "/" ) )
			relativeFileName = "/" + relativeFileName;
		logger.info( "FTP接收文件  " + relativeFileName  + " 来自IP: " + session.getClientAddress().getAddress().getHostAddress() );

		decoderDispatcher.dispatch( relativeFileName );//解码分发器进行解码
		return ftpletResult;
	}
	@Override
	public FtpletResult afterCommand( FtpSession session, FtpRequest request,
			FtpReply reply ) throws FtpException, IOException {
		logger.debug( "FtpLet afterCommand: " + request.getCommand() + " Ftplet argument: " + request.getArgument() );
		FtpletResult  ftpletResult = super.afterCommand( session, request, reply );
		String command = request.getCommand().toUpperCase();
		if ( "CWD".equals( command ) ) {
			session.setAttribute( "DIR", request.getArgument() );
		}
		return ftpletResult;
	}
}//end class DPCFtplet

