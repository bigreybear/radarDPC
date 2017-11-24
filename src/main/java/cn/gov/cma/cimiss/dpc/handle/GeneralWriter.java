package cn.gov.cma.cimiss.dpc.handle;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneralWriter extends Writer {
	private static Logger logger = LoggerFactory.getLogger( GeneralWriter.class );

	@Override
	public void write( Map<String, Object> context) {
		for(String key : context.keySet()){
		    System.out.println(key +" " + context.get(key));
        }
	}
}

