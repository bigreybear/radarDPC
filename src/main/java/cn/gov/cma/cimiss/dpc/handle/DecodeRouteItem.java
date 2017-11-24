package cn.gov.cma.cimiss.dpc.handle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dyf on 18/12/2016.
 */
public class DecodeRouteItem {
    private String filename;
    private Class<? extends Decoder> decoderClass;
    private Class<? extends Writer> writerClass;
    private Decoder decoder;
    private Writer writer;
    private Pattern pattern;

    public DecodeRouteItem(String filename, Class<? extends Decoder> decoderClass, Class<? extends Writer> writerClass) throws InstantiationException, IllegalAccessException{
        this.filename = filename;
        this.decoderClass = decoderClass;
        this.writerClass = writerClass;
        this.pattern = Pattern.compile(filename);
        this.decoder =  decoderClass.newInstance();
        this.writer =  writerClass.newInstance();
    }

    public String getFilename() {
        return filename;
    }

    public Class<? extends Decoder> getDecoderClass() {
        return decoderClass;
    }

    public Decoder getDecoder() {
        return decoder;
    }

    public Writer getWriter() {
        return writer;
    }

    public boolean match(String filename) {
        Matcher matcher = pattern.matcher(filename);
        return matcher.matches();
    }
}
