package com.panghu.springboot.rabbitmq;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 通过spring-amqp的规范 实现
 * 可以看得出来 不需要创建Channel、Connection 以及维护它们的关闭操作了
 */
public class SpringAMQPRabbitMQ {

    public static void main(String[] args) throws InterruptedException {
        //获取一个连接工厂，用户默认是guest/guest（只能使用部署在本机的RabbitMQ）
        //是Spring实现的对com.rabbitmq.client.Connection的包装

        ConnectionFactory cf = null;
        try {
            cf = new CachingConnectionFactory(new URI("amqp://icomm:icomm@192.168.32.21:5672"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //对AMQP 0-9-1的实现
        RabbitAdmin admin = new RabbitAdmin(cf);
        //声明一个队列
        Queue queue = new Queue("SpringAMQPRabbitMQ-queue");
        admin.declareQueue(queue);
        //声明一个exchange
        TopicExchange exchange = new TopicExchange("SpringAMQPRabbitMQ-exchange");

        admin.declareExchange(exchange);
        //绑定队列到exchange，加上routingKey foo.*
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("foo.*"));

        //监听容器(它是对监听这个动作的抽象，一个容器可以有多个Consumer，并且可以控制如超时时间等配置。)
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(cf);
        //监听者对象
        Object listener = new Object() {
            @SuppressWarnings("unused")
            public void handleMessage(String foo) {
                System.out.println("SpringAMQPRabbitMQ收到消息---> : " + foo);
            }
        };
        //通过这个适配器代理listener
        MessageListenerAdapter adapter = new MessageListenerAdapter(listener);
        //把适配器（listener）设置给Container
        container.setMessageListener(adapter);
        //设置该容器监听的队列名，可以传多个，public void setQueueNames(String... queueName) {
        container.setQueueNames("SpringAMQPRabbitMQ-queue");
        //开始监听
        container.start();







        //发送模版，设置上连接工厂
        RabbitTemplate template = new RabbitTemplate(cf);
        //发送消息
        template.convertAndSend("SpringAMQPRabbitMQ-exchange", "foo.bar", "Hello, SpringAMQPRabbitMQ!");

        Thread.sleep(1000);
        container.stop();
    }
}