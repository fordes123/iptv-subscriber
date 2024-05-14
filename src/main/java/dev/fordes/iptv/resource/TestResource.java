package dev.fordes.iptv.resource;

import dev.fordes.iptv.handler.checker.Checker;
import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.scheduler.SourceSchedulerTask;
import dev.fordes.iptv.util.CommonUtils;
import dev.fordes.iptv.util.HttpUtil;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Vertx;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Chengfs on 2023/12/22
 */
@Slf4j
@Path("/test")
public class TestResource {

    @Inject
    SourceSchedulerTask config;
    private final Vertx vertx;

    public TestResource(Vertx vertx) {
        this.vertx = vertx;
    }

//    private static final Logger log = Logger.getLogger(TestResource.class.getName());

    @GET
    @Path("/1")
    public Uni<List<Channel>> test() {
//        return config.source().collect().asList();
        return Uni.createFrom().nullItem();
    }

    @GET
    @Path("/2")
    public Uni<List<String>> test2() throws MalformedURLException {
        URL url = new URL("http://1.195.111.251:11190/tsfile/live/0004_1.m3u8");
        return HttpUtil.download(url, (t, u) -> {
        }).collect().asList();
    }

    @GET
    @Path("/3")
    public Multi<String> test3() throws MalformedURLException {
        URL url = new URL("https://2bf5eb85eee238923fab2857a5470d28.livehwc3.cn/pullsstv90080111.ssws.tv/live/SSTV20220729.m3u8?sub_m3u8=true&edge_slice=true&user_session_id=3170aaa4764436b50ff7ef01cd586445");
        return HttpUtil.download(url, (t, u) -> {
            double speed = CommonUtils.calculateSpeed(t, u);
            System.out.println(1);
        });

    }

    @GET
    @Path("/4")
    public Multi<Channel> test4() throws MalformedURLException {
        final Channel channel = new Channel();
        channel.setUrl(URI.create("http://27.222.3.214/liveali-tp4k.cctv.cn/live/4K10M.stream/playlist.m3u8").toURL());

       return Uni.createFrom().item(channel)
               .flatMap(e -> Checker.check(e, e.getUrl()))
               .toMulti();

//
//
//        return Uni.createFrom().item(channel)
//                .onItem()
//                .invoke(e -> {
//                    Checker.check(e, e.getUrl());
//                })
//                .onItem()
//
//                .transformToMulti(item -> {
//                    return Multi.createFrom().item(item);
//                });
    }

    @GET
    @Path("/5")
    public Multi<String> test5() throws MalformedURLException {
        log.error("hello world");
        URL url = URI.create("http://hls.weathertv.cn/tslslive/qCFIfHB/hls/live_sd.m3u8").toURL();
        HttpUtil.get(url, resp -> {
            resp.body().subscribe()
                    .with(Unchecked.consumer(buffer -> {
                        String string = buffer.toString(StandardCharsets.UTF_8);
                        log.info(string);
                    } ));
        });

        return Multi.createFrom().empty();
    }
}