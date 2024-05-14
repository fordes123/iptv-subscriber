# iptv-subscriber

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## TODO

- [ ] 订阅和合并不同格式不同来源的 IPTV 源
- [ ] 全自动源检测，除可用性外，还包含画质、速度、帧率、码率等
- [ ] 自动去重、自动补全节目单、频道标志
- [ ] 支持 IPV4/IPV6 网络、HTTP/HTTPS 协议、M3U/TXT 格式
- [ ] 高度自定义配置，根据各种条件对频道进行分类筛选
- [ ] 定时处理自动生成订阅链接，提供 HTTP 接口，快捷高效

## 标签

在输出配置中，由多个标签组成过滤项，帮助我们简单的对频道进行筛选和分类，
它的格式为：`<tagName>:<value>`，下面是一份示例的输出配置：  

```
output:
  - file: cctv-ipv4.m3u
    filter:
      - addr:IPV4    # 地址为 ipv4
      - dpi:1080 # 分辨率不低于 1080P
      - fps:30   # 帧率不低于 30
    group:
      央视:
        - name:/.*cctv.*/
      卫视:
        - name:/.*卫视.*/
```

在如上配置中，通过标签，将地址为IPV4、分辨率1080、帧率30的频道分配至 `cctv-ipv4.m3u` 文件中，
同时，根据正则表达式匹配频道名将它们分为 `央视` 和 `卫视` 两个分组。更多关于标签的信息如下表：

| 标签   | 名称 (tagName) | 预设值 (value) | 自定义 (value) | 示例/说明
|:-----|:------|:-------|:----- |:------|
| 分辨率  | `dpi` | `SD`(480) 、`HD`(720)、 `FHD`(1080)、 `UHD`(2160)、`FUHD`(4320) | `<number>` | 视频分辨率，以横向像素为准
| 帧率   | `fps` | - | `<number>` | 视频帧率
| 码率   | `rate` | - | `<number>` | 视频码率，单位: `kbps`
| 视频编码 | `codec` | `h264`、`hevc`、`h265`、`av1` ... | `<string>` | 合法且受支持的编码名，参见 [FFmpeg](https://ffmpeg.org/)
| 频道名 | `name` | - | `<string>` | 使用通配符号或以`/`开头和结尾的正则
| 地址类型 | `addr` | `IPV4`、`IPV6` 、`DOMAIN` | - | 默认不会将域名解析为IP
| 速度 | `speed` | - | `<number>` | 视频播放速度，单位: `Mbps`
| 状态 | `state` | `fail` 、`unhealthy` 、`healthy` 、 `excellent` | - | 720P 25帧 8M (全部满足)认定为良好，否则为不健康；1080P 60帧 10M 及以上为极佳

> - `<number>` 类型标签全部以 **大于等于** 模式匹配  
> - 多个标签之间为 `且` 关系，即必须全部满足