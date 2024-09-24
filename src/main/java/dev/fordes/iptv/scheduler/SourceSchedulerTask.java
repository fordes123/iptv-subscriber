package dev.fordes.iptv.scheduler;

import dev.fordes.iptv.config.ParserConfig;
import dev.fordes.iptv.config.SourceConfig;
import dev.fordes.iptv.handler.composer.Composer;
import dev.fordes.iptv.handler.parser.Parser;
import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.enums.MutinyVertx;
import dev.fordes.iptv.model.enums.SourceType;
import dev.fordes.iptv.service.ChannelService;
import dev.fordes.iptv.util.FileSuffix;
import dev.fordes.iptv.util.FilterUtil;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.buffer.Buffer;
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

                        Composer composer = Composer.getComposer(type);
                        write(file, composer.apply(channels
                                .filter(e -> prop.filter().stream().allMatch(f -> FilterUtil.match(f, e)))
                                .flatMap(e -> Multi.createFrom().emitter(emitter -> {
                                    prop.group().forEach((k, v) -> {
                                        if (v.stream().allMatch(f -> FilterUtil.match(f, e))) {
                                            Channel temp = e.copy();
                                            temp.setGroupTitle(k);
                                            emitter.emit(temp);
                                        }
                                    });
                                    emitter.complete();
                                }))));
                    });
                }, () -> log.error("output is empty!"));
    }


    private void write(String file, Multi<String> content) {
        final OpenOptions openOptions = new OpenOptions()
                .setCreate(true)
                .setTruncateExisting(true);

        MutinyVertx.INSTANCE.getVertx().fileSystem()
                .open(file, openOptions)
                .onItem().transformToUni(asyncFile ->
                        content.onItem().transformToUni(str -> {
                                    Buffer buffer = Buffer.buffer(str);
                                    return asyncFile.write(buffer)
                                            .onFailure().transform(t -> new RuntimeException("Write failed", t)); // Handle write errors
                                })
                                .concatenate()
                                .onFailure().recoverWithItem(t -> {
                                    // Error handling for individual write failures
                                    System.err.println("Error writing to file: " + t.getMessage());
                                    return null; // Continue with next items
                                })
                                .collect().asList() // Collect all writes to ensure completion
                                .onItem().transformToUni(ignored -> asyncFile.close()) // Close file after all writes are done
                )
                .subscribe().with(
                        ignored -> System.out.println("Write completed successfully"),
                        failure -> System.err.println("Write operation failed: " + failure.getMessage())
                );
    }
}