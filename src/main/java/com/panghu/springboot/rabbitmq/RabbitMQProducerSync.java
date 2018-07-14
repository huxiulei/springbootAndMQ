package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 同步消息确认机制
 */
public class RabbitMQProducerSync {
    public final static String QUEUE_NAME = "rabbitMQ.test.sync";
    public static final Logger logger = LoggerFactory.getLogger(RabbitMQProducerSync.class);

    public static void main(String[] args) throws IOException, TimeoutException {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ相关信息
        factory.setHost("192.168.32.21");
        factory.setUsername("icomm");
        factory.setPassword("icomm");

        //factory.setPort(2088);
        //创建一个新的连接
        Connection connection = factory.newConnection();

        //创建一个通道
        Channel channel = connection.createChannel();



        //  声明一个队列
        /**
         queueDeclare第一个参数表示队列名称、
                     第二个参数为是否持久化（true表示是，队列将在服务器重启时生存）、
                     第三个参数为是否是独占队列（创建者可以使用的私有队列，断开后自动删除）、
                     第四个参数为当所有消费者客户端连接断开时是否自动删除队列、
                     第五个参数为队列的其他参数

         注意，RabbitMQ不允许对一个已经存在的队列用不同的参数重新声明，对于试图这么做的程序，会报错，所以，改动之前代码之前，要在控制台中把原来的队列删除
         */
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        String message = "Hello RabbitMQ";

        //发送消息到队列中
        /**
         * basicPublish第一个参数为交换机名称、
         *             第二个参数为队列映射的路由key、
         *             第三个参数为消息的其他属性、
         *             第四个参数为发送信息的主体
         */
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        logger.info("RabbitMQProducerSync 生产消息 : {}", message);

        //confirm 同步机制
        channel.confirmSelect();

        try {
            /**
             * RabbitMQ不会为未ack的消息设置超时时间，它判断此消息是否需要重新投递给消费者的唯一依据是消费该消息的消费者连接是否已经断开。
             * 这么设计的原因是RabbitMQ允许消费者消费一条消息的时间可以很久很久。
             */
            if (channel.waitForConfirms()) {
                //确认ok
                logger.info("当前消息 {} 发送成功",message);
            } else {
                //失败从发
                logger.info("当前消息 {} 发送失败",message);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        //关闭通道和连接
        channel.close();
        connection.close();
    }
}