package dev.fordes.iptv.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
public abstract class JSON {

    public static final String LEFT_BRACE = "{";
    public static final String RIGHT_BRACE = "}";
    public static final String LEFT_BRACKET = "[";
    public static final String RIGHT_BRACKET = "]";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    protected static final ObjectMapper mapper;
    protected static final SimpleModule dateModule;
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
    protected static final DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    protected static final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    protected static final DateTimeFormatter localTimeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT);

    private JSON() {
    }

    static {
        mapper = new ObjectMapper();

        dateModule = new SimpleModule();
        dateModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(localDateTimeFormatter));
        dateModule.addSerializer(LocalDate.class, new LocalDateSerializer(localDateFormatter));
        dateModule.addSerializer(LocalTime.class, new LocalTimeSerializer(localTimeFormatter));
        dateModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(localDateTimeFormatter));
        dateModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(localDateFormatter));
        dateModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(localTimeFormatter));
        mapper.registerModule(dateModule);

        mapper.setDateFormat(dateFormat);
        mapper.setTimeZone(dateFormat.getTimeZone());
        mapper.getDeserializationConfig().with(dateFormat);

        //空对象抛出异常
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //枚举类转换为字符串
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        //日期转换为时间戳
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        //未知属性抛出异常
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //接受空字符作为空对象
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        //是否包写出值为null的字段
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    /**
     * 判断对象是否可以转换为JSON字符串<br/>
     * 不能转换的类型：Date、CharSequence、Number、Boolean、LocalDateTime、LocalDate、LocalTime
     *
     * @param obj 目标对象
     * @return 是否可以转换
     */
    public static boolean canToJson(@Nonnull Object obj) {
        return !(obj instanceof CharSequence) && !(obj instanceof Number) && !(obj instanceof Boolean)
                && !(obj instanceof Date) && !(obj instanceof LocalDateTime) && !(obj instanceof LocalDate)
                && !(obj instanceof LocalTime);
    }

    /**
     * 判断字符串是否为JSON字符串<br/>
     * <p>
     * 标准: 被 {@code []} 或 {@code {}} 包裹，且可以被反序列化为使用一组JsonNode实例表示的树<br/>
     * <p/>
     *
     * @param json JSON字符串
     * @return 是否为JSON字符串
     */
    public static boolean isJson(@Nullable String json) {
        if (StringUtils.isNotBlank(json)) {
            String val = json.trim();
            if (val.startsWith(LEFT_BRACE) && val.endsWith(RIGHT_BRACE) ||
                    val.startsWith(LEFT_BRACKET) && val.endsWith(RIGHT_BRACKET)) {
                try {
                    mapper.readTree(val);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 对象转JSON字符串<br/>
     * 无法转换时返回null
     *
     * @param obj 目标对象
     * @return JSON字符串
     */
    public static @Nullable String toJsonStr(@Nullable Object obj) {
        if (obj != null && canToJson(obj)) {
            try {
                return mapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                log.warn("JsonProcessingException: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 对象转字符串<br/>
     * 对于可以转换的对象，将转换为JSON字符串，对于number、boolean、CharSequence、Date等类型，将转换为其toString()的值
     *
     * @param obj 目标对象
     * @return 字符串
     */
    public static @Nullable String toStr(@Nullable Object obj) {
        if (obj == null) {
            return null;
        } else if (canToJson(obj)) {
            return toJsonStr(obj);
        } else {
            if (obj instanceof Number || obj instanceof Boolean || obj instanceof CharSequence) {
                return obj.toString();
            } else if (obj instanceof Date d) {
                return dateFormat.format(d);
            } else if (obj instanceof LocalDateTime ldt) {
                return ldt.format(localDateTimeFormatter);
            } else if (obj instanceof LocalDate ld) {
                return ld.format(localDateFormatter);
            } else if (obj instanceof LocalTime lt) {
                return lt.format(localTimeFormatter);
            } else
                return obj.toString();
        }
    }

    /**
     * 对象转JSON字节数组
     *
     * @param obj 目标对象
     * @return JSON字节数组
     */
    public static @Nullable byte[] toJsonByte(@Nullable Object obj) {
        if (obj != null && canToJson(obj)) {
            try {
                return mapper.writeValueAsBytes(obj);
            } catch (JsonProcessingException e) {
                log.warn("JsonProcessingException: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * JSON字符串转换为List
     *
     * @param json  JSON字符串
     * @param clazz List中元素类型
     * @return {@link List <T>}
     */
    public static @Nullable <T> List<T> toList(@Nullable String json, @Nonnull Class<T> clazz) {
        if (isJson(json)) {
            try {
                JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
                return mapper.readValue(json, javaType);
            } catch (JsonProcessingException e) {
                log.warn("JsonProcessingException: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * JSON字符串转换为Set
     *
     * @param json  JSON字符串
     * @param clazz Set中元素类型
     * @return {@link Set <T>}
     */
    public static <T> @Nullable Set<T> toSet(@Nullable String json, @Nonnull Class<T> clazz) {
        if (isJson(json)) {
            try {
                JavaType javaType = mapper.getTypeFactory().constructParametricType(Set.class, clazz);
                return mapper.readValue(json, javaType);
            } catch (IOException e) {
                log.warn("JsonProcessingException: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * JSON字符串转换为对象，支持泛型
     * <pre>
     * 例: {@code List<Person> list = toBean(json, new TypeReference<List<Person>>() {});}
     * </pre>
     *
     * @param json          JSON字符串
     * @param typeReference {@link TypeReference} 类型参考子类，可以获取其泛型参数中的Type类型
     * @return 实体类对象
     */
    public static <T> @Nullable T toBean(@Nullable String json, @Nonnull TypeReference<T> typeReference) {
        if (isJson(json)) {
            try {
                return mapper.readValue(json, typeReference);
            } catch (Exception e) {
                log.warn("JsonProcessingException: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * JSON字符串转换为对象<br/>
     * 此方法不接受非json字符串，特殊类型请使用 {@link #convert(Object, Class)}
     *
     * @param json   JSON字符串
     * @param tClass 对象类型
     * @return 实体类对象
     */
    public static <T> @Nullable T toBean(@Nullable String json, @Nonnull Class<T> tClass) {
        if (isJson(json)) {
            try {
                return mapper.readValue(json, tClass);
            } catch (Exception e) {
                log.warn("JsonProcessingException: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 从JSON字符串中获取节点
     *
     * @param json JSON字符串
     * @param key  节点key
     * @return {@link JsonNode}
     */
    public static @Nullable JsonNode getNode(@Nullable String json, @Nullable String key) {
        if (StringUtils.isNotBlank(key) && isJson(json)) {
            try {
                return mapper.readTree(json).get(key);
            } catch (JsonProcessingException e) {
                log.warn("JsonProcessingException: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 从JSON字符串中获取key的值
     *
     * @param json          JSON字符串
     * @param key           节点key
     * @param typeReference {@link TypeReference} 类型参考子类，可以获取其泛型参数中的Type类型
     * @return 值
     */
    public static <T> @Nullable T get(@Nullable String json, @Nullable String key, @Nonnull TypeReference<T> typeReference) {
        if (StringUtils.isNotBlank(key) && isJson(json)) {
            try {
                JsonNode valueNode = getNode(json, key);
                if (valueNode != null) {
                    return mapper.readValue(valueNode.traverse(), typeReference);
                }
            } catch (IOException e) {
                log.warn("JsonProcessingException: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 从JSON字符串中获取key的值
     *
     * @param json  JSON字符串
     * @param key   节点key
     * @param clazz 值的类型
     * @return 值
     */
    public static <T> @Nullable T get(@Nullable String json, @Nullable String key, @Nonnull Class<T> clazz) {
        if (StringUtils.isNotBlank(key) && isJson(json)) {
            try {
                JsonNode valueNode = getNode(json, key);
                if (valueNode != null) {
                    return mapper.readValue(valueNode.traverse(), clazz);
                }
            } catch (IOException e) {
                log.warn("JsonProcessingException: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 从JSON字符串中获取值为List的key的值
     *
     * @param json  JSON字符串
     * @param key   节点key
     * @param clazz List元素类型
     * @return 值
     */
    public static <T> @Nullable List<T> getList(@Nullable String json, @Nullable String key, @Nonnull Class<T> clazz) {
        if (key != null && isJson(json)) {
            try {
                JsonNode valueNode = getNode(json, key);
                if (valueNode != null) {
                    JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
                    return mapper.readValue(valueNode.traverse(), javaType);
                }
            } catch (IOException e) {
                log.warn("JsonProcessingException: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 从JSON字符串中获取值为Set的key的值
     *
     * @param json  JSON字符串
     * @param key   节点key
     * @param clazz Set元素类型
     * @return 值
     */
    public static <T> @Nullable Set<T> getSet(@Nullable String json, @Nullable String key, @Nonnull Class<T> clazz) {
        if (StringUtils.isNotBlank(key) && isJson(json)) {
            try {
                JsonNode valueNode = getNode(json, key);
                if (valueNode != null) {
                    JavaType javaType = mapper.getTypeFactory().constructParametricType(Set.class, clazz);
                    return mapper.readValue(valueNode.traverse(), javaType);
                }
            } catch (IOException e) {
                log.warn("JsonProcessingException: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 从JSON字符串中获取值为String的key的值<br/>
     * 当值不为String时，返回值的原始字符串
     *
     * @param json JSON字符串
     * @param key  节点key
     * @return 值
     */
    public static @Nullable String getStr(@Nullable String json, @Nullable String key) {
        if (StringUtils.isNotBlank(key) && isJson(json)) {
            JsonNode valueNode = getNode(json, key);
            if (valueNode != null) {
                return valueNode.isTextual() ? valueNode.textValue() : valueNode.toString();
            }
        }
        return null;
    }

    /**
     * 从JSON字符串中获取值为Integer的key的值<br/>
     * 当值为 Number 时将尝试转换
     *
     * @param json JSON字符串
     * @param key  节点key
     * @return 值
     */
    public static @Nullable Integer getInt(@Nullable String json, @Nullable String key) {
        if (StringUtils.isNotBlank(key) && isJson(json)) {
            JsonNode valueNode = getNode(json, key);
            if (valueNode != null && valueNode.isNumber()) {
                return valueNode.intValue();
            }
        }
        return null;
    }

    /**
     * 从JSON字符串中获取值为Bool的key的值<br/>
     * 此方法不严格要求类型，会尝试将节点转换为Bool {@link JsonNode#asBoolean()}
     *
     * <pre>
     *     {
     *         "bool": true,
     *         "bool2": "true",
     *         "bool3": 1,
     *         "bool4": "1111"
     *     }
     *     --> getBool(json, "bool") = true
     *     --> getBool(json, "bool2") = true
     *     --> getBool(json, "bool3") = true
     *     --> getBool(json, "bool4") = false
     * </pre>
     *
     * @param json JSON字符串
     * @param key  节点key
     * @return 值
     */
    public static boolean getBool(@Nullable String json, @Nullable String key) {
        if (StringUtils.isNotBlank(key) && isJson(json)) {
            JsonNode valueNode = getNode(json, key);
            if (valueNode != null) {
                return valueNode.asBoolean();
            }
        }
        return false;
    }

    /**
     * 从JSON字符串中获取值为Bool的key的值<br/>
     * 此方法严格要求类型，不会进行任何类型转换 {@link JsonNode#booleanValue()}<br/>
     *
     * <pre>
     *     {
     *         "bool": true,
     *         "bool2": "true",
     *         "bool3": 1
     *     }
     *     --> getBoolAbs(json, "bool") = true
     *     --> getBoolAbs(json, "bool2") = null
     *     --> getBoolAbs(json, "bool3") = null
     * </pre>
     *
     * @param json JSON字符串
     * @param key  节点key
     * @return 值
     */
    public static @Nullable Boolean getBoolAbs(@Nullable String json, @Nullable String key) {
        if (StringUtils.isNotBlank(key) && isJson(json)) {
            JsonNode valueNode = getNode(json, key);
            if (valueNode != null && valueNode.isBoolean()) {
                return valueNode.booleanValue();
            }
        }
        return null;
    }

    /**
     * 从JSON字符串中获取值为BigDecimal的key的值<br/>
     * 当值为 Number 时将尝试转换
     *
     * @param json JSON字符串
     * @param key  节点key
     * @return 值
     */
    public static @Nullable BigDecimal getDecimal(@Nullable String json, @Nullable String key) {
        if (StringUtils.isNotBlank(key) && isJson(json)) {
            JsonNode valueNode = getNode(json, key);
            if (valueNode != null && valueNode.isNumber()) {
                return valueNode.decimalValue();
            }
        }
        return null;
    }

    /**
     * 从JSON字符串中获取值为Long的key的值<br/>
     * 当值为 Number 时将尝试转换
     *
     * @param json JSON字符串
     * @param key  节点key
     * @return 值
     */
    public static Long getLong(@Nullable String json, @Nullable String key) {
        if (StringUtils.isNotBlank(key) && isJson(json)) {
            JsonNode valueNode = getNode(json, key);
            if (valueNode != null && valueNode.isNumber()) {
                return valueNode.longValue();
            }
        }
        return null;
    }

    /**
     * 从JSON字符串中获取值为Double的key的值<br/>
     * 当值为 Number 时将尝试转换
     *
     * @param json JSON字符串
     * @param key  节点key
     * @return 值
     */
    public static @Nullable Double getDouble(@Nullable String json, @Nullable String key) {
        if (StringUtils.isNotBlank(key) && isJson(json)) {
            JsonNode valueNode = getNode(json, key);
            if (valueNode != null && valueNode.isNumber()) {
                return valueNode.doubleValue();
            }
        }
        return null;
    }

    /**
     * 对象转换 {@link ObjectMapper#convertValue(Object, Class)}
     *
     * @param origin      待转换对象
     * @param targetClass 目标对象类型
     * @return 目标对象
     */
    public static <T> @Nullable T convert(@Nonnull Object origin, @Nonnull Class<T> targetClass) {
        return mapper.convertValue(origin, targetClass);
    }

    /**
     * 对象转换 {@link ObjectMapper#convertValue(Object, TypeReference)}
     *
     * @param origin        待转换对象
     * @param typeReference {@link TypeReference} 类型参考子类，可以获取其泛型参数中的Type类型
     * @return 目标对象
     */
    public static <T> @Nullable T convert(@Nonnull Object origin, @Nonnull TypeReference<T> typeReference) {
        return mapper.convertValue(origin, typeReference);
    }
}