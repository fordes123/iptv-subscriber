package dev.fordes.iptv.scheduler;

import dev.fordes.iptv.config.ISProperties;
import dev.fordes.iptv.handler.checker.Checker;
import dev.fordes.iptv.handler.parser.Parser;
import io.quarkus.runtime.Quarkus;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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

        Multi.createFrom()
                .items(config.source().stream())
                .filter(StringUtils::isNotBlank)
                //获取解析器
                .onItem().transform(e -> Parser.getParser(e, config.parser()))
                //解析直播源
                .onItem().transform(e -> e.flatMap(parser -> parser.get().toUni()))
                //检测
                .onItem().transform(e -> e.flatMap(Checker::check))
                .flatMap(Uni::toMulti)
                .log()
                .wait();
    }
}