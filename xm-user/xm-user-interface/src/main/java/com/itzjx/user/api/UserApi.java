package com.itzjx.user.api;

import com.itzjx.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zhaojiexiong
 * @create 2020/6/1
 * @since 1.0.0
 */
public interface UserApi {
    @GetMapping("query")
    public User queryUser(@RequestParam("username") String username,
                          @RequestParam("password") String password);
}
