package com.itzjx.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itzjx.common.dto.CartDTO;
import com.itzjx.common.enums.ExceptionEnum;
import com.itzjx.common.exception.XmException;
import com.itzjx.common.vo.PageResult;
import com.itzjx.item.vo.SpuVo;
import com.itzjx.item.mapper.*;
import com.itzjx.item.pojo.Sku;
import com.itzjx.item.pojo.Spu;
import com.itzjx.item.pojo.SpuDetail;
import com.itzjx.item.pojo.Stock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<SpuVo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 搜索条件
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        example.setOrderByClause("last_update_time DESC");

        // 分页条件
        PageHelper.startPage(page, rows);

        // 执行查询
        List<Spu> spus = this.spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spus)) {
            throw new XmException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //解析查询结果
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);

        List<SpuVo> spuVos = new ArrayList<>();
        spus.forEach(spu -> {
            SpuVo spuVo = new SpuVo();
            // copy共同属性的值到新的对象
            BeanUtils.copyProperties(spu, spuVo);
            // 查询分类名称
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuVo.setCname(StringUtils.join(names, "/"));

            // 查询品牌的名称
            spuVo.setBname(brandService.queryBrandById(spu.getBrandId()).getName());

            spuVos.add(spuVo);
        });
        return new PageResult<>(pageInfo.getTotal(), spuVos);
    }

    /**
     * 新增商品 ：要新增多个表(spu,spuDetail,sku,stock)，逻辑较为复杂
     * @param spu
     */
    @Transactional
    public void saveGoods(Spu spu) {
        // 新增spu
        // 设置默认字段
        spu.setId(null);
        spu.setSaleable(true);
        spu.setValid(false);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        int count = this.spuMapper.insert(spu);
        if (count == 0) {
            throw new XmException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        // 新增spuDetail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        this.spuDetailMapper.insert(spuDetail);

        //新增商品和库存
        saveSkuAndStock(spu);

        //向消息队列发送消息
        this.sendMessage(spu.getId(), "insert");
    }

    private void saveSkuAndStock(Spu spu) {
        int count = 0;
        // 新增sku
        List<Sku> skus = spu.getSkus();
        List<Stock> stocks = new ArrayList<>();
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            count = skuMapper.insert(sku);
            if (count == 0) {
                throw new XmException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stocks.add(stock);
        }

        //批量新增库存
        count = stockMapper.insertList(stocks);
        if (count == 0) {
            throw new XmException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    /**
     * 根据spuId查询spuDetail
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail == null) {
            throw new XmException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    /**
     * 根据spuId查询sku的集合
     * @param spuId
     * @return
     */
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skus)) {
            throw new XmException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //查询库存
        loadStockInSku(skus);
        return skus;
    }

    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null) {
            throw new XmException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        }
        // 查询以前sku
        List<Sku> skus = this.querySkusBySpuId(spu.getId());

        if (!CollectionUtils.isEmpty(skus)) {
            // 如果以前存在，则删除
            skus.forEach(sku -> {
                //删除sku对应的库存
                this.stockMapper.deleteByPrimaryKey(sku.getId());
            });
            //删除sku
            Sku record = new Sku();
            record.setSpuId(spu.getId());
            this.skuMapper.delete(record);
        }
        // 新增sku和库存
        saveSkuAndStock(spu);

        // 更新spu
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count == 0) {
            throw new XmException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        // 更新spu详情
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count == 0) {
            throw new XmException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        //向消息队列发送消息
        this.sendMessage(spu.getId(), "update");
    }

    public Spu querySpuById(Long id) {
        //query spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new XmException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //query sku
        spu.setSkus(querySkusBySpuId(id));
        //query spu detail
        spu.setSpuDetail(querySpuDetailBySpuId(id));
        return spu;

    }

    /**
     * 向rabbitmq中发送消息
     * @param id
     * @param type
     */
    public void sendMessage(Long id, String type) {
        //发送消息
        try {
            amqpTemplate.convertAndSend("item." + type, id);
        } catch (Exception e) {
            log.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }

    /**
     * 通过skuid查询sku
     */
    public Sku querySkuById(Long id) {
        Sku sku = this.skuMapper.selectByPrimaryKey(id);
        return sku;
    }

    public List<Sku> querySkusByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new XmException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        loadStockInSku(skus);
        return skus;
    }

    /**
     * 查询每个sku对应的库存并且设置sku库存属性
     * @param skus
     */
    private void loadStockInSku(List<Sku> skus) {

        skus.forEach(s -> {
            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
            if (stock == null) {
                throw new XmException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
            }
            s.setStock(stock.getStock());
        });
    }

    /**
     * 减库存
     * @param carts
     */
    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            // 不能用if判断来实现减库存，当线程很多的时候，有可能引发超卖问题
            // 加锁也不可以  性能太差，只有一个线程可以执行，当搭了集群时synchronized只锁住了当前一个tomcat
            //redis/zeekooper中分布式锁,  也可以乐观锁 update tb_stock set stock = stock - num where id = * and stock >= num;
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (count != 1) {
                throw new XmException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}
