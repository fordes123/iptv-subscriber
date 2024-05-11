package dev.fordes.iptv.config;

import io.smallrye.common.constraint.NotNull;
import io.smallrye.config.ConfigMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ConfigMapping(prefix = "iptv-subscriber")
public interface ISProperties {

    /**
     * epg 订阅
     */
    List<String> epg();

    /**
     * 直播源订阅
     */
    List<String> source();

    /**
     * 输出配置
     */
    List<OutputItem> output();

    /**
     * 解析器配置
     */
    Parser parser();

    interface OutputItem {

        @NotNull
        String file();

        Set<String> filter();

        Map<String, Set<String>> group();

    }

    interface Parser {

//        @WithDefault("8196")
        Integer readBufferSize();

//        @WithDefault("false")
        Boolean parseDomainAddr();
    }
}
