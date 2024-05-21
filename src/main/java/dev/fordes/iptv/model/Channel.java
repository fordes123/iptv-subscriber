package dev.fordes.iptv.model;


import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.net.URL;

/**
 * @author Chengfs on 2023/12/25
 */
@Data
@Schema(name = "Channel", description = "频道")
public class Channel {

    public static final String EXT_INF = "#EXTINF";
    public static final String TVG_ID = "tvg-id";
    public static final String TVG_NAME = "tvg-name";
    public static final String TVG_LOGO = "tvg-logo";
    public static final String GROUP_TITLE = "group-title";
    public static final String X_TVG_URL = "x-tvg-url";

    /**
     * 扩展标签，用于定义频道的持续时间（以秒为单位）。-1 通常表示直播流，没有确定的节目长度。
     */
    @Schema(description = "扩展标签，用于定义频道的持续时间（以秒为单位）。-1 通常表示直播流，没有确定的节目长度。")
    private Long extInf;

    /**
     * 唯一标识符，用于匹配频道和节目指南数据。
     */
    @Schema(description = "唯一标识符，用于匹配频道和节目指南数据。")
    private String tvgId;

    /**
     * 频道名称
     */
    @Schema(description = "频道名称")
    private String tvgName;

    /**
     * 频道图标，可以是URL
     */
    @Schema(description = "频道图标，可以是URL")
    private String tvgLogo;

    /**
     * 分组名称
     */
    @Schema(description = "分组名称")
    private String groupTitle;

    /**
     * 频道显示名称
     */
    @Schema(description = "频道显示名称")
    private String displayName;

    /**
     * xml节目单地址
     */
    @Schema(description = "xml节目单地址", type = SchemaType.STRING, implementation = Void.class)
    private URL tvgUrl;

    /**
     * 地址
     */
    @Schema(description = "地址", type = SchemaType.STRING, implementation = Void.class)
    private URL url;

    /**
     * 频道标签信息
     */
    @Schema(description = "频道标签信息", implementation = Metadata.class)
    private Metadata metadata;

    public Channel copy() {
        Channel channel = new Channel();
        channel.setExtInf(this.getExtInf());
        channel.setTvgId(this.getTvgId());
        channel.setTvgName(this.getTvgName());
        channel.setTvgLogo(this.getTvgLogo());
        channel.setGroupTitle(this.getGroupTitle());
        channel.setDisplayName(this.getDisplayName());
        channel.setUrl(this.getUrl());
        channel.setTvgUrl(this.getTvgUrl());
        channel.setMetadata(this.getMetadata());
        return channel;
    }
}