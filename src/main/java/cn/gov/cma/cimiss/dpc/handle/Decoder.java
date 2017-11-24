package cn.gov.cma.cimiss.dpc.handle;

import java.util.*;

public abstract class Decoder {
	public abstract void decode(String absoluteFileName, Map<String, Object> context) throws Exception;//将通信系统接收到的原始文件fileName进行解码，将解码后数据保存在MeteorologicalData类的对象的ArrayList中进行返回
}

