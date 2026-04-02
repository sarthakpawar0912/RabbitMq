package com.sarthak.orderservicerabbit.controller;


import com.sarthak.orderservicerabbit.model.Order;
import com.sarthak.orderservicerabbit.service.OrderProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderProducer producer;

    @PostMapping
    public String createOrder(@RequestBody Order order) {
        producer.placeOrder(order);
        return "Order created successfully";
    }

}
