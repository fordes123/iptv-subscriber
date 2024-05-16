package dev.fordes.iptv.handler.checker;

import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.Metadata;
import dev.fordes.iptv.model.enums.tag.Addr;
import dev.fordes.iptv.model.enums.tag.State;
import dev.fordes.iptv.util.CommonUtils;
import dev.fordes.iptv.util.Constants;
import dev.fordes.iptv.util.HttpUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpHeaders;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Chengfs on 2024/1/8
 */
@Slf4j
public class Checker {

    /**
     * 检查频道并生成标签
     *
     * @param channel {@link Channel}
     * @return {@link Channel}
     */
    public static Uni<Channel> check(Channel channel) {
        return detectMetadata(channel.getUrl())
                .onFailure()
                .recoverWithItem(e -> {
                    log.error("detect channel information failed: {}", e.getMessage());
                    return Metadata.fail();
                })
                .onItem().transform(e -> {
                    e.fill();
                    return channel;
                });
    }

    /**
     * 检测频道元数据
     *
     * @param url 频道url地址
     * @return {@link Metadata}
     */
    public static Uni<Metadata> detectMetadata(@Nonnull URL url) {
        return detectMediaInfo(url)
                .onItem().transform(e -> {
                    e.fill();
                    e.setType(Addr.fromURL(url));
                    if (e.getState() == null) {
                        e.setState(evalState(e.imageWidth, e.framerate, e.speed));
                    }
                    return e;
                });
    }

    /**
     * 检测频道媒体信息
     *
     * @param url 频道url地址
     * @return {@link Metadata}
     */
    public static Uni<Metadata> detectMediaInfo(URL url) {
        return HttpUtil.createGet(url, opt ->
                        opt.setConnectTimeout(10000)
                                .setMaxRedirects(5)
                                .setConnectTimeout(5000)
                                .setKeepAlive(true)
                                .setKeepAliveTimeout(2000))
                .flatMap(request -> {
                    long startTime = System.currentTimeMillis();
                    return request.setFollowRedirects(true).send()
                            .onFailure()
                            .retry()
                            .atMost(3)
                            .flatMap(resp -> {

                                if (resp.statusCode() != 200) {
                                    return Uni.createFrom().failure(new RuntimeException("request failed: " + resp.statusCode()));
                                }

                                if (CommonUtils.isM3UByMime(resp.getHeader(HttpHeaders.CONTENT_TYPE))) {

                                    return resp.body().flatMap(buffer -> {
                                        String content = buffer.toString(StandardCharsets.UTF_8);
                                        String path = Arrays.stream(StringUtils.split(content, Constants.LF_C))
                                                .filter(line -> !line.startsWith(Constants.SHARP))
                                                .findFirst().orElse(null);

                                        if (path != null) {
                                            URL subURL = CommonUtils.resolveURL(url, path);
                                            return detectMediaInfo(subURL);
                                        }
                                        return Uni.createFrom().failure(new RuntimeException("parse failed: No valid content found"));
                                    });
                                } else {

                                    //视频流

                                    return resp.body().flatMap(buffer -> {

                                        byte[] bytes = buffer.getBytes();
                                        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                                        InputStream is = new ByteArrayInputStream(byteBuffer.array());

                                        Metadata extend = new Metadata();
                                        extend.setSpeed(CommonUtils.calculateSpeed(System.currentTimeMillis() - startTime, buffer.length()));
                                        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(is)) {
                                            grabber.start();
                                            extend.setFramerate(grabber.getFrameRate());
                                            extend.setImageWidth(grabber.getImageWidth());
                                            extend.setImageHeight(grabber.getImageHeight());
                                            extend.setBitrate(grabber.getVideoBitrate());
                                            extend.setVideoCodec(grabber.getVideoCodecName());
                                            grabber.stop();
                                        } catch (Exception e) {
                                            return Uni.createFrom().failure(new RuntimeException("ffmpeg Detection failed", e));
                                        }
                                        return Uni.createFrom().item(extend);
                                    });
                                }
                            });
                });
    }

    /**
     * 评估状态，标准见 {@link State}
     *
     * @param imageWidth 横向分辨率
     * @param framerate  帧率
     * @param speed      播放速率
     * @return {@link State}
     */
    private static State evalState(Integer imageWidth, Double framerate, Double speed) {
        if (imageWidth >= 720 && framerate >= 25 && speed >= 5) {
            if (imageWidth >= 1080 && framerate >= 60 && speed >= 8) {
                return State.EXCELLENT;
            }
            return State.HEALTHY;

        } else {
            return State.UNHEALTHY;
        }
    }

}