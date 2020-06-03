package com.itzjx.page.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author zhaojiexiong
 * @create 2020/5/28
 * @since 1.0.0
 */
@Controller
public class HelloController {

    @GetMapping("hello")
    public String tHello(Model model){
        model.addAttribute("msg","hello thymeleaf");
        return "hello";
    }
}
