package com.example.assetperform.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String TEST_QUEUE = "test_queue";

    // 延时暂存队列，不设置监听者
    public static final String APPLY_DELAY_QUEUE = "apply_delay_queue";
    
    // 死信交换机 DLX
    public static final String APPLY_DLX_EXCHANGE = "apply_dlx_exchange";

    // 死信路由键
    public static final String APPLY_DLX_ROUTING_KEY = "apply_dlx_routing_key";

    // 死信消费队列
    public static final String APPLY_DLX_QUEUE = "apply_dlx_queue";

    /**
     * 延时暂存队列，绑定死信交换机，TTL设置24小时/30s
     */
    @Bean
    public Queue applyDelayQueue() {
        Map<String, Object> args = new HashMap<>();

        // 核心参数1：消息过期后转给哪个死信交换机
        args.put("x-dead-letter-exchange", APPLY_DLX_EXCHANGE);

        // 转给私信交换机带上的routing key
        args.put("x-dead-letter-routing-key", APPLY_DLX_ROUTING_KEY);

        // 队列中消息的存活时间, dev配置30秒
        args.put("x-message-ttl", 30000);

        return new Queue(APPLY_DELAY_QUEUE, true, false, false, args);
    }

    /**
     * 声明死信交换机
     */
    @Bean
    public DirectExchange applyDlxExchange(){
        return new DirectExchange(APPLY_DLX_EXCHANGE, true, false);
    }

    /**
     * 超时后进入的死信队列
     */
    @Bean 
    public Queue applyDLXQueue() {
        return new Queue(APPLY_DLX_QUEUE, true);
    }

    /**
     * 死信队列绑定到交换机
     */
    @Bean 
    public Binding applyDLXBinding() {
        return BindingBuilder.bind(applyDLXQueue())
                .to(applyDlxExchange())
                .with(APPLY_DLX_ROUTING_KEY);
    }

    // 配置JSON转换
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
