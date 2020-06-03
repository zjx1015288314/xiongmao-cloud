package com.itzjx.auth.service;

import com.itzjx.auth.client.UserClient;
import com.itzjx.auth.config.JwtProperties;
import com.itzjx.auth.entity.UserInfo;
import com.itzjx.auth.utils.JwtUtils;
import com.itzjx.common.enums.ExceptionEnum;
import com.itzjx.common.exception.XmException;
import com.itzjx.user.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties properties;

    public String authentication(String username, String password) {
        // 调用微服务，执行查询
        User user = userClient.queryUser(username, password);
        //如果查询结果为null，则直接返回null
        if (user == null) {
            throw new XmException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        try {
            // 如果有查询结果，则生成token
            //通过密钥和过期时间以及用户信息生成token
            String token = JwtUtils.generateToken(new UserInfo(user.getId(), user.getUsername()),
                    properties.getPrivateKey(), properties.getExpire());
            return token;
        } catch (Exception e) {
            throw new XmException(ExceptionEnum.CREATE_TOKEN_ERROR);
        }
    }
}
