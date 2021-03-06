package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 fanout表示分发，所有的消费者得到同样的队列信息
 FanoutCustomer1 和 FanoutCustomer2 必须要先处于运行状态(等待获取消息)..   才可以接收到FanoutProduct发出的消息


 RabbitMQ中，有4种类型的Exchange
 direct    通过消息的routing key比较queue的key，相等则发给该queue，常用于相同应用多实例之间的任务分发
 默认类型   本身是一个direct类型的exchange，routing key自动设置为queue name。注意，direct不等于默认类型，默认类型是在queue没有指定exchange时的默认处理方式，发消息时，exchange字段也要相应的填成空字符串“”
 topic    话题，通过可配置的规则分发给绑定在该exchange上的队列，通过地理位置推送等场景适用
 headers    当分发规则很复杂，用routing key不好表达时适用，忽略routing key，用header取代之，header可以为非字符串，例如Integer或者String
 headers 类型的 Exchange 不依赖于 routing key 与 binding key 的匹配规则来路由消息，而是根据发送的消息内容中的 headers 属性进行匹配。
        在绑定 Queue 与 Exchange 时指定一组键值对；当消息发送到 Exchange 时，RabbitMQ 会取到该消息的 headers（也是一个键值对的形式），对比其中的键值对是否完全匹配 Queue 与 Exchange 绑定时指定的键值对；如果完全匹配则消息会路由到该 Queue，否则不会路由到该 Queue。
 fanout    分发给所有绑定到该exchange上的队列，忽略routing key，适用于MMO游戏、广播、群聊等场景
 */
public class FanoutProducer {
    public static final Logger logger = LoggerFactory.getLogger(FanoutProducer.class);
    private static final String EXCHANGE_NAME = "logs";
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

        // 声明exchange
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT,true);

        //分发信息
        for (int i=0;i<5;i++){
            String message="Hello World-"+i;
            // 因为fanout会忽略routingKey,所以这里不设置也没关系
            channel.basicPublish(EXCHANGE_NAME,"xx",null,message.getBytes());
            logger.info("FanoutProducer 生产消息 : {}", message);
        }
        //关闭通道和连接
        channel.close();
        connection.close();
    }
}