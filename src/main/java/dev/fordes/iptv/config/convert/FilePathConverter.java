package dev.fordes.iptv.config.convert;

import org.eclipse.microprofile.config.spi.Converter;

/**
 * @author Chengfs on 2024/9/24
 */
public class FilePathConverter implements Converter<String> {

    @Override
    public String convert(String s) throws IllegalArgumentException, NullPointerException {
        //TODO 将文件路径转换为合法路径 & 正确识别http和本地地址
        return s;
    }
}