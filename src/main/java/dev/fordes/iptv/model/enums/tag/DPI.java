package dev.fordes.iptv.model.enums.tag;

import io.smallrye.common.constraint.NotNull;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;

public enum DPI {

    SD(480, null),
    HD(720, null),
    FHD(1080, null),
    UHD(2160, "4K"),
    FUHD(4320, "8K"),
    ;

    private final Integer value;
    private final String alias;

    DPI(Integer value, String alias) {
        this.value = value;
        this.alias = alias;
    }

    public static Integer of(@NotNull String value) {
        if (NumberUtils.isCreatable(value)) {
            return NumberUtils.createInteger(value);

        } else {
            final String data = value.toUpperCase();
            return Arrays.stream(DPI.values())
                    .filter(dpi -> dpi.name().equals(data) ||
                            data.equals(dpi.alias)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported DPI value: " + value))
                    .value;
        }

    }
}
