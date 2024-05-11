package dev.fordes.iptv.util;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logmanager.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * @author Chengfs on 2023/12/29
 */
public class CommonUtils {

    public static final Logger log = Logger.getLogger(CommonUtils.class.getName());


    public static double calculateSpeed(long t, long u) {
        if (t == 0 || u == 0) {
            return 0D;
        }
        double timeInSeconds = (double) t / 1000;
        double speedInMbps = ((double) u / timeInSeconds) / (1 << 20); // 1 Mbps = 1,000,000 bps
        return BigDecimal.valueOf(speedInMbps)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }


    public static URL removeFragment(URL url) {
        try {
            return new URL(removeFragment(url.toString()));
        } catch (MalformedURLException e) {
            throw new RuntimeException();
        }
    }

    public static String removeFragment(String urlStr) {
        int hashIndex = urlStr.indexOf(Constants.SHARP_C);
        return hashIndex == -1 ? urlStr : urlStr.substring(0, hashIndex);
    }

    public static boolean isM3UByMime(String mime) {
        return "application/vnd.apple.mpegurl".equals(mime) || "application/x-mpegurl".equals(mime);
    }

    public static URL resolveURL(URL base, String path) {
        try {
            URL subUrl;
            if (StringUtils.startsWithAny(Constants.HTTP_PREFIX, Constants.HTTPS_PREFIX)) {
                subUrl = URI.create(CommonUtils.removeFragment(path)).toURL();
            } else {
                subUrl = base.toURI().resolve(path.trim()).toURL();
            }
            return subUrl;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}