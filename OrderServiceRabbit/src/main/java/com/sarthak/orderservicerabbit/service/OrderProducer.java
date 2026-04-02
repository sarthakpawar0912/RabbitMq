package com.sarthak.orderservicerabbit.service;

import com.sarthak.orderservicerabbit.config.RabbitConfig;
import com.sarthak.orderservicerabbit.model.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void placeOrder(Order order) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                order);

        System.out.println("Order Sent " + order.getOrderId());
    }
}
