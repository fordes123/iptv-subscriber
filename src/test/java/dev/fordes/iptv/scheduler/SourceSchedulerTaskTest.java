package dev.fordes.iptv.scheduler;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@QuarkusTest
class SourceSchedulerTaskTest {

    @Inject
    SourceSchedulerTask sourceSchedulerTask;

    @Test
    void testSource() throws Exception {
        Assertions.assertTimeout(Duration.ofMinutes(10), sourceSchedulerTask::source);
    }
}
