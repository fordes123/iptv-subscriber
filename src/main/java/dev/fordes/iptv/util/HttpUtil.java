package dev.fordes.iptv.util;

import dev.fordes.iptv.model.enums.MutinyVertx;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.http.HttpClientRequest;
import io.vertx.mutiny.core.http.HttpClientResponse;
import io.vertx.mutiny.core.http.HttpHeaders;
import jakarta.annotation.Nonnull;
import org.jboss.logmanager.Logger;

import java.net.URL;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Chengfs on 2024/5/7
 */
public class HttpUtil {

    public static final Logger log = Logger.getLogger(HttpUtil.class.getName());

    /**
     * 创建get请求
     *
     * @param url             请求url
     * @param optionsConsumer Client参数
     * @return {@link Uni<HttpClientRequest>}
     */
    public static Uni<HttpClientRequest> createGet(String url, Consumer<HttpClientOptions> optionsConsumer) {
        HttpClientOptions opt = new HttpClientOptions();
        Optional.ofNullable(optionsConsumer).ifPresent(consumer -> consumer.accept(opt));
        return MutinyVertx.INSTANCE.getVertx().createHttpClient(opt)
                .request(HttpMethod.GET, url);
    }

    /**
     * @param url             请求url
     * @param optionsConsumer Client参数
     * @return {@link Uni<HttpClientRequest>}
     * @see #get(String, boolean, Consumer, Consumer)
     */
    public static Uni<HttpClientRequest> createGet(URL url, Consumer<HttpClientOptions> optionsConsumer) {
        String uri = url.getPath() + (url.getQuery() == null ? Constants.EMPTY : Constants.QUESTION + url.getQuery());


        HttpClientOptions opt = new HttpClientOptions();
        Optional.ofNullable(optionsConsumer).ifPresent(consumer -> consumer.accept(opt));
        return MutinyVertx.INSTANCE.getVertx().createHttpClient(opt)
                .request(HttpMethod.GET, url.getHost(), uri);
    }

    /**
     * 响应式发送get请求，并自定义结果处理<br/>
     * 结果逐块处理，处理器可能被多次调用
     *
     * @param urlStr           请求url
     * @param flowRedirect     是否跟随重定向
     * @param optionsConsumer  Client参数
     * @param responseConsumer 结果
     */
    public static void get(String urlStr, boolean flowRedirect, Consumer<HttpClientOptions> optionsConsumer,
                           Consumer<HttpClientResponse> responseConsumer) {
        createGet(urlStr, optionsConsumer)
                .subscribe()
                .with(request -> request.setFollowRedirects(flowRedirect).send()
                        .onFailure().invoke(() -> log.severe("request failed:" + urlStr))
                        .subscribe().with(responseConsumer));
    }

    /**
     * @see #get(String, boolean, Consumer, Consumer)
     */
    public static void get(URL url, boolean flowRedirect, Consumer<HttpClientOptions> optionsConsumer,
                           Consumer<HttpClientResponse> responseConsumer) {
        createGet(url, optionsConsumer)
                .subscribe()
                .with(request -> request.setFollowRedirects(flowRedirect).send()
                        .onFailure().invoke(() -> log.severe("request failed:" + url))
                        .subscribe().with(responseConsumer));
    }


    /**
     * 响应式发送get请求，并自定义结果处理<br/>
     * 默认跟随重定向<br/>
     * 结果逐块处理，处理器可能被多次调用
     *
     * @param urlStr           请求url
     * @param responseConsumer 结果
     */
    public static void get(String urlStr, Consumer<HttpClientResponse> responseConsumer) {
        get(urlStr, true, null, responseConsumer);
    }


    /**
     * @see #get(String, Consumer)
     */
    public static void get(URL url, Consumer<HttpClientResponse> responseConsumer) {
        get(url, true, null, responseConsumer);
    }


    /**
     * 下载文件
     *
     * @param url           请求 url
     * @param timerConsumer 计时回调，t: 为完整耗时(ms) u: 响应体长度(byte)
     * @return 文件路径
     */
    public static Multi<String> download(@Nonnull URL url,
                                         @Nonnull BiConsumer<Long, Long> timerConsumer) {

        long startTime = System.currentTimeMillis();
        return createGet(url, opt -> opt.setMaxRedirects(5).setConnectTimeout(5000)
                .setKeepAlive(true).setKeepAliveTimeout(2000))
                .flatMap(request -> request.setFollowRedirects(true)
                        .setMaxRedirects(5)
                        .putHeader(HttpHeaders.USER_AGENT,
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.5410.0 Safari/537.36")
                        .putHeader(HttpHeaders.ACCEPT, "*/*")
                        .putHeader(HttpHeaders.HOST, url.getHost())
                        .putHeader(HttpHeaders.CONNECTION, "keep-alive")
                        .putHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br")
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                        .send()
                        .onFailure()
                        .invoke(ex -> log.severe(url + " content failed: " + ex.getMessage()))
                        .flatMap(response -> {
//                            Assert.isTrue(response.statusCode() == 200, "url" + " download failed, status code: " + response.statusCode());
                            String path = FileUtil.createTempFile(url, response.headers()).getAbsolutePath();

                            long time = System.currentTimeMillis() - startTime;
                            Optional.ofNullable(response.getHeader(HttpHeaders.CONTENT_LENGTH))
                                    .ifPresent(length -> timerConsumer.accept(time, Long.parseLong(length)));

                            return response.body()
                                    .onFailure()
                                    .invoke(ex -> log.severe(url + " response body failed: " + ex.getMessage()))
                                    .flatMap(buffer -> MutinyVertx.INSTANCE.getVertx().fileSystem()
                                            .writeFile(path, buffer)
                                            .onFailure()
                                            .invoke(ex -> log.severe("path: " + path + " temporary file write failure: " + ex.getMessage()))
                                            .onItem()
                                            .transform(voidUni -> {
                                                log.fine("download complete: " + url + " -> " + path);
                                                return path;
                                            }));
                        })
                )
                .toMulti();
    }


}