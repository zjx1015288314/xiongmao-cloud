package com.itzjx.cart.service;


import com.itzjx.auth.entity.UserInfo;
import com.itzjx.cart.interceptor.UserInterceptor;
import com.itzjx.cart.pojo.Cart;
import com.itzjx.common.enums.ExceptionEnum;
import com.itzjx.common.exception.XmException;
import com.itzjx.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description
 *
 * @author 文攀 2019/11/12 10:45
 */
@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    static final String KEY_PREFIX = "cart:uid:";

    /**
     *
     * @param cart
     */
    public void addCart(Cart cart) {
        //获取用户登录信息
        UserInfo user = UserInterceptor.getUser();
        //存入redis的key
        String key = KEY_PREFIX + user.getId();
        //获取hash操作对象,这一步就是通过key从redis中取出该用户下的所有商品了
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        //查询要添加到购物车的商品是否存在于redis
        String skuId = cart.getSkuId().toString();
        //获取商品数量
        Integer num = cart.getNum();
        //判断商品是否存在于redis/购物车
        if(hashOps.hasKey(skuId)) {
            // 存在，获取购物车数据
            String json = hashOps.get(skuId).toString();
            //将获取的json字符串格式的商品转化为商品
            cart = JsonUtils.parse(json, Cart.class);
            // 修改购物车数量
            cart.setNum(cart.getNum() + num);
        }
        //将购物车数据写入redis
        hashOps.put(skuId,JsonUtils.serialize(cart));
    }

    /**
     * 查询购物车列表
     * @return
     */
    public List<Cart> queryCartList() {
        //获取登录用户信息
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        //判断购物车是否存在
        if(!redisTemplate.hasKey(key)){
            //不存在，则直接返回
            throw new XmException(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        //取得购物车中所有商品id
        List<Object> carts = hashOps.values();
        //判断是否有数据
        if(CollectionUtils.isEmpty(carts)){
            throw new XmException(ExceptionEnum.CART_NOT_FOUND);
        }
        //查询购物车数据，将从redis中查询到的String类型的购物车商品数据转化为对象集合类型
        return carts.stream().map(o -> JsonUtils.parse(o.toString(),Cart.class)).collect(Collectors.toList());
    }


    /**
     * 更新购物车数量
     * @param skuId
     * @param num
     */
    public void updateNum(Long skuId,Integer num) {
        //获取用户登录信息
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        //hashKey
        String hashKey = skuId.toString();
        //获取hash操作对象
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        //获取购物车信息
        String cartJson = hashOps.get(hashKey).toString();
        Cart cart = JsonUtils.parse(cartJson, Cart.class);
        //更新数量
        cart.setNum(num);
        //写入购物车
        hashOps.put(hashKey,JsonUtils.serialize(cart));
    }

    /**
     * 根据id删除一条购物车数据
     * @param skuId
     */
    public void deleteCart(Long skuId) {
        //获取登录用户信息
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        //从redis中获取该用户的购物车信息
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        hashOps.delete(skuId.toString());
    }
}

