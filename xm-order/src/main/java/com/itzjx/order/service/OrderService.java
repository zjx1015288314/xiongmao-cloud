package com.itzjx.order.service;

import com.itzjx.auth.entity.UserInfo;
import com.itzjx.common.dto.CartDTO;
import com.itzjx.common.enums.ExceptionEnum;
import com.itzjx.common.exception.XmException;
import com.itzjx.common.utils.IdWorker;
import com.itzjx.item.pojo.Sku;
import com.itzjx.order.client.AddressClient;
import com.itzjx.order.client.GoodsClient;
import com.itzjx.order.dto.AddressDTO;
import com.itzjx.order.dto.OrderDTO;
import com.itzjx.order.enums.OrderStatusEnum;
import com.itzjx.order.enums.PayState;
import com.itzjx.order.interceptors.UserInterceptor;
import com.itzjx.order.mapper.OrderDetailMapper;
import com.itzjx.order.mapper.OrderMapper;
import com.itzjx.order.mapper.OrderStatusMapper;
import com.itzjx.order.pojo.Order;
import com.itzjx.order.pojo.OrderDetail;
import com.itzjx.order.pojo.OrderStatus;
import com.itzjx.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhaojiexiong
 * @create 2020/6/2
 * @since 1.0.0
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {

        //1
        Order order = new Order();

        //1.1订单编号生成,填充基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());
        //1.2用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);

        //1.3收货人信息
        AddressDTO addr = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverState(addr.getState());
        order.setReceiverZip(addr.getZipCode());
        order.setReceiverMobile(addr.getPhone());

        //1.4金额
        //List<CartDTO>转为Map，key为skuId，value为num
        Map<Long,Integer> numsMap = orderDTO.getCarts().stream().collect(Collectors.toMap(CartDTO::getSkuId,CartDTO::getNum));
        Set<Long> ids = numsMap.keySet();
        //2.新增订单详情
        List<Sku> skus = goodsClient.querySkusByIds(new ArrayList<>(ids));
        //准备orderDetail集合
        List<OrderDetail> details = new ArrayList<>();
        Long totalPay = 0L;
        for (Sku sku : skus) {
            totalPay += sku.getPrice() * numsMap.get(sku.getId());

            //封装orderDetail
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            detail.setNum(numsMap.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());

            details.add(detail);
        }
        order.setTotalPay(totalPay);
        //实付金额= 总金额 + 邮费 - 优惠金额
        order.setActualPay(totalPay + order.getPostFee() - 0);
        // 1.5 写入数据库
        int count = orderMapper.insertSelective(order);
        if(count != 1){
            log.error("[创建订单] 创建订单失败，orderID:{}", orderId);
            throw new XmException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 2 新增订单详情
        count = orderDetailMapper.insertList(details);
        if(count != details.size()){
            log.error("[创建订单] 创建订单失败，orderID:{}", orderId);
            throw new XmException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 3 新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        count = statusMapper.insertSelective(orderStatus);
        if(count != 1){
            log.error("[创建订单] 创建订单失败，orderID:{}", orderId);
            throw new XmException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 4 减库存 -- 需要调用商品微服务的减库存功能,选同步调用(同步调用：商品微服务提供接口，通过feign调用)，(异步调用：通过rabbitmq)
        // 传递商品id和数量两个参数
        List<CartDTO> cartDTOS = orderDTO.getCarts();
        goodsClient.decreaseStock(cartDTOS);
        return orderId;
    }

    public Order queryOrderById(Long id) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null){
            throw new XmException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        // 查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> orderDetails = orderDetailMapper.select(detail);
        if(CollectionUtils.isEmpty(orderDetails)){
            throw new XmException(ExceptionEnum.ORDER_DETAIL_NOT_FOUNT);
        }
        order.setOrderDetails(orderDetails);

        // 查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(id);
        if(orderStatus == null){
            throw new XmException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    public String createPayUrl(Long orderId) {
        //查询订单
        Order order = queryOrderById(orderId);
        //判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UN_PAY.value()){
            throw new XmException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //订单获取实际支付金额
        Long actualPay = order.getActualPay();
        //商品描述
        OrderDetail orderDetail = order.getOrderDetails().get(0);
        String desc = orderDetail.getTitle();
        payHelper.createPayUrl(orderId,actualPay,desc);
        return null;
    }

    public PayState handleNotify(Map<String, String> result) {
        payHelper.isSuccess(result);

        payHelper.isValidSign(result);

        // 校验金额
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if(StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(tradeNo)){
            throw new XmException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        // 3.1 获取结果中的金额
        Long totalFee = Long.valueOf(totalFeeStr);
        // 3.2 获取订单金额
        Long orderId = Long.valueOf(tradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(totalFee != order.getActualPay()){
            // 金额不符
            throw new XmException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        String state = result.get("trade_state");
        if("SUCCESS".equals(state)){
            // 支付成功
            // 修改订单状态
            OrderStatus status = new OrderStatus();
            status.setStatus(OrderStatusEnum.PAYED.value());
            status.setOrderId(orderId);
            status.setPaymentTime(new Date());
            int count = statusMapper.updateByPrimaryKeySelective(status);
            if(count != 1){
                throw new XmException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
            }
            // 返回成功
            return PayState.SUCCESS;
        }

        if("NOTPAY".equals(state) || "USERPAYING".equals(state)){
            return PayState.NOT_PAY;
        }
        return PayState.FAIL;
    }

    public PayState queryOrderState(Long orderId) {
        //查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        //判断是否支付
        if (status != OrderStatusEnum.UN_PAY.value()){
            return PayState.SUCCESS;
        }
        //如果未支付，去微信查询支付状态
        return payHelper.queryPayState(orderId);
    }
}
