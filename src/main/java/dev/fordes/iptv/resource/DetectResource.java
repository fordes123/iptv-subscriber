package dev.fordes.iptv.resource;

import dev.fordes.iptv.handler.checker.Checker;
import dev.fordes.iptv.model.Metadata;
import dev.fordes.iptv.model.domain.Code;
import dev.fordes.iptv.model.domain.R;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;

/**
 * @author Chengfs on 2024/5/16
 */
@Slf4j
@Tag(name = "检测")
@Path("/detect")
public class DetectResource {

    @GET
    @Path("/metadata")
    @Operation(summary = "检测频道元数据")
    public Uni<R<Metadata>> metadata(@Valid @NotBlank @Pattern(regexp = "^https?:\\/\\/.+$")
                                     @QueryParam("url") String url) {
        return Uni.createFrom().item(url)
                .map(Unchecked.function(e -> URI.create(e).toURL()))
                .flatMap(Checker::detectMetadata)
                .map(R::ok)
                .onFailure()
                .recoverWithItem(e -> {
                    log.error("url: {}, {}: ", url, e.getMessage(), e);
                    return R.of(Code.INTERNAL_SERVER_ERROR, e.getMessage());
                });
    }
}