package cn.gov.cma.cimiss.dpc.example;

import cn.gov.cma.cimiss.dpc.handle.Decoder;
import cn.gov.cma.cimiss.dpc.util.StringUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

/**
 * Created by dyf on 20/12/2016.
 */
public class MyDecoder extends Decoder {
    @Override
    public void decode(String absoluteFileName, Map<String, Object> context) throws Exception {
        context.put("uploadFile", absoluteFileName);
    }
}