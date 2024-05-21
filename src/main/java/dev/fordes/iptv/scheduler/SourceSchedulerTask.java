package dev.fordes.iptv.scheduler;

import dev.fordes.iptv.config.ISProperties;
import dev.fordes.iptv.handler.checker.Checker;
import dev.fordes.iptv.handler.composer.Composer;
import dev.fordes.iptv.handler.parser.Parser;
import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.enums.MutinyVertx;
import dev.fordes.iptv.model.enums.SourceType;
import dev.fordes.iptv.util.FileSuffix;
import dev.fordes.iptv.util.FilterUtil;
import io.quarkus.runtime.Quarkus;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.fordes.iptv.util.FileSuffix.M3U;
import static dev.fordes.iptv.util.FileSuffix.M3U8;

/**
 * @author Chengfs on 2023/12/25
 */
@Slf4j
@ApplicationScoped
public class SourceSchedulerTask {

    @Inject
    ISProperties config;

    //    @Scheduled(cron = "{scheduled.source: disabled}")
    public void source() throws InterruptedException {

        if (config.output().isEmpty()) {
            log.error("output is empty!");
            Quarkus.asyncExit();
        }

        Multi<Channel> channels = Multi.createFrom()
                .items(config.source().stream())
                .filter(StringUtils::isNotBlank)
                .map(e -> Parser.getParser(e, config.parser()))
                .map(e -> e.flatMap(parser -> parser.get().toUni()))
                .onItem().transform(e -> e.flatMap(Checker::check))
                .flatMap(Uni::toMulti);

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        config.output().forEach(file ->
                //在虚拟线程中处理写入每一个文件
                executor.submit(() -> {
                    String suffix = FileSuffix.get(file.file());
                    SourceType type = StringUtils.equalsAnyIgnoreCase(suffix, M3U, M3U8) ?
                            SourceType.M3U : SourceType.GENERIC;

                    Composer composer = Composer.getComposer(type);
                    composer.apply(channels
                                    .filter(e -> file.filter().stream().allMatch(f -> FilterUtil.match(f, e)))
                                    .flatMap(e -> Multi.createFrom().emitter(emitter -> {
                                        file.group().forEach((k, v) -> {
                                            if (v.stream().allMatch(f -> FilterUtil.match(f, e))) {
                                                Channel temp = e.copy();
                                                temp.setGroupTitle(k);
                                                emitter.emit(temp);
                                            }
                                        });
                                        emitter.complete();
                                    })))
                            .map(e -> MutinyVertx.INSTANCE.getVertx().fileSystem()
                                    .writeFile(file.file(), Buffer.buffer(e)))
                            .flatMap(Uni::toMulti)
                            .subscribe().with(
                                    i -> log.info("write file: {}", file.file()),
                                    e -> log.error("write file error: {}", file.file(), e)
                            );
                }));

        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(1000);
        }
    }
}