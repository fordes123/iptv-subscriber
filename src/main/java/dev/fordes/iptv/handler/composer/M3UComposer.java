package dev.fordes.iptv.handler.composer;

import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.util.Constants;
import io.smallrye.mutiny.Multi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * @author Chengfs on 2024/5/20
 */
@Slf4j
public class M3UComposer implements Composer {

    @Override
    public Multi<String> apply(Multi<Channel> value) {
        return value.onItem().transform(channel -> {
            StringBuilder builder = new StringBuilder();
            builder.append(Constants.EXTINF_PREFIX)
                    .append(channel.getExtInf());

            Optional.ofNullable(channel.getTvgName()).filter(StringUtils::isNotBlank)
                    .ifPresent(tvgName -> builder.append(String.format(" %s=\"%s\"", Channel.TVG_NAME, tvgName)));

            Optional.ofNullable(channel.getTvgLogo()).filter(StringUtils::isNotBlank)
                    .ifPresent(tvgLogo -> builder.append(String.format(" %s=\"%s\"", Channel.TVG_LOGO, tvgLogo)));

            Optional.ofNullable(channel.getGroupTitle()).filter(StringUtils::isNotBlank)
                    .ifPresent(groupTitle -> builder.append(String.format(" %s=\"%s\"", Channel.GROUP_TITLE, groupTitle)));

            Optional.ofNullable(channel.getDisplayName()).filter(StringUtils::isNotBlank)
                    .ifPresent(displayName -> builder.append(String.format(" ,%s", displayName)));

            builder.append(Constants.LF).append(channel.getUrl()).append(Constants.LF);

            log.info("compose m3u8: {}", builder);
            return builder.toString();

        });
    }
}