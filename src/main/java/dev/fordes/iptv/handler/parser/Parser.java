package dev.fordes.iptv.handler.parser;


import dev.fordes.iptv.config.ISProperties;
import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.enums.SourceType;
import dev.fordes.iptv.util.Constants;
import dev.fordes.iptv.util.FileUtil;
import dev.fordes.iptv.util.HttpUtil;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public abstract class Parser implements Supplier<Multi<Channel>> {

    protected final String path;
    protected final ISProperties.Parser config;
    private Buffer cache = Buffer.buffer();
    private static final Logger log = Logger.getLogger(Parser.class.getName());

    public Parser(ISProperties.Parser config, String path) {
        this.config = config;
        this.path = path;
    }

    @Override
    public final Multi<Channel> get() {
        return Multi.createFrom().emitter(emitter -> {
                    if (StringUtils.startsWithAny(this.path, Constants.HTTP_PREFIX,
                            Constants.HTTPS_PREFIX)) {
                        HttpUtil.get(this.path,true, opt ->
                                        opt.setMaxChunkSize(config.readBufferSize()),
                                response -> response.handler(buffer -> {

                                    Buffer data;
                                    if (cache != null && cache.length() > 0) {
                                        data = Buffer.buffer(cache.length() + buffer.length())
                                                .appendBuffer(cache)
                                                .appendBuffer(buffer);
                                        cache = Buffer.buffer();
                                    } else {
                                        data = buffer;
                                    }
                                    parse(data, false, emitter::emit);
                                }).endHandler(() -> {
                                    Optional.of(cache).filter(i -> i.length() > 0)
                                            .ifPresent(i -> parse(i, true, emitter::emit));
                                    emitter.complete();
                                }).exceptionHandler(emitter::fail));
                    } else {
                        FileUtil.read(this.path, file -> file
                                .setReadBufferSize(config.readBufferSize())
                                .handler(buffer -> {
                                    Optional.of(cache).filter(i -> i.length() > 0).ifPresent(i -> {
                                        buffer.appendBuffer(i, 0, i.length());
                                        i = Buffer.buffer();
                                    });
                                    parse(buffer, false, emitter::emit);
                                }).endHandler(() -> {
                                    Optional.of(cache).filter(i -> i.length() > 0)
                                            .ifPresent(i -> parse(i, true, emitter::emit));

                                    file.close();
                                    emitter.complete();
                                }).exceptionHandler(emitter::fail));
                    }
                }
        );
    }

    /**
     * 实现此方法，解析源内容为 Channel
     *
     * @param lines    源内容
     * @param consumer 结果处理器
     */
    abstract byte[] parse(List<String> lines, Consumer<Channel> consumer);

    /**
     * 解析从 buffer 中得到的数据<br/>
     * 由于每次读取到 buffer 为定长内容，需要将不足一行的内容加入 cache 中暂存<br/>
     * 在下次调用本方法之前，cache 内容会被追加至 buffer 头部
     *
     * @param buffer   数据
     * @param consumer 结果处理器
     */
    protected void parse(Buffer buffer, boolean isLast, Consumer<Channel> consumer) {
        List<String> lines = new ArrayList<>();
        Buffer line = Buffer.buffer();

        for (byte b : buffer.getBytes()) {
            if (b == Constants.LF_C) {
                if (line.length() == 0) {
                    cache.appendBytes(new byte[]{b});
                    continue;
                }
                lines.add(line.toString(StandardCharsets.UTF_8));
                line = Buffer.buffer();
            } else if (b != Constants.CR_C) {
                line.appendBytes(new byte[]{b});
            }
        }

        Optional.of(line).filter(s -> s.length() > 0 && isLast)
                .ifPresent(c -> lines.add(c.toString(StandardCharsets.UTF_8)));
        Optional.of(lines).filter(s -> !s.isEmpty())
                .flatMap(s -> Optional.ofNullable(parse(s, consumer)).filter(e -> e.length > 0))
                .ifPresent(residue -> cache.appendBytes(residue).appendBytes(new byte[]{Constants.LF_C}));
        Optional.of(line).filter(s -> s.length() > 0 && !isLast).ifPresent(s -> cache.appendBuffer(s));
    }

    /**
     * 根据文件和配置获取解析器
     *
     * @param filePath 文件路径
     * @param config   解析配置 {@link ISProperties.Parser}
     * @return 解析器
     */
    public static Uni<Parser> getParser(String filePath, ISProperties.Parser config) {
        return SourceType.of(filePath)
                .map(type -> switch (type) {
                    case M3U -> new M3uParser(config, filePath);
                    case GENERIC -> new GenericParser(config, filePath);
                });
    }
}
