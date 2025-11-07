package com.qimo.shiwu.util;
import org.springframework.stereotype.Component;

/**
 * 雪花算法 ID 生成器
 *
 * [开发者注意]:
 * 这是一个全局唯一的 ID 生成器 (Twitter Snowflake 算法)。
 * 它被注册为 Spring @Component (单例)，
 * 所有需要生成新 ID 的服务 (如 UserService, OrderService) 都应该注入并使用它。
 *
 * 你可以根据你的服务器集群配置来修改 `datacenterId` 和 `machineId`。
 */
@Component
public class SnowflakeIdGenerator {

    // 起始时间戳 (2010-11-04 09:42:54)
    private final long twepoch = 1288834974657L;

    // 各部分占用的位数
    private final long workerIdBits = 5L;       // 机器ID
    private final long datacenterIdBits = 5L;  // 数据中心ID
    private final long sequenceBits = 12L;      // 序列号

    // 各部分的最大值
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    // 各部分向左的位移
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long workerId;
    private long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator() {
        // 默认值，在分布式环境中应从配置中读取
        this.datacenterId = 1L;
        this.workerId = 1L;
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
    }

    // 构造函数，允许注入
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 获取下一个 ID (线程安全)
     * @return 唯一的 64 位 ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 时钟回拨检查
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        // 同一毫秒内的序列号
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 序列号溢出, 等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 新的毫秒, 序列号重置为 0
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组装 64 位 ID:
        // (时间戳 - 起始时间戳) << 22 | 数据中心ID << 17 | 机器ID << 12 | 序列号
        return ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成 ID 的时间戳
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     * @return 当前时间戳 (毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
}
