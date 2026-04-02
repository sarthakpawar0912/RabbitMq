package com.sarthak.paymentservicerabbit.service;

import com.sarthak.paymentservicerabbit.config.RabbitConfig;
import com.sarthak.paymentservicerabbit.model.Order;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentConsumer {

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void processPayment(Order order) {
        System.out.println("processing -- " + order.getOrderId());
        if (order.getAmount() > 0) {
            System.out.println("Payment success for order " + order.getOrderId());
        } else {
            System.out.println("Payment failed...");
        }
    }
}
