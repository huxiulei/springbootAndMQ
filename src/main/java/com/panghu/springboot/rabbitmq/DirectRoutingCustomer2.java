package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class DirectRoutingCustomer2 {
    public static final Logger logger = LoggerFactory.getLogger(DirectRoutingCustomer2.class);

    // 交换器名称
    private static final String EXCHANGE_NAME = "direct_logs";
    // 路由关键字
    private static final String[] routingKeys = new String[]{"warning","error"};

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.32.21");
        factory.setUsername("icomm");
        factory.setPassword("icomm");


        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明交换器
        channel.exchangeDeclare(EXCHANGE_NAME, ExchangeTypes.DIRECT);
        //获取匿名队列名称
        String queueName = channel.queueDeclare().getQueue();

        //根据路由关键字进行绑定
        for (String routingKey : routingKeys) {
            channel.queueBind(queueName, EXCHANGE_NAME, routingKey);
            logger.info("DirectRoutingCustomer2 exchange:" + EXCHANGE_NAME + "," +
                    " queue:" + queueName + ", BindRoutingKey:" + routingKey);
        }
        logger.info("DirectRoutingCustomer2  等待获取消息~~~~");
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                logger.info("DirectRoutingCustomer2 获取到消息 '" + envelope.getRoutingKey() + "':'" + message + "'");
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        channel.basicConsume(queueName, false, consumer);
    }
}