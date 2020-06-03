package com.itzjx.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaojiexiong
 * @create 2020/6/2
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long skuId;
    private Integer num;
}
