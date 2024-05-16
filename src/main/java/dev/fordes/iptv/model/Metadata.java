package dev.fordes.iptv.model;

import dev.fordes.iptv.model.enums.tag.Addr;
import dev.fordes.iptv.model.enums.tag.DPI;
import dev.fordes.iptv.model.enums.tag.State;
import lombok.Data;
import lombok.experimental.Accessors;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Optional;

/**
 * @author Chengfs on 2024/5/15
 */
@Data
@Schema(name = "Metadata" , description = "标签元数据")
@Accessors(chain = true)
public class Metadata {

    /**
     * 比特率
     */
    @Schema(description = "比特率")
    public Integer bitrate;

    /**
     * 播放速度，Mbps
     */
    @Schema(description = "播放速率Mbps")
    public Double speed;

    /**
     * 横向分辨率
     */
    @Schema(description = "横向分辨率")
    public Integer imageWidth;

    /**
     * 纵向分辨率
     */
    @Schema(description = "纵向分辨率")
    public Integer imageHeight;

    /**
     * 分辨率等级，基于横向分辨率计算
     */
    @Schema(description = "分辨率等级", implementation = DPI.class)
    public DPI resolution;

    /**
     * 帧率
     */
    @Schema(description = "帧率")
    public Double framerate;

    /**
     * 视频编码
     */
    @Schema(description = "视频编码")
    private String videoCodec;

    /**
     * 地址类型 {@link Addr}
     */
    @Schema(description = "地址类型", implementation = Addr.class)
    public Addr type;

    /**
     * 评估状态 {@link State}
     */
    @Schema(description = "评估状态", implementation = State.class)
    private State state;

    public void fill() {
        this.bitrate = Optional.ofNullable(this.bitrate).orElse(0);
        this.speed = Optional.ofNullable(this.speed).orElse(0.0);
        this.imageWidth = Optional.ofNullable(this.imageWidth).orElse(0);
        this.imageHeight = Optional.ofNullable(this.imageHeight).orElse(0);
        this.framerate = Optional.ofNullable(this.framerate).orElse(0.0);
        this.videoCodec = Optional.ofNullable(this.videoCodec).orElse("unknown");
    }

    public static Metadata empty() {
        Metadata info = new Metadata();
        info.fill();
        return info;
    }

    public static Metadata fail() {
        Metadata info = new Metadata();
        info.fill();
        info.setState(State.FAIL);
        return info;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
        if (imageWidth != null && imageWidth > 0) {
            this.resolution = DPI.valueOf(imageWidth);
        }
    }
}