package dev.fordes.iptv.model.domain;

import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

@Getter
@Schema(name = "R" , description = "全局响应体")
public class R<T> implements Serializable {

    @Schema(description = "状态码", required = true, type = SchemaType.INTEGER, implementation = Code.class)
    private final Code code;

    @Schema(description = "响应消息")
    private String msg;

    @Schema(description = "响应数据")
    private final T data;

    @Serial
    private static final long serialVersionUID = 1L;

    public R(Code code, String msg, T data) {
        this.code = code;
        this.data = data;
        this.msg = Optional.ofNullable(msg).filter(s -> !s.isBlank()).orElse(code.message);
    }

    public void disableMsg() {
        this.msg = null;
    }

    public static <T> R<T> of(Code code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    public static <T> R<T> of(Code code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> of(Code code) {
        return R.of(code, null);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(Code.OK, msg, data);
    }

    public static <T> R<T> ok(T data) {
        return R.ok(null, data);
    }

    public static <T> R<T> ok() {
        return R.ok(null);
    }

    public static <T> R<T> fail(String msg, T data) {
        return R.of(Code.INTERNAL_SERVER_ERROR, msg, data);
    }

    public static <T> R<T> fail(String msg) {
        return R.fail(msg, null);
    }

    public static <T> R<T> fail() {
        return R.fail(null);
    }
}