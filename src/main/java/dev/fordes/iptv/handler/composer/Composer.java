package dev.fordes.iptv.handler.composer;

import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.enums.SourceType;
import io.smallrye.mutiny.Multi;

import java.util.function.Function;

public interface Composer extends Function<Multi<Channel>, Multi<String>> {

    @Override
    Multi<String> apply(Multi<Channel> value);

    static Composer getComposer(SourceType type) {
        return switch (type) {
            case M3U -> new M3UComposer();
            case GENERIC -> new GenericComposer();
        };
    }
}
