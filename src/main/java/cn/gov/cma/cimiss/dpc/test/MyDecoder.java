package cn.gov.cma.cimiss.dpc.test;

import cn.gov.cma.cimiss.dpc.handle.Decoder;

import java.io.*;
import java.util.Map;

/**
 * Created by dyf on 20/12/2016.
 */
public class MyDecoder extends Decoder {
    @Override
    public void decode(String absoluteFileName, Map<String, Object> context) throws Exception {
        try {

            System.out.println("aaaaaaaaa");
            BufferedReader br = new BufferedReader(new FileReader(absoluteFileName));
            String line = br.readLine();
            while(line != null){
                String[] arrs = line.split(" ");
                if(Integer.parseInt(arrs[1]) > 0){
                    context.put(arrs[0], arrs[1]);
                }
                line = br.readLine();
            }
        }catch (FileNotFoundException e){
            System.out.println("File:" + absoluteFileName +" not exist");
        }
    }
}