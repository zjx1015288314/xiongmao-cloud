package com.itzjx.cart.interceptor;

import com.itzjx.auth.entity.UserInfo;
import com.itzjx.auth.utils.JwtUtils;
import com.itzjx.cart.config.JwtProperties;
import com.itzjx.common.utils.CookieUtils;
import com.itzjx.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhaojiexiong
 * @create 2020/6/1
 * @since 1.0.0
 */
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties prop;

    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public UserInterceptor(JwtProperties prop){
        this.prop = prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //get token in the cookie
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
            //resolve token
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());

            //add user attribute  or put userinfo into threadLocal
//            request.setAttribute("user",userInfo);
            tl.set(userInfo);
            return true;
        }catch (Exception e){
            //not login in
            log.error("[购物车服务 解析用户身份失败] ",e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //用完即删除，防止内存泄露
        tl.remove();
    }

    public static UserInfo getUser(){
        return tl.get();
    }
}
