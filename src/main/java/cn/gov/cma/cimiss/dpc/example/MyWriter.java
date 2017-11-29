package cn.gov.cma.cimiss.dpc.example;

import cn.gov.cma.cimiss.dpc.handle.Writer;
import cn.gov.cma.cimiss.dpc.outlet.Zipper;
import cn.gov.cma.cimiss.dpc.util.StringUtil;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
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
            rs = session.execute("CREATE KEYSPACE IF NOT EXISTS radarDPC with REPLICATION = " +
                    "{'class': 'SimpleStrategy', 'replication_factor': 1};");

            rs = session.execute("CREATE TABLE IF NOT EXISTS file.test (" +
                    "filename text," +
                    "content blob," +
                    "PRIMARY KEY(filename));");
            rs = session.execute("CREATE TABLE IF NOT EXISTS radarDPC.cd (" +
                    "station text," +
                    "date int," +
                    "data blob," +
                    "PRIMARY KEY(station, date));");
            logger.info("Cassandra initiated.");
            System.out.println(rs);

        } finally {
        }
    }

    public static void main(String[] args) {
        Zipper ziper = new Zipper();
//        ziper.zipBatch("slimTestData/");
        MyWriter mw = new MyWriter();
        mw.writeByRadarUnit();
    }

    public boolean writeByRadarUnit(){
        return writeByRadarUnit("");
    }

    public boolean writeByRadarUnit(String dir){
        //Argument dir is the directory of mix files which from different stations and dates. Not recursively.
        if(dir.contentEquals("")){
            dir = "slimTestData/";
        }
        Zipper zip = new Zipper();
        boolean isFinished = false;
        File basedir = new File(dir);
        if(!basedir.isDirectory()){
            logger.info("BASEDIR IS NOT A DIRECTORY.");
            return false;
        }

        File remainFiles[] = basedir.listFiles();
        StringBuffer markString = new StringBuffer("");

        //initiate markString
        markString.setLength(remainFiles.length);
        for(int i = 0; i<remainFiles.length; i++){
            markString.setCharAt(i, '0');
        }

        if(remainFiles.length==0){
            isFinished = true;
            return isFinished;
        }

        String station = null;
        int date = 0;
        ArrayList<File> filtered = new ArrayList<File>();

        logger.info("COMPACTION BEGIN.");
        while(markString.indexOf("0") != -1){
            filtered.clear();
            station = StringUtil.getStationFromRadarData(remainFiles[markString.indexOf("0")].getName());
            date = Integer.parseInt(StringUtil.getDateFromRadarData(remainFiles[markString.indexOf("0")].getName()));

            String outputPath = "verify/" + station + "_" + Integer.toString(date) + ".zip";

            for(int i = 0; i < remainFiles.length; i++){
                if( StringUtil.getStationFromRadarData(remainFiles[i].getName()).contentEquals(station) &&
                        Integer.parseInt(StringUtil.getDateFromRadarData(remainFiles[i].getName())) == date )
                {
                    filtered.add(remainFiles[i]);
                    markString.setCharAt(i, '1');
                }
            }
            zip.filterdZip(dir, filtered, outputPath);
            this.writeByFileName(outputPath);
        }
        logger.info("COMPACTION FINISHED.");

        return  isFinished;
    }

    public void writeByFileName(String absName){

        int index = absName.lastIndexOf("/");
        String fileName = absName.substring(index + 1);
        String path =  absName.substring(0, index);
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

        String storeName = absName.substring(1);
        writeCompactBytes(StringUtil.getStationFromRadarData(fileName),
                Integer.parseInt(StringUtil.getDateFromRadarData(fileName)), fileContent);
//        session.execute("insert into file.test(filename, content) values (?, ?)", storeName, fileContent);
    }

    private void writeCompactBytes(String station, int date, ByteBuffer content){
        session.execute("insert into radarDPC.cd(station, date, data) values(?, ?, ?)", station, date, content);
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