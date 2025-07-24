package com.example.filmsfinder.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;

/**
 * JsonUtil 提供统一的 JSON 序列化/反序列化方法，
 * 同时支持 String（JSON 字符串）和 Map（Redis Hash）的反序列化到任意 Bean。
 */
public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // 设置全局日期格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 支持 Java 8 时间（LocalDateTime 等）
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // LocalDateTime 默认不带格式，手动添加反序列化规则
        javaTimeModule.addSerializer(LocalDateTime.class,
                new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        javaTimeModule.addDeserializer(LocalDateTime.class,
                new com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        mapper.registerModule(javaTimeModule);

        //当前端传来多余的字段 程序不会崩溃
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /** --------- 序列化 --------- */

    /**
     * 将对象转换为 JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 序列化失败: " + obj, e);
        }
    }

    /** --------- 反序列化：字符串 --------- */

    /**
     * 将 JSON 字符串反序列化为指定类型的 Bean。
     *   String shopJson = redisTemplate.opsForValue().get(key);
     *   Shop shop = JsonUtil.toBean(shopJson, Shop.class);
     */
    public static <T> T toBean(String json, Class<T> clazz) {
        if (json == null || clazz == null) return null;
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("JSON 反序列化失败: json=%s, targetClass=%s", json, clazz), e
            );
        }
    }

    /**
     * 将 JSON 字符串反序列化为带泛型的复杂类型（例如 List<User>）
     *   String ordersJson = redisTemplate.opsForValue().get(key);
     *   List<Order> orders = JsonUtil.toBean(ordersJson, new TypeReference<List<Order>>(){});
     */
    public static <T> T toBean(String json, TypeReference<T> typeReference) {
        if (json == null || typeReference == null) return null;
        try {
            return mapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("JSON 反序列化失败: json=%s, typeReference=%s", json, typeReference), e
            );
        }
    }

    /** --------- 反序列化：Map（Redis Hash） --------- */

    /**
     * 将一个 Map（通常是 StringRedisTemplate.opsForHash().entries(key) 得到的 Map<Object,Object>）
     * 直接转换成目标 Bean。
     *   Map<Object,Object> map = redisTemplate.opsForHash().entries(hashKey);
     *   Shop shop = JsonUtil.toBean(map, Shop.class);
     */
    public static <T> T toBean(Map<?, ?> map, Class<T> clazz) {
        if (map == null || clazz == null) return null;
        // Jackson 会把 Map key/value 转为对应的 Bean 属性
        try {
            return mapper.convertValue(map, clazz);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    String.format("Map 转 Bean 失败: map=%s, targetClass=%s", map, clazz), e
            );
        }
    }

    /**
     * 将 Map 转为 List<Bean>（当 Redis Hash 存的是一个 JSON 数组，或多个条目对应 List 时可用）
     */
    public static <T> List<T> toList(Map<?, ?> map, TypeReference<List<T>> typeReference) {
        if (map == null || map.isEmpty() || typeReference == null) return null;
        try {
            return mapper.convertValue(map, typeReference);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    String.format("Map 转 List 失败: map=%s, typeReference=%s", map, typeReference.getType()), e
            );
        }
    }

    /** --------- 额外工具 --------- */

    /**
     * 将 JSON 字符串转为 Map<String,Object>
     */
    public static Map<String, Object> toMap(String json) {
        return toBean(json, new TypeReference<Map<String, Object>>() {});
    }

}
