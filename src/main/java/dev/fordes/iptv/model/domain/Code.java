package dev.fordes.iptv.model.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum Code {

    OK(200, HttpResponseStatus.OK, "成功"),

    INTERNAL_SERVER_ERROR(500, HttpResponseStatus.INTERNAL_SERVER_ERROR, HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase()),

    BAD_REQUEST(400, HttpResponseStatus.BAD_REQUEST, HttpResponseStatus.BAD_REQUEST.reasonPhrase()),
    MISSING_PARAMETER(40001, HttpResponseStatus.BAD_REQUEST, "缺少必要参数"),
    INVALID_PARAMETER(40002, HttpResponseStatus.BAD_REQUEST, "参数错误"),

    UNAUTHORIZED(401, HttpResponseStatus.UNAUTHORIZED, HttpResponseStatus.UNAUTHORIZED.reasonPhrase()),
    USER_OR_PASSWORD_ERROR(40101, HttpResponseStatus.UNAUTHORIZED, "用户名或密码错误"),
    INVALID_TOKEN(40102, HttpResponseStatus.UNAUTHORIZED, "无效的凭据"),

    FORBIDDEN(403, HttpResponseStatus.FORBIDDEN, HttpResponseStatus.FORBIDDEN.reasonPhrase()),
    NOT_FOUND(404, HttpResponseStatus.NOT_FOUND, HttpResponseStatus.NOT_FOUND.reasonPhrase()),


    ;

    @JsonValue
    public final int value;
    public final HttpResponseStatus status;
    public final String message;


    public static Code of(int code) {
        return Arrays.stream(values())
                .filter(e -> e.value == code)
                .findFirst()
                .orElse(INTERNAL_SERVER_ERROR);
    }
}