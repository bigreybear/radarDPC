package cn.gov.cma.cimiss.dpc.handle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyf on 18/12/2016.
 */
public class DecodeRoute {
    public static Logger logger = LoggerFactory.getLogger( DecodeRoute.class );//Log4j日志
    private List<DecodeRouteItem> decodeRouteItemList = new ArrayList<DecodeRouteItem>();
    public void addRoute (String filename, Class<? extends Decoder> decoderClass, Class<? extends Writer> writerClass){
        try {
            decodeRouteItemList.add(new DecodeRouteItem(filename, decoderClass, writerClass));
        }catch (Exception e){
            logger.error("Can not addRoute：" +e.getMessage());
            throw new RuntimeException();
        }
    }

    protected Decoder getDecoder(String filename){
        for(DecodeRouteItem decodeRouteItem: decodeRouteItemList){
            if(decodeRouteItem.match(filename))
                return decodeRouteItem.getDecoder();
        }
        return null;
    }

    protected Writer getWriter(String filename){
        for(DecodeRouteItem decodeRouteItem: decodeRouteItemList){
            if(decodeRouteItem.match(filename))
                return decodeRouteItem.getWriter();
        }
        return null;
    }
}
