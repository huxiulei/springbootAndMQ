package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 代码我们可以看出和生成者一样的，后面的是获取生产者发送的信息，其中envelope主要存放生产者相关信息（比如交换机、路由key等）body是消息实体。
 */
public class FanoutCustomer2 {
    private static final String EXCHANGE_NAME = "logs";
    public static final Logger logger = LoggerFactory.getLogger(FanoutCustomer2.class);


    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ地址
        factory.setHost("192.168.32.21");
        factory.setUsername("icomm");
        factory.setPassword("icomm");
        //创建一个新的连接
        Connection connection = factory.newConnection();
        //创建一个通道
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout",true);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName,EXCHANGE_NAME,"");

        logger.info("FanoutCustomer2 消费端正在等待获取消息~~~~~~~~~~ ");
        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，
        // 告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                logger.info("FanoutCustomer2 消费端获取到的消息是:{}", message);
            }
        };
        channel.basicConsume(queueName, true, consumer);//队列会自动删除
    }
}