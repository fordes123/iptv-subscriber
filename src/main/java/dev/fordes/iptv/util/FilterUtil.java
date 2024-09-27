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
import java.util.Set;

/**
 * @author Chengfs on 2024/5/17
 */
@Slf4j
public class FilterUtil {

    public interface Flag {
        String DPI = "DPI";
        String FPS = "FPS";
        String RATE = "RATE";
        String CODEC = "CODEC";
        String NAME = "NAME";
        String ADDR = "ADDR";
        String SPEED = "SPEED";
        String STATE = "STATE";
    }

    /**
     * 判断 {@link Channel} 是否匹配多个标签
     *
     * @param filters 过滤标签集合 {@link Optional<Set<String>>}
     * @param channel {@link Channel}
     * @return 过滤标签为空或全部匹配时为 true
     */
    public static boolean match(Optional<Set<String>> filters, Channel channel) {
        return filters.filter(list -> !list.isEmpty())
                .map(list -> list.stream().allMatch(item -> match(item, channel)))
                .orElse(true);
    }

    /**
     * 判断 {@link Channel} 是否匹配指定的标签
     *
     * @param tag     {@pre <tagName>:<tagValue>}
     * @param channel {@link Channel}
     * @return {@link boolean}
     */
    public static boolean match(String tag, Channel channel) {
        String[] split = StringUtils.split(tag, Constants.COLON);
        if (split.length == 2 && StringUtils.isNotBlank(split[0]) && StringUtils.isNotBlank(split[1])) {
            return Optional.of(match(Tuple2.of(split[0], split[1]), channel))
                    .filter(Boolean::booleanValue)
                    .map(matched -> {
                        log.debug("channel: {}({}) => matched tag: {}", channel.absName(), channel.getUrl(), tag);
                        return true;
                    }).orElse(false);
        }
        log.warn("Invalid filter flag: {}", tag);
        return false;
    }

    /**
     * 判断 {@link Channel} 是否匹配指定的标签
     *
     * @param tuple   Tag {@link Tuple2<String, String>}
     * @param channel {@link Channel}
     * @return {@link boolean}
     */
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