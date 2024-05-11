package dev.fordes.iptv.model.enums;

/**
 * @author Chengfs on 2024/5/7
 */
public enum MimeType {

    //"application/x-mpegurl", "application/vnd.apple.mpegurl"

    HLS("application/x-mpegurl", "m3u8"),
    TS("video/mp2t", "ts"),
    ;

    private final String type;
    private final String suffix;

    MimeType(String type, String suffix) {
        this.type = type;
        this.suffix = suffix;
    }

    public String getType() {
        return type;
    }

    public String getSuffix() {
        return suffix;
    }
}