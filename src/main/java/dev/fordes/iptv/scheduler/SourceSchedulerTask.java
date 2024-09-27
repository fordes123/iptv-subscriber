package dev.fordes.iptv.scheduler;

import dev.fordes.iptv.config.ParserConfig;
import dev.fordes.iptv.config.SourceConfig;
import dev.fordes.iptv.handler.composer.Composer;
import dev.fordes.iptv.handler.parser.Parser;
import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.enums.SourceType;
import dev.fordes.iptv.service.ChannelService;
import dev.fordes.iptv.util.FileSuffix;
import dev.fordes.iptv.util.FileUtil;
import dev.fordes.iptv.util.FilterUtil;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Set;

import static dev.fordes.iptv.util.FileSuffix.M3U;
import static dev.fordes.iptv.util.FileSuffix.M3U8;

/**
 * @author Chengfs on 2023/12/25
 */
@Slf4j
@ApplicationScoped
public class SourceSchedulerTask {

    private final SourceConfig config;
    private final Parser.Config parserConfig;
    private final ChannelService channelService;

    @Inject
    public SourceSchedulerTask(SourceConfig config, ParserConfig parserConfig, ChannelService channelService) {
        this.config = config;
        this.parserConfig = parserConfig;
        this.channelService = channelService;
    }

    @Scheduled(cron = "${tv-source.cron:disabled}")
    public void source() {

        Optional.ofNullable(config.consumer())
                .filter(e -> !e.isEmpty())
                .ifPresentOrElse(consumer -> {
                    Set<String> supplier = config.supplier()
                            .filter(e -> !e.isEmpty())
                            .orElseGet(Set::of);

                    Multi<Channel> channels = Multi.createFrom()
                            .items(supplier.stream())
                            .flatMap(e -> channelService.parse(parserConfig, e))
                            .onItem().transform(channelService::detect)
                            .flatMap(Uni::toMulti);

                    consumer.orElse(Set.of()).forEach((prop) -> {
                        String file = prop.file();
                        String suffix = FileSuffix.get(file);
                        SourceType type = StringUtils.equalsAnyIgnoreCase(suffix, M3U, M3U8) ?
                                SourceType.M3U : SourceType.GENERIC;

                        Multi<String> apply = Composer.getComposer(type).apply(channels
                                .filter(channel -> FilterUtil.match(prop.filter(), channel))
                                .flatMap(channel -> Multi.createFrom().emitter(emitter -> {
                                    Optional.ofNullable(prop.group()).filter(e -> !e.isEmpty())
                                            .ifPresentOrElse(group -> {
                                                group.forEach((groupName, filters) -> {
                                                    if (FilterUtil.match(filters, channel)) {
                                                        Channel temp = channel.copy();
                                                        temp.setGroupTitle(groupName);
                                                        emitter.emit(temp);
                                                    }
                                                });

                                            }, () -> {
                                                emitter.emit(channel.copy());
                                            });
                                    emitter.complete();
                                })));

                        FileUtil.write(file, apply);
                    });
                }, () -> log.error("output is empty!"));
    }


}