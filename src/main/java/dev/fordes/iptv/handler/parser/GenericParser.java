package dev.fordes.iptv.handler.parser;


import dev.fordes.iptv.config.ISProperties;
import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logmanager.Logger;

import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Chengfs on 2023/12/25
 */
public class GenericParser extends Parser {

    private final static Logger log = Logger.getLogger(GenericParser.class.getName());


    public GenericParser(ISProperties.Parser config, String fileName) {
        super(config, fileName);
    }

    @Override
    byte[] parse(List<String> lines, Consumer<Channel> consumer) {

        String group = null;
        for (String line : lines) {
            if (StringUtils.containsAny(line, Constants.HTTP_PREFIX, Constants.HTTPS_PREFIX)) {
                try {
                    consumer.accept(parse(line, group));
                } catch (RuntimeException e) {
                    log.severe(e.getMessage());
                }
            } else {
                group = line;
            }
        }
        return null;
    }

    public Channel parse(String line, String ext) {
        try {
            String name = StringUtils.substringBeforeLast(line, Constants.COMMA);
            String value = StringUtils.substringAfterLast(line, Constants.COMMA);
            URL url = new URL(value);
            Channel channel = new Channel();
            channel.setExtInf(-1L);
            channel.setTvgName(name);
            channel.setGroupTitle(StringUtils.substringBefore(ext, Constants.COMMA));
            channel.setDisplayName(name);
            channel.setUrl(url);
            return channel;
        } catch (Exception e) {
            throw new RuntimeException("parsing error:" + e.getMessage() + "\n line:" + line
                    + "\n ext:" + ext, e);
        }

    }
}