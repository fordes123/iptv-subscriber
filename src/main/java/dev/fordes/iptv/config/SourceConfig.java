package dev.fordes.iptv.config;

import dev.fordes.iptv.config.convert.FilePathConverter;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Chengfs on 2024/9/24
 */
@ConfigMapping(prefix = "tv-source")
public interface SourceConfig {

    Optional<@NotBlank String> cron();

    Optional<Set<@NotBlank String>> supplier();

    Optional<Set<Config>> consumer();

    interface Config {

        @WithConverter(FilePathConverter.class)
        String file();

        Set<@NotBlank String> filter();

        Map<String, Set<@NotBlank String>> group();
    }
}