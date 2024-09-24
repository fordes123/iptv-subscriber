package dev.fordes.iptv.config;

import dev.fordes.iptv.handler.parser.Parser;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * @author Chengfs on 2024/9/24
 */
@ConfigMapping(prefix = "server.parser")
public interface ParserConfig extends Parser.Config {

    @WithDefault("info")
    String logLevel();

}