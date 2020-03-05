package com.demo.Application;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * @author chengle
 * @version 创建时间：2020/3/5 15:51
 * @describe 缓存雪崩
 */
public class RedisSnowCrash {
    private static Logger logger =LoggerFactory.getLogger(RedisSnowCrash.class);
    public static void main(String[] args) {
        final String host="192.168.25.133";
        final Integer port=6301;
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(8);
        jedisPoolConfig.setMaxWaitMillis(2000);
        jedisPoolConfig.setMaxTotal(10000);
        jedisPoolConfig.setMinIdle(2);
        Jedis jedis= new JedisPool(jedisPoolConfig,host,port).getResource();

        //solution1展示失效时间层梯形散开，以避免同一时间缓存大量同时失效造成雪崩
      //  solution1(jedis,TimeUnit.SECONDS);
        //solution2展示redis分布式锁保证缓存的单线程（进程）读写，从而避免失效时大量的并发请求落到底层存储系统上。
        solution2(jedis);
    }
    /***
     * solution1展示的是解决缓存雪崩中分散缓存数据失效时间以此来避免大规模数据同时失效造成的雪崩
     */
    public static void solution1(Jedis jedis,TimeUnit unit){

        long timeout =50;
        long expireSeconds = TimeUnit.SECONDS.convert(timeout,unit );
        for (int i = 0; i < 100; i++) {
            double random = Math.random() * (10);
            long  lockExpireTime = (long) (expireSeconds+random);
            jedis.set("helllo" + i, "test" + i, "NX", "EX", lockExpireTime);

        }
    }
    /***
     * solution2展示的是用redis分布式锁方式保证缓存的单线程读写，从而避免失效时大量的并发请求落到底层存储系统上。
     */
    private static void solution2(Jedis jedis) {
        boolean lock = RedisTool.tryGetDistributedLock(jedis, "lock", UUID.randomUUID().toString(), 1000);
        if (lock){
            logger.info(Thread.currentThread().getName() + "加锁成功吗：" + lock);
            for (int i = 0; i < 20; i++) {
                jedis.set("hello" + i, "world" + i);
            }
        } else {
            try {
                logger.info(Thread.currentThread().getName() + "加锁成功吗：" + lock);
                Thread.sleep(1);
            } catch (InterruptedException e) {
               logger.error("{}",e);
            }
        }
        boolean release = RedisTool.releaseDistributedLock(jedis, "lock", UUID.randomUUID().toString());
        if (release) {
            logger.info(Thread.currentThread().getName() + "解锁成功吗：" + lock);
            for (int i = 0; i < 20; i++) {
                jedis.set("hello" + i, "world" + i);
            }
        } else {
            try {
                Thread.sleep(1);
                logger.info(Thread.currentThread().getName() + "解锁成功吗：" + lock);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    static class RedisTool {

        private static final String LOCK_SUCCESS = "OK";
        private static final String SET_IF_NOT_EXIST = "NX";
        private static final String SET_WITH_EXPIRE_TIME = "PX";
        private static final Long RELEASE_SUCCESS = 1L;

        /**
         * 尝试获取分布式锁
         *
         * @param jedis      Redis客户端
         * @param lockKey    锁
         * @param requestId  请求标识
         * @param expireTime 超期时间
         * @return 是否获取成功2
         */
        public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, int expireTime) {

            String result = jedis.set(lockKey, UUID.randomUUID().toString(), SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);

            if (LOCK_SUCCESS.equals(result)) {
                return true;
            }
            return false;

        }

        /**
         * 释放获取分布式锁
         *
         * @param jedis     Redis客户端
         * @param lockKey   锁
         * @param requestId 请求标识
         * @return 是否获取成功
         */
        public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String requestId) {

            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
            if (RELEASE_SUCCESS.equals(result)) {
                return true;
            }
            return false;

        }
    }
}
