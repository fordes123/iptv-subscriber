package dev.fordes.iptv.util;

import org.apache.commons.lang3.StringUtils;

public class FileSuffix {

    public FileSuffix() {}

    public static final String M3U = "m3u";
    public static final String M3U8 = "m3u8";
    public static final String TXT = "txt";
    public static final String TS = "ts";

    public static String get(String path) {
        return StringUtils.substringAfterLast(path, Constants.DOT);
    }
}
