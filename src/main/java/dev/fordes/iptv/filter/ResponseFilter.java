package dev.fordes.iptv.filter;

import dev.fordes.iptv.model.domain.Code;
import dev.fordes.iptv.model.domain.R;
import dev.fordes.iptv.util.Constants;
import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Chengfs on 2024/5/16
 */
@Slf4j
@Provider
public class ResponseFilter implements ContainerResponseFilter {

    @ConfigProperty(name = "iptv-subscriber.debug", defaultValue = "false")
    boolean debug;

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        Object entity = Optional.ofNullable(response.getEntity()).orElse(R.ok());

        R<?> result;
        if (entity instanceof R<?> r) {
            result = r;
        } else if (entity instanceof ViolationReport report) {

            String msg = report.getViolations()
                    .stream()
                    .findFirst().map(e -> e.getField() + Constants.COLON + e.getMessage())
                    .orElse(Constants.EMPTY);
            result =R.of(Code.INVALID_PARAMETER, msg);
        } else {
            result = R.ok(entity);
        }

        if (!debug) {
            result.disableMsg();
        }
        response.setStatus(result.getCode().status.code());
        response.setEntity(result);
    }
}