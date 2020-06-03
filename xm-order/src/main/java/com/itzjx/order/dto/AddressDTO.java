package com.itzjx.order.dto;

import lombok.Data;

/**
 * @author zhaojiexiong
 * @create 2020/6/2
 * @since 1.0.0
 */
@Data
public class AddressDTO {
    private Long id;
    private String name;
    private String phone;
    private String state;
    private String city;
    private String district;
    private String address;
    private String zipCode;
    private Boolean isDefault;
}

