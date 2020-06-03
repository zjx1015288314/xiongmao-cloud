package com.itzjx.order.controller;

import com.itzjx.order.dto.OrderDTO;
import com.itzjx.order.enums.PayState;
import com.itzjx.order.pojo.Order;
import com.itzjx.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhaojiexiong
 * @create 2020/6/2
 * @since 1.0.0
 */
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO){
            return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    /**
     *根据id查询订单
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id){
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }

    /**
     * 创建支付链接
     * @param orderId
     * @return
     */
    @GetMapping("/url/{id]")
    public ResponseEntity<String> createPayUrl(@PathVariable("id") Long orderId){
        return  ResponseEntity.ok(orderService.createPayUrl(orderId));
    }

    @GetMapping("/state/{id}")
    public ResponseEntity<PayState> queryOrderState(@PathVariable("id") Long orderId){
        return ResponseEntity.ok(orderService.queryOrderState(orderId));
    }
}
