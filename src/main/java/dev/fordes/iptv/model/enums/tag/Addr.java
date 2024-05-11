package dev.fordes.iptv.model.enums.tag;

import dev.fordes.iptv.util.Constants;
import io.smallrye.common.constraint.NotNull;

import java.net.*;
import java.util.Arrays;

public enum Addr {

    IPV4, IPV6, DOMAIN;

    public static Addr fromURL(URL url) {
        try {
            InetAddress address = InetAddress.getByName(url.getHost());

            if (address instanceof Inet4Address) {
                return IPV4;
            } else if (address instanceof Inet6Address) {
                return IPV6;
            } else if (url.getHost().contains(Constants.DOT)) {
                return DOMAIN;
            }
        } catch (UnknownHostException ignored) {
        }
        throw new RuntimeException("unknown type");
    }

    public static Addr of(@NotNull String value) {
        final String data = value.toUpperCase();
        return Arrays.stream(Addr.values())
                .filter(addr -> addr.name().equals(data)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported Addr value: " + value));
    }
}
