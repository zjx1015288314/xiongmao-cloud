package com.itzjx.item.pojo;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "tb_sku")
public class Sku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long spuId;
    private String title;
    private String images;
    private Long price;
    private String ownSpec;// 商品特殊规格的键值对
    private String indexes;// 商品特殊规格的下标
    private Boolean enable;// 是否有效，逻辑删除用
    private Date createTime;// 创建时间
    private Date lastUpdateTime;// 最后修改时间
    @Transient
    private Integer stock;// 库存


    @Override
    public String toString() {
        return "Sku{" +
                "id=" + id +
                ", spuId=" + spuId +
                ", title='" + title + '\'' +
                ", images='" + images + '\'' +
                ", price=" + price +
                ", ownSpec='" + ownSpec + '\'' +
                ", indexes='" + indexes + '\'' +
                ", enable=" + enable +
                ", createTime=" + createTime +
                ", lastUpdateTime=" + lastUpdateTime +
                ", stock=" + stock +
                '}';
    }
}
