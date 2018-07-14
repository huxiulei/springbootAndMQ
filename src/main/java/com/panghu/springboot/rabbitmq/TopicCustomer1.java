package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TopicCustomer1 {
    public static final Logger logger = LoggerFactory.getLogger(TopicCustomer1.class);
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.32.21");
        factory.setUsername("icomm");
        factory.setPassword("icomm");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //声明一个匹配模式的交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        String queueName = channel.queueDeclare().getQueue();
        //路由关键字
        String[] routingKeys = new String[]{"*.orange.*"};
        //绑定路由
        for (String routingKey : routingKeys) {
            channel.queueBind(queueName, EXCHANGE_NAME, routingKey);
            logger.info("TopicCustomer1 exchange: " + EXCHANGE_NAME + ", queue: " + queueName + ", BindRoutingKey: " + routingKey);
        }
        logger.info("TopicCustomer1 等待获取消息~~~~~");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                logger.info("TopicCustomer1 接收到消息 '" + envelope.getRoutingKey() + "':'" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }
}