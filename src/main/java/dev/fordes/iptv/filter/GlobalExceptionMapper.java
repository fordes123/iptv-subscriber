package dev.fordes.iptv.filter;

import dev.fordes.iptv.model.domain.Code;
import dev.fordes.iptv.model.domain.R;
import io.quarkus.runtime.util.ExceptionUtil;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable e) {

        if (e instanceof NullPointerException) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(R.fail("空指针异常")).build();
        }

        if (e instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(R.of(Code.INVALID_PARAMETER, "非法参数异常")).build();
        }

        if (e instanceof IndexOutOfBoundsException) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(R.of(Code.INTERNAL_SERVER_ERROR, "索引越界异常")).build();
        }

        if (e instanceof ReflectiveOperationException) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(R.of(Code.INTERNAL_SERVER_ERROR, "反射操作异常")).build();
        }

        log.error(ExceptionUtil.generateStackTrace(e));
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(R.fail()).build();
    }
}