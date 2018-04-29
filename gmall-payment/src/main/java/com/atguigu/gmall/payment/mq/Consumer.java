package com.atguigu.gmall.payment.mq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Consumer {
    public static void main(String[] args) {
        ConnectionFactory connect=new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,ActiveMQConnectionFactory.DEFAULT_PASSWORD,"tcp://192.168.220.129:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            Session session = connection.createSession(false,Session.CLIENT_ACKNOWLEDGE);
            Destination test = session.createQueue("TEST");
            MessageConsumer consumer=session.createConsumer(test);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {

                        TextMessage textMessage=(TextMessage) message;
                        try {
                            String text = textMessage.getText();
                            System.out.println("text = " + text);
                            textMessage.acknowledge();
                            consumer.close();
                            connection.close();
                            session.close();
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }

                }
            });

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
