package dev.fordes.iptv.handler.composer;

import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.util.Constants;
import io.smallrye.mutiny.Multi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static dev.fordes.iptv.util.Constants.LF;
import static dev.fordes.iptv.util.Constants.M3U_HEADER;

/**
 * @author Chengfs on 2024/5/20
 */
@Slf4j
public class M3UComposer extends Composer {

    @Override
    public String compose(Channel value) {
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.EXTINF_PREFIX).append(value.getExtInf());

        Optional.ofNullable(value.getGroupTitle()).filter(StringUtils::isNotBlank)
                .ifPresent(groupTitle -> builder.append(String.format(" %s=\"%s\"", Channel.GROUP_TITLE, groupTitle)));

        Optional.ofNullable(value.getTvgId()).filter(StringUtils::isNotBlank)
                .ifPresent(tvgId -> builder.append(String.format(" %s=\"%s\"", Channel.TVG_ID, tvgId)));

        Optional.ofNullable(value.getTvgName()).filter(StringUtils::isNotBlank)
                .ifPresent(tvgName -> builder.append(String.format(" %s=\"%s\"", Channel.TVG_NAME, tvgName)));

        Optional.ofNullable(value.getTvgLogo()).filter(StringUtils::isNotBlank)
                .ifPresent(tvgLogo -> builder.append(String.format(" %s=\"%s\"", Channel.TVG_LOGO, tvgLogo)));

        Optional.ofNullable(value.getDisplayName()).filter(StringUtils::isNotBlank)
                .ifPresent(displayName -> builder.append(String.format(" ,%s", displayName)));

        builder.append(Constants.LF).append(value.getUrl()).append(Constants.LF);

        log.debug("compose m3u8: {}", builder);
        return builder.toString();
    }

    @Override
    public Multi<String> apply(Multi<Channel> value) {
        return Multi.createBy().merging().streams(Multi.createFrom().items(M3U_HEADER, LF), super.apply(value));
    }
}