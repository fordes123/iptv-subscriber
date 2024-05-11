package dev.fordes.iptv;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ExampleResourceTest {
    @Test
    void testHelloEndpoint() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(is("Hello from Quarkus REST"));
    }

//    @Test
//    void testConfig() throws MalformedURLException {
//        final Channel channel = new Channel();
//        channel.setUrl(URI.create("http://hls.weathertv.cn/tslslive/qCFIfHB/hls/live_sd.m3u8").toURL());
//
//        Checker.check(channel, channel.getUrl());
//        System.out.println(1);
//    }

}