package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 这种应该属于模糊匹配
 * ：可以替代一个词
 * #：可以替代0或者更多的词
 */
public class TopicProducer {
    public static final Logger logger = LoggerFactory.getLogger(TopicProducer.class);
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = null;
        Channel channel = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("192.168.32.21");
            factory.setUsername("icomm");
            factory.setPassword("icomm");

            connection = factory.newConnection();
            channel = connection.createChannel();

            //声明一个匹配模式的交换机
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            //待发送的消息
            String[] routingKeys = new String[]{
                    "quick.orange.rabbit",
                    "lazy.orange.elephant",
                    "quick.orange.fox",
                    "lazy.brown.fox",
                    "quick.brown.fox",
                    "quick.orange.male.rabbit",     // 没有匹配到的  将会被丢弃
                    "lazy.orange.male.rabbit"
            };
            //发送消息
            for (String severity : routingKeys) {
                String message = "From " + severity + " routingKey's message!";
                channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes());
                logger.info("TopicProducer 产生消息 '" + severity + "':'" + message + "'");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (connection != null) {
                channel.close();
                connection.close();
            }
        } finally {
            if (connection != null) {
                channel.close();
                connection.close();
            }
        }
    }
}