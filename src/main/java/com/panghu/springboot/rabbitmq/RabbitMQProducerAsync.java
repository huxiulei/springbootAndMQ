package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

/**
 * 异步消息确认机制
 */
public class RabbitMQProducerAsync {
    public final static String QUEUE_NAME = "rabbitMQ.test.sync";
    public static final Logger logger = LoggerFactory.getLogger(RabbitMQProducerAsync.class);

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
         */
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        String message = "Hello RabbitMQ";


        // confirm  机制 异步 通过注册listener，实现异步ack，提高性能
        channel.confirmSelect();
        // 可以考虑将要发送到MQ的msg记录到SortedSet(TreeSet)中
        SortedSet<Long> confirmSet = Collections.synchronizedSortedSet(new TreeSet<Long>());
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                logger.info("---- handleAck  deliveryTag: {} , multiple: ",deliveryTag ,multiple);
                if (multiple) {
                    // confirmSet.headSet(deliveryTag + 1).clear();
                } else {
                    // confirmSet.remove(deliveryTag);
                }
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                logger.info("---- handleNack  deliveryTag: {} , multiple: ",deliveryTag ,multiple);
                if (multiple) {
                    // confirmSet.headSet(deliveryTag + 1) TODO    进行重新发送，或记录下来，后续再统一发送;
                } else {
                    // confirmSet.first();  进行重新发送，或记录下来，后续再统一发送;
                }
            }
        });

        //发送消息到队列中  该句要在 channel.addConfirmListener 之后
        /**
         * basicPublish第一个参数为交换机名称、
         *             第二个参数为队列映射的路由key、
         *             第三个参数为消息的其他属性、
         *             第四个参数为发送信息的主体
         */
        logger.info("RabbitMQProducerAsync 生产消息 : {}", message);
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));


        // 模拟耗时, 以便于让 channel.addConfirmListener 异步执行
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //关闭通道和连接
        channel.close();
        connection.close();
    }
}