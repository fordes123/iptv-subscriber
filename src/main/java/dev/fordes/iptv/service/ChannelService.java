package dev.fordes.iptv.service;

import dev.fordes.iptv.handler.parser.ParseOptions;
import dev.fordes.iptv.handler.parser.Parser;
import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.Metadata;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

import java.net.URI;
import java.net.URL;

/**
 * @author Chengfs on 2024/8/20
 */
public interface ChannelService {

    Multi<Channel> parse(Parser.Config options, String path);

    default Multi<Channel> parse(String path) {
        return parse(new ParseOptions(), path);
    }

    Uni<Channel> detect(Channel channel);

    Uni<Metadata> metadata(URL url);

    default Uni<Metadata> metadata(String url) {
        return Uni.createFrom().item(url)
                .onItem().transform(Unchecked.function(e -> URI.create(e).toURL()))
                .flatMap(this::metadata);
    }

    Uni<Channel> detect(URL url);

    default Uni<Channel> detect(String url) {
        return Uni.createFrom().item(url)
                .onItem().transform(Unchecked.function(e -> URI.create(e).toURL()))
                .flatMap(this::detect);
    }
}