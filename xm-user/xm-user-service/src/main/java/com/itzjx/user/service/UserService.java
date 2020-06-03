package com.itzjx.user.service;

import com.itzjx.common.enums.ExceptionEnum;
import com.itzjx.common.exception.XmException;
import com.itzjx.common.utils.CodecUtils;
import com.itzjx.common.utils.NumberUtils;
import com.itzjx.user.mapper.UserMapper;
import com.itzjx.user.pojo.User;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhaojiexiong
 * @create 2020/5/31
 * @since 1.0.0
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:phone:";

    public Boolean checkData(String data, Integer type) {
        //判断数据类型
        User user = new User();
        user.setUsername(data);
        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new XmException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        return userMapper.selectCount(user) == 0;
    }

    public void sendCode(String phone) {
        //get code key,put into resdis
        String key =  KEY_PREFIX + phone;
        //get code
        String code = NumberUtils.generateCode(6);
        Map<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);

        //send code
        amqpTemplate.convertAndSend("xm.sms.exchange","sms.verify.code",msg);
        //save code
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
    }

    public Boolean register(User user, String code) {

        //去redis中获取验证码，校验短信验证码
        String cacheCode = (String) redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());

        if(!StringUtils.equals(code,cacheCode)){
            return false;
        }
        //验证码校验通过则生成盐,并且设置到用户上
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        //使用盐对密码进行加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

        // 强制设置不能指定的参数为null
        user.setId(null);
        user.setCreated(new Date());

        //将用户信息添加到数据库
        Boolean flag = userMapper.insertSelective(user) == 1;
        if(flag){
            // 注册成功，删除redis中的记录
            this.redisTemplate.delete(KEY_PREFIX + user.getPhone());
        }
        return flag;
    }

    public User queryUser(String username, String password) {
        User recode = new User();
        recode.setUsername(username);
        User user = userMapper.selectOne(recode);
        //校验用户名
        if(user == null){
            return null;
        }
        //校验密码
        if(!user.getPassword().equals(CodecUtils.md5Hex(password,user.getSalt()))){
            return null;
        }
        return user;
    }
}
