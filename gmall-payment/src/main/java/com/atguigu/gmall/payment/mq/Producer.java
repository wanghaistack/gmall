package com.atguigu.gmall.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class Producer {
    public static void main(String[] args) {
        //创建连接
        ConnectionFactory connect=new ActiveMQConnectionFactory("tcp://192.168.220.129:61616");
        try {
            //获取连接
            Connection connection = connect.createConnection();
            //启动连接
            connection.start();
            //第一个值表示是否使用事务，如果选择true,第二个值相当于选择0
            //创建session                                      //手动提交
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            //创建生产者队列
            Queue queueTest = session.createQueue("TEST");
            //创建生产者
            MessageProducer producer = session.createProducer(queueTest);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("successful");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            connection.close();
            producer.close();
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
