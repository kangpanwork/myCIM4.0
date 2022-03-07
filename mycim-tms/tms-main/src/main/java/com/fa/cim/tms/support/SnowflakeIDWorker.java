package com.fa.cim.tms.support;

import com.fa.cim.common.annotation.IdPrefix;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.locks.StampedLock;

/**
 * description:
 * <p>Twitter_Snowflake<br>
 * SnowFlake的结构如下(每部分用-分开):<br>
 * <p>                          41 bit-时间戳                                      12bit-序列号
 * <p>           --------------------------------------------                    -----------
 * <p>          |                                           |                   |          |
 * <p>      0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * <p>      |                                                   |   |   |   |
 * <p>     1 bit-不用                                           ----    ----
 * <p>                                                          10 bit-工作id
 * <p> 1. 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0
 * <p> 2. 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
 * <p> 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IDWorker类的startTime属性）。41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
 * <p> 3. 10位的数据机器位，可以部署在1024个节点，包括5位dataCenterID和5位workerID;5位（bit）可以表示的最大正整数是2^5−1=31，即可以用0、1、2、3、....31这32个数字，来表示不同的dateCenterId或workerId<br>
 * <p> 4. 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生2^12-1=4096个ID序号
 * <p>
 * <p> 由于在Java中64bit的整数是long类型，所以在Java中SnowFlake算法生成的id就是long来存储的。
 * <p> SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 * <p>
 * <p> 雪花算法改进版：(2021/02/23)
 * <p> 将10位工作id，换成部署该服务的IP地址（二进制后16位），
 * <p>
 * <p>                          41 bit-时间戳                                          6bit-序列号
 * <p>           --------------------------------------------                          ------
 * <p>          |                                           |                         |    |
 * <p>      0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000000 - 00000000 - 000000
 * <p>      |                                                   |      |   |      |
 * <p>     1 bit-不用                                           -------    --------
 * <p>                                                             16 bit-主机号
 * <p> 其中，主机号前8bit,取IP地址后16位中的前8位；主机号后8bit，取二进制IP最后8位
 * <p> 如：
 * <p> IP：                192.168.1.125
 * <p> 二进制：    11000000 10101000 00000001 01111101
 * <p>                                  |        |
 * <p>                           主机号前8bit   主机号后8bit
 * <p>
 * <p> 当环境变量 MYCIM_WORKER_ID 赋值后，会替换 workID
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/9/19        ********             PlayBoy            create file
 * 2021/2/23        ********             zqi                更改雪花算法，支持分布式多实例部署
 *
 * @author: PlayBoy
 * @date: 2018/9/19 21:46
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
public class SnowflakeIDWorker implements IdentifierGenerator {

    /**
     * strategy reference
     */
    public final static String STRATEGY_REFERENCE = "com.fa.cim.support.SnowflakeIDWorker";

    /**
     * 开始时间截 (2018-01-01)
     */
    private final static long START_TIMESTAMP = 1514736000000L;

    /**
     * 机器id所占的位数
     */
    private final static long WORKER_ID_BITS = 8L;

    /**
     * 数据标识id所占的位数
     */
    private final static long DATA_CENTER_ID_BITS = 8L;

    /**
     * 支持的最大机器id，结果是 pow(2,8) (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private final static long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 支持的最大数据标识id，结果是 pow(2,8)
     */
    private final static long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    /**
     * 序列在id中占的位数
     */
    private final static long SEQUENCE_BITS = 6L;

    /**
     * 机器ID向左移6位
     */
    private final static long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据标识id向左移14位(6+8)
     */
    private final static long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间截向左移22位(6+8+8)
     */
    private final static long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final static long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 默认的mycim 当前worker id
     */
    private final static long MYCIM_WORKER_ID = 0L;

    /**
     * 默认的mycim 当前dataCenter id
     */
    private final static long MYCIM_DATA_CENTER_ID = 0L;

    /**
     * 工作机器ID(0~256) 或 IP地址
     */
    private long workerID;

    /**
     * 数据中心ID(0~256) 或 IP地址
     */
    private long dataCenterID;

    /**
     * 毫秒内序列(0~64)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;


    private static StampedLock stampedLock = new StampedLock();

    public SnowflakeIDWorker() {
        this(MYCIM_WORKER_ID, MYCIM_DATA_CENTER_ID);
    }

    /**
     * description:
     * <p>构造函数</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param workerID     工作ID (0~31)
     * @param dataCenterID 数据中心ID (0~31)
     * @author PlayBoy
     * @date 2018/9/21 13:11:55
     */
    public SnowflakeIDWorker(long workerID, long dataCenterID) {
        if (workerID > MAX_WORKER_ID || workerID < 0) {
            throw new IllegalArgumentException(String.format("worker ID can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (dataCenterID > MAX_DATA_CENTER_ID || dataCenterID < 0) {
            throw new IllegalArgumentException(String.format("dataCenter ID can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
        }
        this.workerID = workerID;
        this.dataCenterID = dataCenterID;
    }

    // ==============================Methods==========================================

    /**
     * description:
     * <p>获得下一个ID (该方法是线程安全的)<br></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return SnowflakeID
     * @author PlayBoy
     * @date 2018/9/21 13:12:24
     */
    public /*synchronized*/ long nextID() {
        long writeLock = stampedLock.writeLock();
        try {
            long timestamp = timeGen();

            //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
            if (timestamp < lastTimestamp) {
                throw new RuntimeException(
                        String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
            }

            //如果是同一时间生成的，则进行毫秒内序列
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & SEQUENCE_MASK;
                //毫秒内序列溢出
                if (sequence == 0) {
                    //阻塞到下一个毫秒,获得新的时间戳
                    timestamp = tilNextMillis(lastTimestamp);
                }
            }
            //时间戳改变，毫秒内序列重置
            else {
                sequence = 0L;
            }

            //上次生成ID的时间截
            lastTimestamp = timestamp;

            //移位并通过或运算拼到一起组成64位的ID
            return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                    | (dataCenterID << DATA_CENTER_ID_SHIFT)
                    | (workerID << WORKER_ID_SHIFT)
                    | sequence;
        } finally {
            stampedLock.unlockWrite(writeLock);
        }

    }

    /**
     * description:
     * <p>阻塞到下一个毫秒，直到获得新的时间戳<br></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     * @author PlayBoy
     * @date 2018/9/25 12:20:20
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * description:
     * <p>返回以毫秒为单位的当前时间<br></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @return 当前时间(毫秒)
     * @author PlayBoy
     * @date 2018/9/25 12:20:42
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * description:
     * <p><br></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param entityClazz entityClazz
     * @return id prefix
     * @author PlayBoy
     * @date 2018/9/27 13:14:15
     */
    public String getIdPrefix(Class<?> entityClazz) {
        Objects.requireNonNull(entityClazz, "entityClazz can not be null");
        IdPrefix prefixAnnotation = entityClazz.getAnnotation(IdPrefix.class);
        return prefixAnnotation == null ? entityClazz.getSimpleName().toUpperCase() : prefixAnnotation.value();
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object entity) throws HibernateException {
        return generateId(entity.getClass());
    }

    /**
     * generateId
     *
     * @param clazz
     * @return id
     * @author PlayBoy
     */
    public String generateId(Class<?> clazz) {
        return String.format("%s.%s", getIdPrefix(clazz), nextID());
    }

    /**
     * getInstance
     *
     * @return SnowflakeIDWorker
     * @author PlayBoy
     *//*
    public static SnowflakeIDWorker getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final SnowflakeIDWorker INSTANCE = new SnowflakeIDWorker(workID, dataCenterID);
        }
    }*/

    private static volatile SnowflakeIDWorker instance;
    private static final String MYCIM_WORKER_ID_ENV = "MYCIM_WORKER_ID";

    /**
     * Gets the singleton instance for SnowflakeIDWorker.
     *
     * @return singleton instance
     * @author ZQI
     * @date 2021/2/23 15:32
     */
    public static SnowflakeIDWorker getInstance() {
        long dataCenterID = 0L;
        long workID = 0L;
        if (null == instance) {
            synchronized (SnowflakeIDWorker.class) {
                if (null == instance) {
                    InetAddress localHost = null;
                    try {
                        localHost = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        workID = new Random().nextInt(~(-1 << WORKER_ID_BITS));
                        dataCenterID = 0L;
                    }
                    if (null != localHost) {
                        String hostAddress = localHost.getHostAddress();
                        log.trace("Service IP: {}", hostAddress);
                        String[] split = hostAddress.split("[.]");
                        dataCenterID = Long.parseLong(split[2]);
                        workID = Long.parseLong(split[3]);
                    }
                    String workIDEnv = System.getenv(MYCIM_WORKER_ID_ENV);
                    if (null != workIDEnv) {
                        log.trace("MYCIM_WORKER_ID_ENV: {}", workIDEnv);
                        workID = Long.parseLong(workIDEnv);
                        dataCenterID = 0L;
                    }
                    log.trace("MYCIM_WORKER_ID: {}", workID);
                    log.trace("MYCIM_DATA_CENTER: {}", dataCenterID);
                    instance = new SnowflakeIDWorker(workID, dataCenterID);
                }
            }
        }
        return instance;
    }
}
