package dev.fordes.iptv.handler.parser;


import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.util.Constants;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chengfs on 2023/12/25
 */
@Slf4j
public class M3uParser extends Parser {

    private static final Pattern pattern = Pattern.compile("([a-z,A-Z,\\-]+)=\\\"(.*?)\\\"");

    public M3uParser(Config config, String fileName) {
        super(config, fileName);
    }

    /**
     * 解析 m3u8<br/>
     * <br/>
     * 由于 m3u8 以两行为一组，形如:
     * <pre>
     *     #EXTINF:-1,CCTV-1
     *     http://live.example.com/1/live.m3u8
     * </pre>
     * <p>
     * 当最后一行为 #EXTINF 时，表示该组被切断<br/>
     * 将其返回，后面会自动追加到下一批数据开头
     *
     * @param lines    源内容
     * @param consumer 结果处理器
     */
    @Override
    byte[] parse(List<String> lines, Consumer<Channel> consumer) {
        byte[] residue = null;

        String last = lines.get(lines.size() - 1);
        if (last.startsWith(Constants.SHARP)) {
            residue = last.getBytes(StandardCharsets.UTF_8);
            lines.removeLast();
        }

        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            String ext = null;
            while (line.startsWith(Channel.EXT_INF) && iterator.hasNext()) {
                ext = line;
                line = iterator.next();
            }
            if (ext != null) {
                try {
                    consumer.accept(parse(line, ext));
                } catch (RuntimeException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return residue;
    }

    public Channel parse(String line, String ext) {
        try {
            String[] parts = ext.split(Constants.COMMA);
            String infoPart = parts[0];
            String namePart = parts[1];

            // 解析EXTINF部分
            Map<String, String> infoMap = new HashMap<>();
            Matcher matcher = pattern.matcher(infoPart);
            while (matcher.find()) {
                infoMap.put(matcher.group(1), matcher.group(2));
            }

            URL url = URI.create(line).toURL();

            Channel channel = new Channel();
            channel.setExtInf(NumberUtils.createLong(StringUtils.substringBetween(infoPart, Constants.EXTINF_PREFIX, Constants.WHITESPACE)));
            channel.setTvgId(infoMap.get(Channel.TVG_ID));
            channel.setTvgName(infoMap.get(Channel.TVG_NAME));
            channel.setTvgLogo(infoMap.get(Channel.TVG_LOGO));
            channel.setGroupTitle(infoMap.get(Channel.GROUP_TITLE));
            channel.setDisplayName(namePart);
            channel.setUrl(url);

            log.debug("channel parsed: {}", Json.encode(channel));
            return channel;
        } catch (Exception e) {
            throw new RuntimeException("parsing error:" + e.getMessage() + "\n line:" + line
                    + "\n ext:" + ext, e);
        }
    }
}