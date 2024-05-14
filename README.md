# iptv-subscriber

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/iptv-subscriber-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and
  Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on
  it.
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus
  REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- YAML Configuration ([guide](https://quarkus.io/guides/config-yaml)): Use YAML to configure your Quarkus application
- Scheduler ([guide](https://quarkus.io/guides/scheduler)): Schedule jobs and tasks

## Provided Code

### YAML Config

Configure your application with YAML

[Related guide section...](https://quarkus.io/guides/config-reference#configuration-examples)

The Quarkus application configuration is located in `src/main/resources/application.yml`.

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

### 可用标签

> 标签格式 `<tagName>:<value>`

| tagName | value 预设                                                       |                          自定义                           | 说明          
|:--------|:---------------------------------------------------------------|:------------------------------------------------------:|:------------
| dpi     | `SD`(480) 、`HD`(720)、 `FHD`(1080)、 `UHD`(2160)、`FUHD`(4320)    |            `<number>` 视频分辨率，以横向像素为准，如 1080             | 分辨率         
| fps     | -                                                              |                    `<number>` 如 25                     | 帧率          
| rate    | -                                                              |              `<number>` 如 866, 单位: `kbps`              | 码率          
| codec   | `h264`、`hevc`、`h265`、`av1` 等                                   | `<string>` 合法且受支持的编码名，参见 [FFmpeg](https://ffmpeg.org/) | 视频编码        
| name    | -                                                              |                   `<string>` 频道名通配符                    | 频道名         
| addr    | `IPV4`、`IPV6` 、`DOMAIN`                                        |                           -                            | 请求地址类型      
| speed   | -                                                              |              `<number>` 如 20, 单位: `mbps`               | 理论播放速率      
| status  | `fail`(不可用) 、`unhealthy`(不健康) 、`healthy`(良好) 、 `excellent`(极佳) |                           -                            | 程序评估状态，仅供参考 