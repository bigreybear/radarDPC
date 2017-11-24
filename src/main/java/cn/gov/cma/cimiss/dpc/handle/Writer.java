package cn.gov.cma.cimiss.dpc.handle;

import java.util.Map;

public abstract class Writer {
	public abstract void write(Map<String, Object> context);//将具体的某个气象数据对象写入存储中，CassandraWriter,SambaWriter实现了这个接口
}

