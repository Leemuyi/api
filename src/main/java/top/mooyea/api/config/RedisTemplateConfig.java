package top.mooyea.api.config;


import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>RedisConfig<h1>
 * <p>Copyright (C), 星期一,18,10月,2021,</p>
 * <br/>
 * <hr>
 * <h3>File Info:</h3>
 * <p>FileName: RedisConfig</p>
 * <p>Author:   mooye</p>
 * <p>Work_Email： lidy@skyvis.com.cn</p>
 * <p>E-mail： curtainldy@163.com</p>
 * <p>Date:     2021/10/18</p>
 * <p>Description: Redis 配置类</p>
 * <hr>
 * <h3>History:</h3>
 * <hr>
 * <table>
 *  <thead>
 *  <tr><td style='width:100px;' center>作者姓名</td><td style='width:200px;' center>修改时间</td><td style='width:100px;' center>版本号</td><td style='width:100px;' center>描述</td></tr>
 *  </thead>
 *  <tbody>
 *    <tr><td style='width:100px;' center>mooye</td><td style='width:200px;' center>09:11 2021/10/18</td><td style='width:100px;' center>v_0.0.1</td><td style='width:100px;' center>创建</td></tr>
 *  </tbody>
 * </table>
 * <hr>
 * <br/>
 *
 * @author mooye
 */

@Configuration
public class RedisTemplateConfig {
    
    @Value("${spring.redis.host:127.0.0.1}")
    private String host;
    @Value("${spring.redis.port:6379}")
    private int port;
    @Value("${spring.redis.password:skyvis123456}")
    private String password;
    @Value("${spring.redis.database:0}")
    private int defaultIndex;
    
    private static final int MIN_DATABASE_INDEX = 0;
    private static final int MAX_DATABASE_INDEX = 15;
    private static final List<Integer> INDEX_LIST = new ArrayList<>() {{
        add(0);
        add(1);
        add(2);
        add(3);
        add(4);
        add(5);
        add(6);
        add(7);
        add(8);
        add(9);
        add(10);
        add(11);
        add(12);
        add(13);
        add(14);
        add(15);
    }};
    
    private final Map<Integer, RedisTemplate<String, Object>> redisTemplateMap = new HashMap<>();
    
    @PostConstruct
    public void initRedisTemplate() {
        INDEX_LIST.forEach(index -> redisTemplateMap.put(index, initRedisTemplate(index)));
    }
    
    /**
     * 初始化连接工厂 这里使用 LettuceConnectionFactory 类
     *
     * @param index 数据库序号
     *
     * @return LettuceConnectionFactory
     */
    private LettuceConnectionFactory initConnectionFactory(Integer index) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);
        configuration.setPassword(password);
        // 指定数据库
        if (index == null || index > MAX_DATABASE_INDEX || index < MIN_DATABASE_INDEX) {
            configuration.setDatabase(defaultIndex);
        } else {
            configuration.setDatabase(index);
        }
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration);
        // 刷新配置
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }
    
    /**
     * 初始化 RedisTemplate 模板
     *
     * @param index 数据库序号
     *
     * @return RedisTemplate<String, Object>
     */
    private RedisTemplate<String, Object> initRedisTemplate(Integer index) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置 Redis 连接
        redisTemplate.setConnectionFactory(initConnectionFactory(index));
        // 配置 JSON 序列化 这里使用 FastJsonRedisSerializer
        FastJsonRedisSerializer<Object> jsonRedisSerializer = fastJsonRedisSerializer();
        // 配置 String 序列化
        StringRedisSerializer stringRedisSerializer = StringRedisSerializer.UTF_8;
        // 装配 redisTemplate 模板
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);
        // 刷新配置
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    
    private FastJsonRedisSerializer<Object> fastJsonRedisSerializer() {
        FastJsonRedisSerializer<Object> jsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
        FastJsonConfig fastJsonConfig = jsonRedisSerializer.getFastJsonConfig();
        SerializeConfig serializeConfig = fastJsonConfig.getSerializeConfig();
        serializeConfig.put(LocalDate.class, (serializer, object, fieldName, fieldType, features) -> {
            SerializeWriter writer = serializer.out;
            if (object == null) {
                writer.writeNull();
                return;
            }
            writer.writeString(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format((LocalDateTime) object));
        });
        serializeConfig.put(LocalDate.class, (serializer, object, fieldName, fieldType, features) -> {
            SerializeWriter writer = serializer.out;
            if (object == null) {
                writer.writeNull();
                return;
            }
            writer.writeString(DateTimeFormatter.ofPattern("yyyy-MM-dd").format((LocalDate) object));
        });
        serializeConfig.put(LocalTime.class, (serializer, object, fieldName, fieldType, features) -> {
            SerializeWriter writer = serializer.out;
            if (object == null) {
                writer.writeNull();
                return;
            }
            writer.writeString(DateTimeFormatter.ofPattern("HH:mm:ss").format((LocalTime) object));
        });
        fastJsonConfig.setSerializeConfig(serializeConfig);
        fastJsonConfig.setFeatures(Feature.SupportAutoType);
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteClassName);
        jsonRedisSerializer.setFastJsonConfig(fastJsonConfig);
        return jsonRedisSerializer;
    }
    
    /**
     * 进行数据库切换
     *
     * @param index 数据库标识
     *
     * @return RedisTemplate<String, Object>
     */
    public RedisTemplate<String, Object> changeDatabase(Integer index) {
        return redisTemplateMap.get(index);
    }
    
    public RedisTemplate<String, Object> defaultDatabase() {
        return redisTemplateMap.get(defaultIndex);
    }
}
