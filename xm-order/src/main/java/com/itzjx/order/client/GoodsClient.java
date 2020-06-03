package com.itzjx.order.client;

import com.itzjx.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author zhaojiexiong
 * @create 2020/6/2
 * @since 1.0.0
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
