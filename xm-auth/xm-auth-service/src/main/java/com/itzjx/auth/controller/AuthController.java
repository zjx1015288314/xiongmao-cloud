package com.itzjx.auth.controller;

import com.itzjx.auth.config.JwtProperties;
import com.itzjx.auth.entity.UserInfo;
import com.itzjx.auth.service.AuthService;
import com.itzjx.auth.utils.JwtUtils;
import com.itzjx.common.enums.ExceptionEnum;
import com.itzjx.common.exception.XmException;
import com.itzjx.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    /**
     * 登录授权
     * @param username
     * @param password
     * @return
     */
    @PostMapping("accredit")
    public ResponseEntity<Void> authentication(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response) {
        // 登录校验
        String token = authService.authentication(username, password);
        // 将token写入cookie,并指定httpOnly为true，防止通过JS获取和修改
        CookieUtils.setCookie(request, response, prop.getCookieName(),
                token, prop.getCookieMaxAge(),  true);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 从cookie中取出用户信息，并进行验证该用户是否已经登录，
     * 若cookie中携带的token中包含有用户信息则校验通过
     * @CookieValue注解：是从cookie中按照cookie名称取得cookie值
     * @param token
     * @return
     */
    @GetMapping(value = "verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("LY_TOKEN") String token,
                                               HttpServletRequest request, HttpServletResponse response){
        if (StringUtils.isBlank(token)){
            throw new XmException(ExceptionEnum.UNAUTHORISED);
        }
        try {
            //从cookie中携带的token中解析出用户信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 解析成功要重新刷新token
            JwtUtils.generateToken(userInfo,prop.getPrivateKey(),prop.getExpire());
            //更新cookie中携带的token(其实是为了更新token的过期时间，为了能让用户在操作超过30分钟时不退出登录)
            CookieUtils.setCookie(request,response,prop.getCookieName(),token,prop.getCookieMaxAge());
            //解析成功，返回用户信息
            return ResponseEntity.ok(userInfo);
        }catch (Exception e){
            throw new XmException(ExceptionEnum.UNAUTHORISED);
        }
    }
}
