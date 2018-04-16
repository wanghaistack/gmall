package com.atguigu.gmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  //相当于springmvc里面的xml文件配置
public class RedisConfig {
    //读取配置文件中的ip地址
    @Value("${spring.redis.host:disabled}")
    private  String host;
    //读取配置文件中的端口号
    @Value("${spring.redis.port:0}")
    private int port;
    //读取配置的数据库名称 第几个数据库
    @Value("${spring.redis.database:0}")
    private int database;
    //获取实体bean对象
    @Bean
    public RedisUtil getRedisUtil(){
        if (host.equals("disabled")){
            return null;
        }
        RedisUtil redisUtil=new RedisUtil();
        redisUtil.initJedisPool(host,port,database);
        return redisUtil;
    }

}
