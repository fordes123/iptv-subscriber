package dev.fordes.iptv.model.vo;


import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.Metadata;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Optional;

/**
 * @author Chengfs on 2023/12/25
 */
@Data
@Schema(name = "ChannelVO", description = "频道信息")
public class ChannelVO {

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
    @Schema(description = "xml节目单地址", type = SchemaType.STRING)
    private String tvgUrl;

    /**
     * 地址
     */
    @Schema(description = "地址", type = SchemaType.STRING)
    private String url;

    /**
     * 频道标签信息
     */
    @Schema(description = "频道标签信息", implementation = Metadata.class)
    private Metadata metadata;

    public static ChannelVO of(Channel channel) {
        ChannelVO vo = new ChannelVO();
        vo.setExtInf(channel.getExtInf());
        vo.setTvgId(channel.getTvgId());
        vo.setTvgName(channel.getTvgName());
        vo.setTvgLogo(channel.getTvgLogo());
        vo.setGroupTitle(channel.getGroupTitle());
        vo.setDisplayName(channel.getDisplayName());
        vo.setMetadata(channel.getMetadata());
        Optional.ofNullable(channel.getTvgUrl()).ifPresent(e -> vo.setTvgUrl(e.toString()));
        Optional.ofNullable(channel.getUrl()).ifPresent(e -> vo.setUrl(e.toString()));
        return vo;
    }
}