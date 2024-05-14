package dev.fordes.iptv.model;


import dev.fordes.iptv.model.enums.tag.Addr;
import dev.fordes.iptv.model.enums.tag.Status;
import lombok.Data;

import java.net.URL;

/**
 * @author Chengfs on 2023/12/25
 */
@Data
public class Channel {

    public static final String EXTINF = "#EXTINF";
    public static final String TVG_ID = "tvg-id";
    public static final String TVG_NAME = "tvg-name";
    public static final String TVG_LOGO = "tvg-logo";
    public static final String GROUP_TITLE = "group-title";
    public static final String X_TVG_URL = "x-tvg-url";

    /**
     * 扩展标签，用于定义频道的持续时间（以秒为单位）。-1 通常表示直播流，没有确定的节目长度。
     */
    private Long extInf;

    /**
     * 唯一标识符，用于匹配频道和节目指南数据。
     */
    private String tvgId;

    /**
     * 频道名称
     */
    private String tvgName;

    /**
     * 频道图标，可以是URL
     */
    private String tvgLogo;

    /**
     * 分组名称
     */
    private String groupTitle;

    /**
     * 频道显示名称
     */
    private String displayName;

    /**
     * xml节目单地址
     */
    private URL tvgUrl;

    /**
     * 地址
     */
    private URL url;

    /**
     * 比特率
     */
    public Integer bitrate;

    /**
     * 播放速度，Mbps
     */
    public Double speed;

    /**
     * 横向分辨率
     */
    public Integer imageWidth;

    /**
     * 帧率
     */
    public Double framerate;

    /**
     * 视频编码
     */
    private String videoCodec;

    /**
     * 地址类型 {@link Addr}
     */
    public Addr type;

    /**
     * 评估状态
     */
    private Status status;

}