package dev.fordes.iptv.util;

import dev.fordes.iptv.model.enums.MutinyVertx;
import io.smallrye.mutiny.Multi;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.AsyncFile;
import io.vertx.mutiny.core.http.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Chengfs on 2024/5/7
 */
public class FileUtil {

    //全局临时文件路径
    public static final Path TEMP_DIR;

    static {
        TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir") + File.separator + "iptv-subscriber");
        try {
            if (!Files.exists(TEMP_DIR)) {
                Files.createDirectory(TEMP_DIR);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 从指定路径响应式读取文件，并自定义结果处理<br/>
     * 结果逐块处理，处理器可能被多次调用
     *
     * @param path              路径
     * @param optSupplier       {@link Supplier<OpenOptions>}
     * @param asyncFileConsumer 结果处理器
     */
    public static void read(String path, Supplier<OpenOptions> optSupplier, Consumer<AsyncFile> asyncFileConsumer) {
        MutinyVertx.INSTANCE.getVertx().fileSystem()
                .open(path, optSupplier.get())
                .subscribe().with(asyncFileConsumer);
    }

    /**
     * @see #read(String, Supplier, Consumer)
     */
    public static void read(String path, Consumer<AsyncFile> asyncFileConsumer) {
        read(path, () -> new OpenOptions().setRead(true), asyncFileConsumer);
    }

    /**
     * 从指定路径响应式读取文件，将结果转换为 String 并返回
     *
     * @param path 文件路径
     * @return {@link Multi<String}
     * @see #read(String, Consumer)
     */
    public static Multi<String> read(String path) {
        return MutinyVertx.INSTANCE.getVertx().fileSystem().readFile(path)
                .onItem().transform(buffer -> buffer.toString(StandardCharsets.UTF_8)).toMulti();
    }


    /**
     * 以指定扩展名创建临时文件
     *
     * @param ext 扩展名
     * @return 临时文件
     */
    public static File createTempFile(String ext) {
        try {
            Path path = Files.createFile(Path.of(TEMP_DIR.toAbsolutePath() + File.separator + UUID.randomUUID() + "." + ext));
            return path.toFile();
        } catch (IOException e) {
            throw new RuntimeException("Can not create temp file: " + e.getMessage());
        }
    }

    /**
     * 尝试从 响应头或请求路径中获取文件扩展名并创建临时文件<br/>
     * 如获取不到，将以 .tmp 作为扩展名
     *
     * @param url    请求url
     * @param header 响应头集合
     * @return 文件名
     */
    public static File createTempFile(URL url, MultiMap header) {
        try {
            //默认从path中获取文件名
            String fileName = StringUtils.substringAfterLast(url.getPath(), Constants.SLASH);

            String ext = StringUtils.substringAfterLast(fileName, Constants.DOT);

            //获取不到，尝试从 Content-Disposition 中获取文件名
            if (ext == null && header.contains(HttpHeaders.CONTENT_DISPOSITION)) {
                String tempName = StringUtils.substringAfterLast(header.get(HttpHeaders.CONTENT_DISPOSITION),
                        "filename=");
                if (tempName != null) {
                    ext = StringUtils.substringAfterLast(URLDecoder.decode(tempName, StandardCharsets.UTF_8),
                            Constants.DOT);
                }
            }

            //获取不到，尝试从 Content-Type 中获取文件名
            if (ext == null && header.contains(HttpHeaders.CONTENT_TYPE)) {
                String contentType = header.get(HttpHeaders.CONTENT_TYPE);
                if (StringUtils.equalsAny(contentType, "application/x-mpegurl", "application/vnd.apple.mpegurl")) {
                    ext = "m3u8";
                }
            }

            return createTempFile(ext == null ? "tmp" : ext);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("File creation failure: " + e.getMessage());
        }
    }

    /**
     * 尝试从请求路径中获取文件扩展名并创建临时文件<br/>
     * 如获取不到，将以 .tmp 作为扩展名
     *
     * @param url 请求url
     * @return 文件名
     */
    public static File createTempFile(URL url) {
        return createTempFile(url, null);
    }

    /**
     * 将多行内容写入文件
     *
     * @param file    文件路径
     * @param content 内容
     */
    public static void write(String file, Multi<String> content) {
        final OpenOptions openOptions = new OpenOptions()
                .setCreate(true)
                .setTruncateExisting(true);

        MutinyVertx.INSTANCE.getVertx().fileSystem()
                .open(file, openOptions)
                .onItem().transformToUni(asyncFile ->
                        content.onItem().transformToUni(str -> {
                                    Buffer buffer = Buffer.buffer(str);
                                    return asyncFile.write(buffer)
                                            .onFailure().transform(t -> new RuntimeException("Write failed", t)); // Handle write errors
                                })
                                .concatenate()
                                .onFailure().recoverWithItem(t -> {
                                    // Error handling for individual write failures
                                    System.err.println("Error writing to file: " + t.getMessage());
                                    return null; // Continue with next items
                                })
                                .collect().asList() // Collect all writes to ensure completion
                                .onItem().transformToUni(ignored -> asyncFile.close()) // Close file after all writes are done
                )
                .subscribe().with(
                        ignored -> System.out.println("Write completed successfully"),
                        failure -> System.err.println("Write operation failed: " + failure.getMessage())
                );
    }
}