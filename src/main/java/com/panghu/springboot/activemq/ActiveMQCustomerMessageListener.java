package com.panghu.springboot.activemq;

import org.springframework.jms.listener.SessionAwareMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * 消费监听  jms规范
 */
public class ActiveMQCustomerMessageListener implements SessionAwareMessageListener<Message> {

    @Override
    public void onMessage(Message message, Session session) throws JMSException {

        if(message instanceof TextMessage){

            System.out.println("consumer get msg : " + ((TextMessage) message).getText());

        }

    }

}
