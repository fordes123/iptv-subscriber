package dev.fordes.iptv.scheduler;

import dev.fordes.iptv.config.ISProperties;
import dev.fordes.iptv.handler.checker.Checker;
import dev.fordes.iptv.handler.parser.Parser;
import dev.fordes.iptv.model.Channel;
import io.quarkus.runtime.Quarkus;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logmanager.Logger;

/**
 * @author Chengfs on 2023/12/25
 */
@ApplicationScoped
public class SourceSchedulerTask {

    @Inject
    ISProperties config;

    private final Vertx vertx;

    public SourceSchedulerTask(Vertx vertx) {
        this.vertx = vertx;
    }

    private static final Logger log = Logger.getLogger(SourceSchedulerTask.class.getName());

//        @Scheduled(cron = "{scheduled.source: disabled}")
    public Multi<Channel> source() {
        log.severe("output is empty");
        if (config.output().isEmpty()) {
            log.severe("output is empty");
            Quarkus.asyncExit();
        }
        return Multi.createFrom()
                .items(config.source().stream())
                .filter(StringUtils::isNotBlank)
                .flatMap(e -> Parser.getParser(e, config.parser())
                        .onItem().transformToMulti(Parser::get))
                .flatMap(e -> Checker.check(e, e.getUrl())
                        .toMulti());

    }
}