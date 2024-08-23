package dev.fordes.iptv.handler.parser;

import io.vertx.codegen.annotations.DataObject;
import lombok.Setter;

/**
 * @author Chengfs on 2024/8/20
 */
@Setter
@DataObject
public class ParseOptions implements Parser.Config {

    private Integer readBufferSize = 8196;
    private Boolean domainToHost = false;

    @Override
    public Integer readBufferSize() {
        return readBufferSize;
    }

    @Override
    public Boolean domainToHost() {
        return domainToHost;
    }
}