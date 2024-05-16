package dev.fordes.iptv.model.enums.tag;

import io.smallrye.common.constraint.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.Comparator;

@Getter
@RequiredArgsConstructor
public enum DPI {

    SD(720, 480, null),
    HD(1280, 720, null),
    FHD(1920, 1080, null),
    UHD(3840, 2160, "4K"),
    FUHD(7680, 4320, "8K"),
    ;

    private final Integer width;
    private final Integer height;
    private final String alias;

    public static DPI valueOf(int width) {
        return Arrays.stream(DPI.values())
                .sorted(Comparator.comparingInt(DPI::getWidth).reversed())
                .filter(dpi -> width >= dpi.getWidth())
                .findFirst()
                .orElse(SD);
    }


    public static Integer of(@NotNull String value) {
        if (NumberUtils.isCreatable(value)) {
            return NumberUtils.createInteger(value);
        } else {
            final String data = value.toUpperCase();
            return Arrays.stream(DPI.values())
                    .filter(dpi -> dpi.name().equalsIgnoreCase(data) ||
                            data.equalsIgnoreCase(dpi.alias)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported DPI value: " + value))
                    .width;
        }

    }
}
