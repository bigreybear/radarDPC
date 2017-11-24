package cn.gov.cma.cimiss.dpc.example;

import cn.gov.cma.cimiss.dpc.file.DPCFileCleaner;
import cn.gov.cma.cimiss.dpc.handle.Writer;
import cn.gov.cma.cimiss.dpc.util.StringUtil;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.ftpserver.command.impl.STRU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

/**
 * Created by dyf on 20/12/2016.
 */
public class MyWriter extends Writer {
    private static Logger logger = LoggerFactory.getLogger( MyWriter.class );
    private static int size = 1024*1024;
    public static Cluster cluster;
    public static Session session;
    //init
    {
        try {
            cluster = Cluster.builder()
                    .addContactPoint("127.0.0.1")
                    .build();
            session = cluster.connect();

            ResultSet rs = session.execute("select release_version from system.local");
            Row row = rs.one();
            logger.info("CASSANDRA_VERSION:");
            System.out.println(row.getString("release_version"));

            rs = session.execute("CREATE KEYSPACE IF NOT EXISTS file with REPLICATION = " +
                    "{'class': 'SimpleStrategy', 'replication_factor': 1};");
            rs = session.execute("CREATE TABLE IF NOT EXISTS file.test (" +
                    "filename text," +
                    "content blob," +
                    "PRIMARY KEY(filename));");
            logger.info("Cassandra initiated.");
            System.out.println(rs);

        } finally {
        }
    }

    public void wirteByFileName(String absName){
        String dataPath = StringUtil.getDataPathFromFtpPath(absName);
        String realDataPath = "/" + StringUtil.normalizePath(dataPath);

        int index = realDataPath.lastIndexOf("/");
        String fileName = realDataPath.substring(index + 1);
        String path =  realDataPath.substring(0, index);
        if (path == ""){
            path="/";
        }
        ByteBuffer fileContent;

        try {
            fileContent = MyWriter.getFileByteArray(absName);
            fileContent.position(0);
        }catch (IOException x){
            System.out.println(x.getMessage());
            fileContent = null;
        }

        String storeName = realDataPath.substring(1);
        session.execute("insert into file.test(filename, content) values (?, ?)", storeName, fileContent);
        /*
        byte[] byteArray = fileContent.array();
        int totalSize = byteArray.length;
        int splitNumber = totalSize / size + (totalSize % size == 0 ? 0 : 1);
        for(int partNunber = 1; partNunber <= splitNumber; partNunber++){
            int start = (partNunber - 1) * size;
            int end =  partNunber * size ;
            if(end > totalSize )
                end = totalSize;
            ByteBuffer bf = ByteBuffer.wrap(byteArray, start, end - start);
            session.execute( "insert into file.file(path, filename, content, part) values (?,?,?,?)",path, fileName, bf, partNunber);
        }

        String[] dirs = realDataPath.split("/");
        String rootDir = "/";
        String filestatus = "D";
        for(int i = 1; i< dirs.length; i++){
            if(i == dirs.length - 1){
                filestatus = splitNumber  + " " + String.valueOf(totalSize);
            }

            session.execute( "insert into file.fileview(path, filename, filestatus) values (?,?,?)",rootDir, dirs[i], filestatus);

            if(!rootDir.endsWith("/"))
                rootDir += "/";
            rootDir += dirs[i];
        }
        */
    }

    public void write(Map<String, Object> context){
        String absoluteFileName = (String)context.get("uploadFile");
        String dataPath = StringUtil.getDataPathFromFtpPath(absoluteFileName);
        String realDataPath = "/" + StringUtil.normalizePath(dataPath);

        int index = realDataPath.lastIndexOf("/");
        String fileName = realDataPath.substring(index + 1);
        String path =  realDataPath.substring(0, index);
        if (path == ""){
        	path="/";
        }
        ByteBuffer fileContent;
        try {
            fileContent = MyWriter.getFileByteArray(absoluteFileName);
            fileContent.position(0);
        }catch (IOException e){
            System.out.println(e.getMessage());
            fileContent = null;
        }
        byte[] byteArray = fileContent.array();
        int totalSize = byteArray.length;
        int splitNumber = totalSize / size + (totalSize % size == 0 ? 0 : 1);
        for(int partNunber = 1; partNunber <= splitNumber; partNunber++){
            int start = (partNunber - 1) * size;
            int end =  partNunber * size ;
            if(end > totalSize )
                end = totalSize;
            ByteBuffer bf = ByteBuffer.wrap(byteArray, start, end - start);
            session.execute( "insert into file.file(path, filename, content, part) values (?,?,?,?)",path, fileName, bf, partNunber);
        }
        String[] dirs = realDataPath.split("/");
        String rootDir = "/";
        String filestatus = "D";
        for(int i = 1; i< dirs.length; i++){
            if(i == dirs.length - 1){
                filestatus = splitNumber  + " " + String.valueOf(totalSize);
            }

            session.execute( "insert into file.fileview(path, filename, filestatus) values (?,?,?)",rootDir, dirs[i], filestatus);

            if(!rootDir.endsWith("/"))
                rootDir += "/";
            rootDir += dirs[i];
        }
    }

    public static ByteBuffer getFileByteArray(String filename) throws IOException {

        File f = new File(filename);
        if (!f.exists()) {
            throw new FileNotFoundException(filename);
        }

        FileChannel channel = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(f);
            channel = fs.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            while ((channel.read(byteBuffer)) > 0) {}
            return byteBuffer;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}