package dev.fordes.iptv.listener;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.ConvertWith;
import io.quarkus.runtime.configuration.NormalizeRootHttpPathConverter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

/**
 * @author Chengfs on 2024/5/15
 */
@Slf4j
@ApplicationScoped
public class AppLifecycleListener {

    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
    Integer port;

    @ConvertWith(NormalizeRootHttpPathConverter.class)
    @ConfigProperty(name = "quarkus.http.root-path", defaultValue = "/")
    String httpPath;

    @ConvertWith(NormalizeRootHttpPathConverter.class)
    @ConfigProperty(name = "quarkus.http.non-application-root-path", defaultValue = "q")
    String nonAppPath;

    @ConfigProperty(name = "quarkus.smallrye-openapi.path", defaultValue = " ")
    String openApiPath;

    @ConfigProperty(name = "quarkus.swagger-ui.path", defaultValue = " ")
    String swaggerUiPath;

    void onStart(@Observes StartupEvent ev) {

        String address = "http://localhost:" + port;
        log.info("Resource Endpoints: {}", address + httpPath);
        log.info("Open API UI: {}", address + Optional.of(swaggerUiPath)
                .filter(e -> !e.isBlank()).orElse(nonAppPath + "/swagger-ui"));
        log.info("Open API Schema document: {}", address + Optional.of(openApiPath)
                .filter(e -> !e.isBlank()).orElse(nonAppPath + "/openapi"));
    }


    void onStop(@Observes ShutdownEvent ev) {
        //TODO clean caches
    }
}