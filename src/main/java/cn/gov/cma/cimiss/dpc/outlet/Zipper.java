package cn.gov.cma.cimiss.dpc.outlet;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {
    private static Logger logger = LoggerFactory.getLogger( Zipper.class );

    static {
        PropertyConfigurator.configure( "conf/log4j.properties" );
    }

    public void filterdZip(String dir, ArrayList<File> filteredFiles, String output){
        //Zip all files in same station and month to a zip file.
        File tFile = new File(output);

        if (tFile.exists()) {
            tFile.delete();
        }
        try{
            if (!tFile.exists()){
                tFile.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        ZipOutputStream zout = null;
        try{
            zout = new ZipOutputStream(new FileOutputStream(tFile));

            for (File file: filteredFiles) {
                if(file.isDirectory()){
                    logger.error("DIRECTORY IN RADAR DATA SETS.");
                    break;
                }
                else {
                    zipFile(file, zout, "");
                }
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }finally{
            try {
                if (zout != null){
                    zout.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void zipBatch(String dir){
        File srcfile = new File(dir);
        File targetFile = new File("verify/zip.zip");

        try{
            if(targetFile.exists()){
                targetFile.delete();
            }

            if (!targetFile.exists())
            {
                targetFile.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        ZipOutputStream out = null;
        try {
            logger.info("Compaction begin.");
            out = new ZipOutputStream(new FileOutputStream(targetFile));

            if(srcfile.isFile()){
                zipFile(srcfile, out, "");
            } else{
                File[] list = srcfile.listFiles();
                for (int i = 0; i < list.length; i++) {
                    compress(list[i], out, "");
                }
            }

            logger.info("Compaction finished.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void compress(File file, ZipOutputStream out, String basedir) {
        if (file.isDirectory()) {
            this.zipDirectory(file, out, basedir);
        } else {
            this.zipFile(file, out, basedir);
        }
    }

    private void zipFile(File srcfile, ZipOutputStream out, String basedir) {
        if (!srcfile.exists())
            return;

        byte[] buf = new byte[1024];
        FileInputStream in = null;

        try {
            int len;
            in = new FileInputStream(srcfile);
            out.putNextEntry(new ZipEntry(basedir + srcfile.getName()));

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.closeEntry();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void zipDirectory(File dir, ZipOutputStream out, String basedir) {
        if (!dir.exists())
            return;

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            compress(files[i], out, basedir + dir.getName() + "/");
        }
    }
}
