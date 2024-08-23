package dev.fordes.iptv.resource;

import dev.fordes.iptv.handler.checker.Checker;
import dev.fordes.iptv.handler.composer.Composer;
import dev.fordes.iptv.model.Channel;
import dev.fordes.iptv.model.Metadata;
import dev.fordes.iptv.model.domain.Code;
import dev.fordes.iptv.model.domain.R;
import dev.fordes.iptv.model.enums.SourceType;
import dev.fordes.iptv.scheduler.SourceSchedulerTask;
import dev.fordes.iptv.service.ChannelService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.net.URI;

import static dev.fordes.iptv.util.Constants.HTTP_PREFIX_REGEX;

/**
 * @author Chengfs on 2024/5/16
 */
@Slf4j
@Path("/channel")
public class ChannelResource {

    @Inject
    ChannelService channelService;

    @GET
    @Path("/")
    @Operation(summary = "通过URL获取频道信息")
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Channel.class)))
    public Uni<?> channel(@Valid @NotBlank @Pattern(regexp = HTTP_PREFIX_REGEX) @QueryParam("url") String url) {
        return channelService.detect(url)
                .map(R::ok)
                .onFailure().recoverWithItem(e -> {
                    log.error("url: {}, {}: ", url, e.getMessage(), e);
                    return R.of(Code.INTERNAL_SERVER_ERROR, e.getMessage());
                });
    }

    @GET
    @Path("/metadata")
    @Operation(summary = "通过URL获取频道元数据")
    @Parameter(name = "url", required = true)
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Metadata.class)))
    public Uni<?> metadata(@Valid @NotBlank @Pattern(regexp = HTTP_PREFIX_REGEX) @QueryParam("url") String url) {
        return channelService.metadata(url)
                .map(R::ok)
                .onFailure().recoverWithItem(e -> {
                    log.error("url: {}, {}: ", url, e.getMessage(), e);
                    return R.of(Code.INTERNAL_SERVER_ERROR, e.getMessage());
                });
    }

    @GET
    @Path("/test")
    @Operation(summary = "测试接口")
    @Parameter(name = "url", required = true)
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Metadata.class)))
    public Uni<?> test(@Valid @NotBlank @Pattern(regexp = HTTP_PREFIX_REGEX) @QueryParam("url") String url) {
        Multi<Channel> multi = Uni.createFrom().item(url)
                .onItem().transform(Unchecked.function(e -> {
                    Channel channel = new Channel();
                    channel.setUrl(URI.create(e).toURL());
                    return channel;
                }))
                .flatMap(Checker::check)
                .toMulti();

        Composer composer = Composer.getComposer(SourceType.M3U);
        return composer.apply(multi)
                .map(R::ok)
                .toUni();
    }

    @Inject
    SourceSchedulerTask source;

    @GET
    @Path("/test2")
    public String test2() {
        source.source();
        return null; //TODO replace this stub to something useful
    }
}