package dev.fordes.iptv.util;

import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.enums.tag.Addr;
import dev.fordes.iptv.model.enums.tag.DPI;
import dev.fordes.iptv.model.enums.tag.State;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Optional;

/**
 * @author Chengfs on 2024/5/17
 */
@Slf4j
public class FilterUtil {

    public static class Flag {
        public static final String DPI = "DPI";
        public static final String FPS = "FPS";
        public static final String RATE = "RATE";
        public static final String CODEC = "CODEC";
        public static final String NAME = "NAME";
        public static final String ADDR = "ADDR";
        public static final String SPEED = "SPEED";
        public static final String STATE = "STATE";
    }

    public static boolean match(String flag, Channel channel) {
        String[] split = StringUtils.split(flag, Constants.COLON);
        if (split.length == 2 && StringUtils.isNotBlank(split[0]) && StringUtils.isNotBlank(split[1])) {

            return Optional.of(match(Tuple2.of(split[0], split[1]), channel))
                    .filter(Boolean::booleanValue)
                    .map(matched -> {
                        log.debug("channel: {}({}) => matched tag: {}", channel.absName(), channel.getUrl(), flag);
                        return true;
                    }).orElse(false);
        }
        log.warn("Invalid filter flag: {}", flag);
        return false;
    }

    public static boolean match(Tuple2<String, String> tuple, Channel channel) {
        return switch (tuple.getItem1().toUpperCase()) {
            case Flag.DPI -> {
                Integer i = DPI.of(tuple.getItem2());
                yield channel.getMetadata().getImageWidth() >= i;
            }
            case Flag.FPS -> {
                Integer i = NumberUtils.createInteger(tuple.getItem2());
                yield channel.getMetadata().getFramerate() >= i;
            }
            case Flag.RATE -> {
                Integer i = NumberUtils.createInteger(tuple.getItem2());
                yield channel.getMetadata().getBitrate() >= i;
            }
            case Flag.CODEC -> tuple.getItem2().equalsIgnoreCase(channel.getMetadata().getVideoCodec());
            case Flag.NAME -> {
                String name = channel.absName();
                if (tuple.getItem2().startsWith("/") && tuple.getItem2().endsWith("/")) {
                    //正则
                    String reg = StringUtils.substringBetween(tuple.getItem2(), "/", "/");
                    yield name != null && name.matches(reg);
                } else {
                    yield name != null && name.contains(tuple.getItem2());
                }
            }
            case Flag.ADDR -> Addr.of(tuple.getItem2()).equals(channel.getMetadata().getType());
            case Flag.SPEED -> {
                Double i = NumberUtils.createDouble(tuple.getItem2());
                yield channel.getMetadata().getSpeed() >= i;
            }
            case Flag.STATE -> {
                State state = channel.getMetadata().getState();
                State target = State.valueOf(tuple.getItem2().toUpperCase());
                yield state.getValue() >= target.getValue();
            }
            default -> false;
        };
    }
}