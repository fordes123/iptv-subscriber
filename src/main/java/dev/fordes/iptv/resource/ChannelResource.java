package dev.fordes.iptv.resource;

import dev.fordes.iptv.handler.checker.Checker;
import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.Metadata;
import dev.fordes.iptv.model.domain.Code;
import dev.fordes.iptv.model.domain.R;
import dev.fordes.iptv.model.vo.ChannelVO;
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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.net.URI;

/**
 * @author Chengfs on 2024/5/16
 */
@Slf4j
@Path("/channel")
public class ChannelResource {

    @GET
    @Path("/")
    @Operation(summary = "通过URL获取频道信息")
    public Uni<R<ChannelVO>> channel(@Valid @NotBlank @Pattern(regexp = "^https?:\\/\\/.+$")
                                     @Parameter(name = "url", required = true) @QueryParam("url") String url) {
        return Uni.createFrom().item(url)
                .onItem().transform(Unchecked.function(e -> {
                    Channel channel = new Channel();
                    channel.setUrl(URI.create(e).toURL());
                    return channel;
                }))
                .flatMap(Checker::check)
                .map(e -> R.ok(ChannelVO.of(e)))
                .onFailure()
                .recoverWithItem(e -> {
                    log.error("url: {}, {}: ", url, e.getMessage(), e);
                    return R.of(Code.INTERNAL_SERVER_ERROR, e.getMessage());
                });
    }

    @GET
    @Path("/metadata")
    @Operation(summary = "通过URL获取频道元数据")
    public Uni<R<Metadata>> metadata(@Valid @NotBlank @Pattern(regexp = "^https?:\\/\\/.+$")
                                     @Parameter(name = "url", required = true) @QueryParam("url") String url) {
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