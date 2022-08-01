package top.mooyea.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.mooyea.api.config.RedisTemplateConfig;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <h1>RedisTemplateUtil<h1>
 * <p>Copyright (C), 星期一,18,10月,2021,</p>
 * <br/>
 * <hr>
 * <h3>File Info:</h3>
 * <p>FileName: RedisTemplateUtil</p>
 * <p>Author:   mooye</p>
 * <p>Work_Email： lidy@skyvis.com.cn</p>
 * <p>E-mail： curtainldy@163.com</p>
 * <p>Date:     2021/10/18</p>
 * <p>Description: Redis 工具类</p>
 * <hr>
 * <h3>History:</h3>
 * <hr>
 * <table>
 *  <thead>
 *  <tr><td style='width:100px;' center>作者姓名</td><td style='width:200px;' center>修改时间</td><td style='width:100px;' center>版本号</td><td style='width:100px;' center>描述</td></tr>
 *  </thead>
 *  <tbody>
 *    <tr><td style='width:100px;' center>mooye</td><td style='width:200px;' center>09:22 2021/10/18</td><td style='width:100px;' center>v_0.0.1</td><td style='width:100px;' center>创建</td></tr>
 *  </tbody>
 * </table>
 * <hr>
 * <br/>
 *
 * @author mooye
 */

@Component
@Slf4j
public class RedisTemplateUtil {
    @Resource
    RedisTemplateConfig templateConfig;
    
    private static final Integer MIN_DATABASE_INDEX = 0;
    private static final Integer MAX_DATABASE_INDEX = 15;
    
    private RedisTemplate<String, Object> select(Integer dbIndex) {
        if (dbIndex == null || dbIndex > MAX_DATABASE_INDEX || dbIndex < MIN_DATABASE_INDEX) {
            return templateConfig.defaultDatabase();
        } else {
            return templateConfig.changeDatabase(dbIndex);
        }
    
    }
    
    public Set<String> keys(String keys, Integer select) {
        try {
            return select(select).keys(keys);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     *
     * @return 成功标志
     */
    public boolean expire(String key, Integer select, long time) {
        try {
            if (time > 0) {
                select(select).expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     *
     * @return 时间(秒) 返回 0 代表为永久有效,-1表示 出现错误;
     */
    public long getExpire(String key, Integer select) {
        if (key == null) {
            return -1;
        } else {
            Long time = select(select).getExpire(key, TimeUnit.SECONDS);
            return time != null ? time : -1;
        }
    }
    
    /**
     * 判断key是否存在
     *
     * @param key 键
     *
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key, Integer select) {
        try {
            return Boolean.TRUE.equals(select(select).hasKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    public void del(Integer select, String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                select(select).delete(key[0]);
            } else {
                select(select).delete(Arrays.stream(key).collect(Collectors.toList()));
            }
        }
    }
    
    /**
     * 普通缓存获取
     *
     * @param key 键
     *
     * @return 值
     */
    public Object get(String key, Integer select) {
        return key == null ? null : select(select).opsForValue().get(key);
    }
    
    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     *
     * @return true成功 false失败
     */
    public boolean set(String key, Object value, Integer select) {
        try {
            select(select).opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     *
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, Integer select, long time) {
        try {
            if (time > 0) {
                select(select).opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value, select);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     *
     * @return -1表示出现错误
     */
    public long incr(String key, Integer select, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        Long incr = select(select).opsForValue().increment(key, delta);
        return incr != null ? incr : -1;
    }
    
    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     *
     * @return -1 表示拆装箱异常
     */
    public long decr(String key, Integer select, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        Long incr = select(select).opsForValue().increment(key, -delta);
        return incr != null ? incr : -1;
    }
    
    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     *
     * @return 值
     */
    public Object hget(String key, Integer select, String item) {
        return select(select).opsForHash().get(key, item);
    }
    
    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     *
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key, Integer select) {
        return select(select).opsForHash().entries(key);
    }
    
    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     *
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Integer select, Map<String, Object> map) {
        try {
            select(select).opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     *
     * @return true成功 false失败
     */
    public boolean hmset(String key, Integer select, Map<String, Object> map, long time) {
        boolean result = false;
        try {
            select(select).opsForHash().putAll(key, map);
            if (time > 0) {
                result = expire(key, select, time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     *
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, Integer select) {
        try {
            select(select).opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     *
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, Integer select, long time) {
        try {
            select(select).opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, select, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Integer select, Object... item) {
        select(select).opsForHash().delete(key, item);
    }
    
    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     *
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item, Integer select) {
        return select(select).opsForHash().hasKey(key, item);
    }
    
    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     *
     * @return
     */
    public double hincr(String key, String item, Integer select, double by) {
        return select(select).opsForHash().increment(key, item, by);
    }
    
    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     *
     * @return
     */
    public double hdecr(String key, String item, Integer select, double by) {
        return select(select).opsForHash().increment(key, item, -by);
    }
    
    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     *
     * @return
     */
    public Set<Object> sGet(String key, Integer select) {
        try {
            return select(select).opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     *
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value, Integer select) {
        try {
            return Boolean.TRUE.equals(select(select).opsForSet().isMember(key, value));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     *
     * @return 成功个数
     */
    public long sSet(String key, Integer select, Object... values) {
        try {
            Long successCount = select(select).opsForSet().add(key, values);
            return successCount != null ? successCount : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     *
     * @return 成功个数
     */
    public long sSetAndTime(String key, Integer select, long time, Object... values) {
        try {
            Long count = select(select).opsForSet().add(key, values);
            if (time > 0) {
                expire(key, select, time);
            }
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 获取set缓存的长度
     *
     * @param key 键
     *
     * @return -1表示拆装箱异常
     */
    public long sGetSetSize(String key, Integer select) {
        try {
            Long size = select(select).opsForSet().size(key);
            return size != null ? size : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     *
     * @return 移除的个数(- 1表示拆装箱异常)
     */
    public long setRemove(String key, Integer select, Object... values) {
        try {
            Long count = select(select).opsForSet().remove(key, values);
            return count != null ? count : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    // ===============================list=================================
    
    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1 代表所有值
     *
     * @return
     */
    public List<Object> lGet(String key, Integer select, long start, long end) {
        try {
            return select(select).opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取list缓存的长度
     *
     * @param key 键
     *
     * @return
     */
    public long lGetListSize(String key, Integer select) {
        try {
            Long size = select(select).opsForList().size(key);
            return size != null ? size : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     *
     * @return
     */
    public Object lGetIndex(String key, Integer select, long index) {
        try {
            return select(select).opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     *
     * @return
     */
    public boolean lSet(String key, Object value, Integer select) {
        try {
            select(select).opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     *
     * @return
     */
    public boolean lSet(String key, Object value, Integer select, long time) {
        try {
            select(select).opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, select, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     *
     * @return
     */
    public boolean lSet(String key, Integer select, List<Object> value) {
        try {
            select(select).opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     *
     * @return
     */
    public boolean lSet(String key, Integer select, List<Object> value, long time) {
        try {
            select(select).opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, select, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     *
     * @return
     */
    public boolean lUpdateIndex(String key, Integer select, long index, Object value) {
        try {
            select(select).opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     *
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value, Integer select) {
        try {
            Long remove = select(select).opsForList().remove(key, count, value);
            return remove != null ? remove : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
