package dev.fordes.iptv.model.enums;

import io.vertx.mutiny.core.Vertx;
import lombok.Getter;

@Getter
public enum MutinyVertx {

    INSTANCE,
    ;

    private final Vertx vertx;

    MutinyVertx() {
        vertx = Vertx.vertx();
    }

}
