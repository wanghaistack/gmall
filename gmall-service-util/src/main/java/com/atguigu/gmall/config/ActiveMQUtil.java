package com.atguigu.gmall.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.ConnectionFactory;
public class ActiveMQUtil {
    PooledConnectionFactory pooledConnectionFactory=null;
    public ConnectionFactory init(String brokerUrl){
        ActiveMQConnectionFactory factory=new ActiveMQConnectionFactory(brokerUrl);
        //加入连接池
        pooledConnectionFactory=new PooledConnectionFactory(factory);
        //出现异常时重新连接
        pooledConnectionFactory.setReconnectOnException(true);
        //设置最大连接数
        pooledConnectionFactory.setMaxConnections(5);
        //设置超时时间10秒
        pooledConnectionFactory.setExpiryTimeout(10*10000);
     return pooledConnectionFactory;
    }
    public ConnectionFactory getConnectionFactory(){
        return pooledConnectionFactory;
    }

}
