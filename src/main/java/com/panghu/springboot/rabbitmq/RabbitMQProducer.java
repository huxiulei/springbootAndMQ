package com.panghu.springboot.rabbitmq;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * <url>http://www.h3399.cn/201707/108715.html</url>
 * 基本概念 :
 * ConnectionFactory、Connection、Channel
 * ConnectionFactory、Connection、Channel 都是 RabbitMQ 对外提供的 API 中最基本的对象。Connection 是 RabbitMQ 的 socket 链接，它封装了 socket 协议相关部分逻辑。ConnectionFactory 为 Connection 的制造工厂。
 * Channel 是我们与 RabbitMQ 打交道的最重要的一个接口，我们大部分的业务操作是在 Channel 这个接口中完成的，包括定义 Queue、定义 Exchange、绑定 Queue 与 Exchange、发布消息等。
 * Broker： 简单来说就是消息队列服务器实体。
 * Exchange： 消息交换机，它指定消息按什么规则，路由到哪个队列。
 * Queue： 消息队列载体，每个消息都会被投入到一个或多个队列。
 * Binding： 绑定，它的作用就是把 exchange 和 queue 按照路由规则绑定起来。
 * Routing Key： 路由关键字，exchange 根据这个关键字进行消息投递。
 * vhost： 虚拟主机，一个 broker 里可以开设多个 vhost，用作不同用户的权限分离。
 * producer： 消息生产者，就是投递消息的程序。
 * consumer： 消息消费者，就是接受消息的程序。
 * channel： 消息通道，在客户端的每个连接里，可建立多个 channel，每个 channel 代表一个会话任务。
 * 由 Exchange，Queue，RoutingKey 三个才能决定一个从 Exchange 到 Queue 的唯一的线路。
 */
public class RabbitMQProducer {
    public final static String QUEUE_NAME = "rabbitMQ.test";
    public static final Logger logger = LoggerFactory.getLogger(RabbitMQProducer.class);

    public static void main(String[] args) throws Exception {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ相关信息
        factory.setHost("192.168.32.21");
        factory.setUsername("icomm");
        factory.setPassword("icomm");
        //factory.setPort(5672);
        // 每个 virtual host 本质上都是一个 RabbitMQ Server，拥有它自己的 queue，exchagne，和 bings rule 等等。这保证了你可以在多个不同的 application 中使用 RabbitMQ。
        //factory.setVirtualHost("/icomm");
        // 或者通过uri的形式设置 amqp://userName:password@hostName:portNumber[/virtualHost]
        //factory.setUri("amqp://icomm:icomm@192.168.32.21:5672");
        // 自动重连 Network connection between clients and RabbitMQ nodes can fail
        factory.setAutomaticRecoveryEnabled(true); // 默认是true
        factory.setNetworkRecoveryInterval(10000); // 默认是5s 重连的间隔时间
         // 会自动选择一个
//        List<Address> addressList = new ArrayList<>();
//        addressList.add(new Address("192.168.32.21",5672));
//        addressList.add(new Address("192.168.32.21",5673));



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
         注意: queueDeclare是幂等的，也就是说，消费者和生产者，不论谁先声明，都只会有一个queue

         再次注意: 这里只是声明了queue, 然后RabbitMQCustomer消费的时候 也有这一句.. 看似生产者是将消息是发送到了queue,消费者又从queue中获取
         其实并非如此,消息还是发送到了exchange中, 只是此处会默认一个exchange(可以从RabbitMQ控制台中查看).. 消费规则是如果有多个customer则平均消费queue中数据
         */
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        String message = "Hello RabbitMQ";

        //发送消息到队列中
        /**
         * basicPublish第一个参数为交换机名称、
         *             第二个参数为队列映射的路由key、
         *             第三个参数为消息的其他属性、
         *             第四个参数为发送信息的主体
         *
         * 如果要让消息在RabbitMQ服务器重启后依然存在的话  除了要在queueDeclare的时候设置duiable为true, 此处basicPublist的props也要设置MessageProperties.PERSISTENT_TEXT_PLAIN
         */
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        logger.info("RabbitMQProducer 生产消息 : {}", message);


        //关闭通道和连接
        channel.close();
        connection.close();
    }
}