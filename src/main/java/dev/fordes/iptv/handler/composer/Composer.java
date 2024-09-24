package dev.fordes.iptv.handler.composer;

import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.enums.SourceType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.util.Objects;
import java.util.function.Function;

public abstract class Composer implements Function<Multi<Channel>, Multi<String>> {

    public abstract String compose(Channel value);

    @Override
    public Multi<String> apply(Multi<Channel> value) {
        return value.onItem().transform(this::compose).filter(Objects::nonNull);
    }

    public final Uni<String> single(Uni<Channel> value) {
        return value.onItem().transform(this::compose);
    }

    public static Composer getComposer(SourceType type) {
        return switch (type) {
            case M3U -> new M3UComposer();
            case GENERIC -> new GenericComposer();
        };
    }
}
