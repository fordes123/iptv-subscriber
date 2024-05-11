package dev.fordes.iptv.model.enums;

import io.vertx.mutiny.core.Vertx;

public enum MutinyVertx {

    INSTANCE,
    ;

    private final Vertx vertx;

    MutinyVertx() {
        vertx = Vertx.vertx();
    }

    public Vertx getVertx() {
        return vertx;
    }
}
