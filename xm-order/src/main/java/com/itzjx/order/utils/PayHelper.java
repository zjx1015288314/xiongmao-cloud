package com.itzjx.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.itzjx.common.enums.ExceptionEnum;
import com.itzjx.common.exception.XmException;
import com.itzjx.order.config.PayConfig;
import com.itzjx.order.enums.OrderStatusEnum;
import com.itzjx.order.enums.PayState;
import com.itzjx.order.mapper.OrderMapper;
import com.itzjx.order.mapper.OrderStatusMapper;
import com.itzjx.order.pojo.Order;
import com.itzjx.order.pojo.OrderStatus;
import com.itzjx.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;

    @Autowired
    private PayConfig payConfig;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    public String createPayUrl(Long orderId, Long totalPay, String desc) {
        String key = "xm.pay.url." + orderId;
        try {
            String url = redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(url)) {
                return url;
            }
        } catch (Exception e) {
            log.error("查询缓存付款链接异常,订单编号：{}", orderId, e);
        }

        try {
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body", desc);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            //货币
            data.put("fee_type", "CNY");
            //金额，单位是分
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端IP（乐优商城的IP）
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址，付款成功后的接口
            data.put("notify_url", payConfig.getNotifyUrl());
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");
            //商品id,使用假数据
            data.put("product_id", "1234567");

            // 利用wxPay工具,完成下单
            Map<String, String> result = wxPay.unifiedOrder(data);
            // 判断通信和业务标示
            isSuccess(result);
            // 校验签名
            isValidSign(result);

            String url = result.get("code_url");
            // 将付款地址缓存，时间为10分钟
            redisTemplate.opsForValue().set(key, url, 10, TimeUnit.MINUTES);
            return url;
        } catch (Exception e) {
            log.error("创建预交易订单异常", e);
            return null;
        }
    }

    public void isSuccess(Map<String, String> result) {
        // 判断通信标识
        String returnCode = result.get("return_code");
        if ("FAIL".equals(returnCode)) {
            // 通信失败
            log.error("[微信下单] 微信下单通信失败,失败原因:{}", result.get("return_msg"));
            throw new XmException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }

        // 判断业务标示
        String resultCode = result.get("result_code");
        if ("FAIL".equals(resultCode)) {
            // 通信失败
            log.error("[微信下单] 微信下单业务失败,错误码:{}, 错误原因:{}",
                    result.get("err_code"), result.get("err_code_des"));
            throw new XmException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
    }

    public void isValidSign(Map<String, String> result) {
        // 校验签名: 重新生成签名，和传过来的签名进行比较
        try {
            String sign1 = WXPayUtil.generateSignature(result, payConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
            String sign2 = WXPayUtil.generateSignature(result, payConfig.getKey(), WXPayConstants.SignType.MD5);

            String sign = result.get("sign");
            if (!StringUtils.equals(sign, sign1) && !StringUtils.equals(sign, sign2)) {
                //签名有误
                throw new XmException(ExceptionEnum.INVALID_SIGN_ERROR);
            }
        } catch (Exception e) {
            log.error("[微信支付] 校验签名失败，数据：{}", result);
            throw new XmException(ExceptionEnum.INVALID_SIGN_ERROR);
        }
    }

    /**
     * 查询订单状态
     * @param orderId
     * @return
     */
    public PayState queryPayState(Long orderId) {
        try {
            Map<String, String> data = new HashMap<>();
            // 订单号
            data.put("out_trade_no", orderId.toString());
            Map<String, String> result = wxPay.orderQuery(data);
            // 校验状态
            isSuccess(result);
            // 校验签名
            isValidSign(result);

            // 校验金额
            String totalFeeStr = result.get("total_fee");
            String tradeNo = result.get("out_trade_no");
            if(StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(tradeNo)){
                throw new XmException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            // 3.1 获取结果中的金额
            Long totalFee = Long.valueOf(totalFeeStr);
            // 3.2 获取订单金额
            Order order = orderMapper.selectByPrimaryKey(orderId);
            if(totalFee != order.getActualPay()){
                // 金额不符
                throw new XmException(ExceptionEnum.INVALID_ORDER_PARAM);
            }

            String state = result.get("trade_state");
            if ("SUCCESS".equals(state)) {
                // success，则认为付款成功,修改订单状态
                updateStatus(orderId, 2);
                return PayState.SUCCESS;
            } else if (StringUtils.equals("USERPAYING", state) || StringUtils.equals("NOTPAY", state)) {
                // 未付款或正在付款，都认为是未付款
                return PayState.NOT_PAY;
            } else {
                // 其它状态认为是付款失败
                return PayState.FAIL;
            }
        } catch (Exception e) {
            log.error("查询订单状态异常", e);
            return PayState.NOT_PAY;
        }
    }

    /**
     * 更新订单状态
     * @param orderId
     * @param status
     */
    private void updateStatus(Long orderId,Integer status){
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setStatus(status);
        orderStatus.setOrderId(orderId);
        orderStatus.setPaymentTime(new Date());
        int count = statusMapper.updateByPrimaryKeySelective(orderStatus);
        if (count != 1){
            throw new XmException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
    }
}
