package dev.fordes.iptv.util;

import dev.fordes.iptv.model.enums.MutinyVertx;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.http.HttpClientRequest;
import io.vertx.mutiny.core.http.HttpClientResponse;
import org.jboss.logmanager.Logger;

import java.net.URI;
import java.net.URL;
import java.util.Optional;
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
        return Uni.createFrom().item(url)
                .onItem().transform(Unchecked.function(e -> URI.create(e).toURL()))
                .flatMap(e -> createGet(e, optionsConsumer));
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
        if (url.getPort() > 0) {
            return MutinyVertx.INSTANCE.getVertx().createHttpClient(opt)
                    .request(HttpMethod.GET, url.getPort(), url.getHost(), uri);
        }
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


}