package dev.fordes.iptv.handler.checker;

import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.util.CommonUtils;
import dev.fordes.iptv.util.Constants;
import dev.fordes.iptv.util.HttpUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.jboss.logmanager.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Chengfs on 2024/1/8
 */
public class Checker {

    private static final Logger log = Logger.getLogger(Checker.class.getName());

    public static Uni<Channel> check(Channel channel) {
        return check(channel, channel.getUrl());
    }

    public static Uni<Channel> check(Channel channel, URL url) {
        return HttpUtil.createGet(url, opt -> {
            opt.setConnectTimeout(10000)
                    .setMaxRedirects(5)
                    .setConnectTimeout(5000)
                    .setKeepAlive(true)
                    .setKeepAliveTimeout(2000);
        }).flatMap(request -> {
            long startTime = System.currentTimeMillis();
            return request.setFollowRedirects(true).send().flatMap(resp -> {

                if (resp.statusCode() != 200) {
                    return Uni.createFrom().failure(new RuntimeException("status code: " + resp.statusCode()));
                }

                if (CommonUtils.isM3UByMime(resp.getHeader(HttpHeaders.CONTENT_TYPE))) {

                    return resp.body().flatMap(buffer -> {
                        String content = buffer.toString(StandardCharsets.UTF_8);
                        String path = Arrays.stream(StringUtils.split(content, Constants.LF_C))
                                .filter(line -> !line.startsWith(Constants.SHARP))
                                .findFirst().orElse(null);

                        if (path != null) {
                            URL subURL = CommonUtils.resolveURL(channel.getUrl(), path);
                            return check(channel, subURL);
                        }

                        return Uni.createFrom().failure(new RuntimeException("path is null"));
                    });
                } else {

                    //视频流

                    return resp.body().flatMap(buffer -> {

                        byte[] bytes = buffer.getBytes();
                        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                        InputStream is = new ByteArrayInputStream(byteBuffer.array());

                        channel.setSpeed(CommonUtils.calculateSpeed(System.currentTimeMillis() - startTime, buffer.length()));
                        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(is)) {
                            grabber.start();
                            channel.setFramerate(grabber.getFrameRate());
                            channel.setImageWidth(grabber.getImageWidth());
                            channel.setBitrate(grabber.getVideoBitrate());
                            channel.setVideoCodec(grabber.getVideoCodecName());
                            grabber.stop();
                        } catch (Exception e) {
                            log.severe("get video info failed:" + e.getMessage());
                        }
                        return Uni.createFrom().item(channel);
                    });
                }
            });
        });
    }

}