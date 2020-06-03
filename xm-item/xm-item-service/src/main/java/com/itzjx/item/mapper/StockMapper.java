package com.itzjx.item.mapper;

import com.itzjx.common.mapper.BaseMapper;
import com.itzjx.item.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * product stock
 */
public interface StockMapper extends BaseMapper<Stock>{
    @Update("UPDATE tb_stock SET stock = stock - #{num} WHERE sku_id = #{id} AND stock >= #{num}")
    int decreaseStock(@Param("id") Long id, @Param("num") Integer num);
}
