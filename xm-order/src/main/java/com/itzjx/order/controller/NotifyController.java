package com.itzjx.order.controller;

import com.itzjx.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhaojiexiong
 * @create 2020/6/2
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;

    /**
     * 微信支付成功回调
     * @param result
     * @return
     */
    @PostMapping(value = "wxpay",produces = "application/xml")
    public Map<String,String> hello(@RequestBody Map<String,String> result){
        orderService.handleNotify(result);
        log.info("【支付回调】接收微信支付回调，结果：",result);

        Map<String,String> msg = new HashMap<>();
        msg.put("return_code","SUCCESS");
        msg.put("return_msg","OK");
        return msg;
    }
}
