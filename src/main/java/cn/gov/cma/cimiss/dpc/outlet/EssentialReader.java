package cn.gov.cma.cimiss.dpc.outlet;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class EssentialReader {
    private static Logger logger = null;

    public static Cluster cluster;
    public static Session session;
    //init
    static {
        PropertyConfigurator.configure( "conf/log4j.properties" );
        logger = LoggerFactory.getLogger( EssentialReader.class );
        try {
            cluster = Cluster.builder()
                    .addContactPoint("127.0.0.1")
                    .build();
            session = cluster.connect();

        } finally {
        }
    }

    public static void writeToFS(ByteBuffer bbf) throws FileNotFoundException, IOException{
        writeToFS(bbf,"example.pgm");
    }

    public static void writeToFS(ByteBuffer bbf, String fileName) throws FileNotFoundException, IOException{
        String pathName = "verify/" + fileName;
        File file = new File(pathName);
        FileOutputStream fos = new FileOutputStream(file);
        if (!file.exists())
        {
            file.createNewFile();
        }
        byte[] b = new byte[bbf.capacity()];
        bbf.get(b, 0, b.length);
        fos.write(b);
        fos.flush();
        fos.close();
        logger.info("read success.");
    }


    public static void main(String[] args) {

        System.out.println("hello");
        ResultSet rs = session.execute("select * from file.test;");
        Row r1 = rs.one();
        ByteBuffer bbf = r1.getBytes("content");
        try{
            writeToFS(bbf);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
