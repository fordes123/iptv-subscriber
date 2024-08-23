package dev.fordes.iptv.service.Impl;

import dev.fordes.iptv.handler.checker.Checker;
import dev.fordes.iptv.handler.parser.Parser;
import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.Metadata;
import dev.fordes.iptv.service.ChannelService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;

/**
 * @author Chengfs on 2024/8/20
 */
@Slf4j
@ApplicationScoped
public class ChannelServiceImpl implements ChannelService {

    @Override
    public Multi<Channel> parse(Parser.Config options, String path) {
        return Parser.getParser(path, options).get();
    }

    @Override
    public Uni<Channel> detect(Channel channel) {
        return Checker.check(channel);
    }

    @Override
    public Uni<Channel> detect(URL url) {
        return Uni.createFrom().item(url)
                .flatMap(e -> Checker.detectMetadata(e)
                        .map(metadata -> Channel.builder().url(e).metadata(metadata).build()));
    }

    @Override
    public Uni<Metadata> metadata(URL url) {
        return Uni.createFrom().item(url)
                .flatMap(Checker::detectMetadata);
    }
}