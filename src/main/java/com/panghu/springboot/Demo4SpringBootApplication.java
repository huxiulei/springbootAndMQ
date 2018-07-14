package com.panghu.springboot;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @RestController 注解 是 @Controller和@ResponseBody注解的组合..  使用RestController注解的话 当前的控制器中的所有方法返回都是json,相当于在每个方法的返回前加了@ResponseBody
 * @SpringBootApplication注解是 @SpringBootConfiguration,@EnableAutoConfiguration,@ComponentScan的组合注解
 * 		会自动的帮我们注册一些配置,如果想要关闭掉某个配置的话 可以使用 @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
 * 		@EnableAutoConfiguration 会根据类路径下的jar包依赖为当前项目进行自动配置  如 pom.xml中添加了spring-boot-starter-web,则SpringBoot会自动配置 tomcat和springmvc相关依赖
 * 		SpringBoot会自动扫描添加了@SpringBootApplication所在包的同级包以及下级包的Bean  所以这个入口类要放在 根包路径下
 */
@RestController
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
public class Demo4SpringBootApplication implements CommandLineRunner{

	@Autowired
	RabbitTemplate rabbitTemplate;

	public static void main(String[] args) {
		//SpringApplication.run(Demo4SpringBootApplication.class, args);

		//或者可以这么写,这样写的话 可以设置一些属性
		SpringApplication springApplication = new SpringApplication(Demo4SpringBootApplication.class);
		springApplication.setBannerMode(Banner.Mode.OFF);
		springApplication.run(args);
	}


	@RequestMapping("/")
	public String helloWorld(){
		return "Hello Spring Boot!";
	}


	@Bean
	public Queue myQueue(){
		return new Queue("huhuhu-queue");
	}


	// 会被 Receiver接收
	@Override
	public void run(String... strings) throws Exception {
		rabbitTemplate.convertAndSend("huhuhu-queue","来至huhuhu-queue的消息");
	}


}
