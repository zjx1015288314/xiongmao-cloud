package com.itzjx.gateway.filter;

import com.itzjx.auth.utils.JwtUtils;
import com.itzjx.common.utils.CookieUtils;
import com.itzjx.gateway.config.FilterProperties;
import com.itzjx.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhaojiexiong
 * @create 2020/6/1
 * @since 1.0.0
 */
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties prop;

    @Autowired
    private FilterProperties filterProp;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;   //前置过滤器
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        //get context
        RequestContext context = RequestContext.getCurrentContext();
        //get request
        HttpServletRequest request = context.getRequest();
        //get url path
        String path = request.getRequestURI();
        return !isAllowPath(path);
    }

    private boolean isAllowPath(String path) {
        for (String allowPath : filterProp.getAllowPaths()) {
            if (path.startsWith(allowPath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        //get context
        RequestContext context = RequestContext.getCurrentContext();
        //get request
        HttpServletRequest request = context.getRequest();
        //get token in cookie
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        //resolve token
        try {
            JwtUtils.getInfoFromToken(token,prop.getPublicKey());
            //TODO 校验权限
        } catch (Exception e) {
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(403);
        }
        return null;
    }
}
