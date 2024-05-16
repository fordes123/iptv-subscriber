package dev.fordes.iptv.model.enums.tag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum State {

    /**
     * 不可用
     */
    FAIL(-1),

    /**
     * 不健康<br/>
     * 不满足: HD 25帧 5M
     */
    UNHEALTHY(0),

    /**
     * 良好，介于 UNHEALTHY 和 EXCELLENT 之间
     */
    HEALTHY(1),

    /**
     * 极佳<br/>
     * 满足: FHD 60帧 8M
     */
    EXCELLENT(2)

    ,;

    private final Integer value;

}
