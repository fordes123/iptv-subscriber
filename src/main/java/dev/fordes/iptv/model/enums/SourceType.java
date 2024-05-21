package dev.fordes.iptv.model.enums;

import dev.fordes.iptv.util.FileSuffix;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum SourceType {

    M3U, GENERIC;

    public static Uni<SourceType> of(String path) {
        String suffix = FileSuffix.get(path);
        if (FileSuffix.M3U.equals(suffix) || FileSuffix.M3U8.equals(suffix)) {
            return Uni.createFrom().item(M3U);
        }
        return trySourceType(path);
    }

    public static Uni<SourceType> trySourceType(String path) {
        return Uni.createFrom().item(SourceType.GENERIC);
        //TODO
//        try {
//            Multi<SourceType> emitter1 = Multi.createFrom().emitter(emitter -> {
//                CommonUtils.httpGet(path, opt -> opt.setMaxChunkSize(1024), response -> {
//                    response.handler(buffer -> {
//                        String begin = buffer.toString(StandardCharsets.UTF_8);
//                        SourceType type = begin.startsWith(Constants.M3U_HEADER) ? M3U : GENERIC;
//                        emitter.emit(type);
//                        emitter.complete();
//                    });
//                });
//            });
//            return emitter1.toUni();
//        } catch (Exception e) {
//            log.severe(e.getMessage());
//            throw new RuntimeException("File format not recognized: " + path);
//        }
    }
}
