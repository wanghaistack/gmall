package com.atguigu.gmall.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    //定义连接池
    private JedisPool jedisPool;
    //初始化连接池
    public void initJedisPool(String host,int port,int database){
        Jedis jedis=new Jedis("redis.server.com",6379);
        jedis.set("k1","100");
        jedis.close();
        //获取连接池对象
        JedisPoolConfig poolConfig=new JedisPoolConfig();
        //设置最大连接数
        poolConfig.setMaxTotal(200);
        //设置是否连接断开是否等待
        poolConfig.setBlockWhenExhausted(true);
        //设置最大等待时间
        poolConfig.setMaxWaitMillis(10*1000);
        //设置空闲时间最大连接数
        poolConfig.setMaxIdle(20);
        //设置空间时间最少连接数
        poolConfig.setMinIdle(10);
        //设置是否可以测试连接
        poolConfig.setTestOnBorrow(true);
        jedisPool=new JedisPool(poolConfig,host,port,20*1000);

    }
    //获取jedis的方法
    public Jedis getJedis(){
        Jedis jedis=jedisPool.getResource();
        return jedis;
    }

}
