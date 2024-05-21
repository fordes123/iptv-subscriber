package dev.fordes.iptv.handler.composer;

import dev.fordes.iptv.model.Channel;
import io.smallrye.mutiny.Multi;

/**
 * @author Chengfs on 2024/5/20
 */
public class GenericComposer implements Composer {

    @Override
    public Multi<String> apply(Multi<Channel> value) {
        return Multi.createFrom().empty();
    }
}