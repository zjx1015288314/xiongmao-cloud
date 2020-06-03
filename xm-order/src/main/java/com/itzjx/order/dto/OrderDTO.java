package com.itzjx.order.dto;

import com.itzjx.common.dto.CartDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zhaojiexiong
 * @create 2020/6/2
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    @NotNull
    private Long addressId;   //收获人地址id
    @NotNull
    private Integer paymentType;  //付款类型
    @NotNull
    private List<CartDTO> carts;   //订单详情
}
