package com.itzjx.user.controller;

import com.itzjx.user.pojo.User;
import com.itzjx.user.service.UserService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author zhaojiexiong
 * @create 2020/5/31
 * @since 1.0.0
 */
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 校验数据
     * @param data
     * @param type 1: username     2:phoneNumber
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data,
                     @PathVariable("type") Integer type){

        return ResponseEntity.ok(userService.checkData(data,type));
    }

    /**
     * send message registry code when register user
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 用户注册
     * @Valid 使用hibernate-validator做后台数据格式校验
     * @param user
     * @param code
     * @return
     */
    @PostMapping(value = "register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code") String code){

        Boolean flag = userService.register(user,code);
        if(flag == null || !flag){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return new  ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<User> queryUser(@RequestParam("username") String username,
                                          @RequestParam("password") String password){

        User user = this.userService.queryUser(username,password);
        if(user == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(user);
    }

}
