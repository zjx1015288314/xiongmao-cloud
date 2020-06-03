package com.itzjx.item.vo;


import com.itzjx.item.pojo.Sku;
import com.itzjx.item.pojo.Spu;
import com.itzjx.item.pojo.SpuDetail;
import lombok.Data;

import java.util.List;

/**
 * 该类用于拓展spu类
 */
@Data
public class SpuVo extends Spu {

    String cname;// 商品分类名称

    String bname;// 品牌名称

    SpuDetail spuDetail;// 商品详情

    List<Sku> skus;// sku列表

}
