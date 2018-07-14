package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 如果Work1和Work2都处于等待接收消息的话(就是两个都先运行后在运行WorkTask)
 * 两者会平均分配WorkTask产生的消息(多个消费者可以订阅同一个 Queue，这时 Queue 中的消息会被平均分摊给多个消费者进行处理，而不是每个消费者都收到所有的消息并处理。)
 */
public class WorkTask {
    private static final String TASK_QUEUE_NAME = "task_queue";
    public static final Logger logger = LoggerFactory.getLogger(WorkTask.class);

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        //CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        factory.setHost("192.168.32.21");
        factory.setUsername("icomm");
        factory.setPassword("icomm");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        //分发信息
        for (int i = 1; i <= 10; i++) {
            String message = "Hello RabbitMQ-" + i;
            channel.basicPublish("", TASK_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
            logger.info("WorkTask 发送消息 {} ",message);
        }
        channel.close();
        connection.close();
    }
}