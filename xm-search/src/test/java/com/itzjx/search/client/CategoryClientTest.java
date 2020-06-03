package com.itzjx.search.client;

import com.itzjx.common.vo.PageResult;
import com.itzjx.item.pojo.Category;
import com.itzjx.item.vo.SpuVo;
import com.itzjx.search.pojo.Goods;
import com.itzjx.search.service.SearchService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class CategoryClientTest {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    void queryCategoryByIds() {
        List<Category> list = categoryClient.queryCategoryByIds(Arrays.asList(1L, 2L, 3L));
        Assert.assertEquals(3,list.size());
        for (Category catogory : list) {
            System.out.println("category= " + catogory);
        }
    }

    @Test
    void test(){
        int page = 1;
        int rows = 20;
        int size = 0;
        do {
            PageResult<SpuVo> result = goodsClient.querySpuByPage(page, rows, true, null);
            System.out.println(result.getItems());
            List<SpuVo> spus = result.getItems();
            if (CollectionUtils.isEmpty(spus)){
                break;
            }
            List<Goods> goodsList = new ArrayList<>();
            for (SpuVo spuVo: spus){
                Goods goods = searchService.buildGoods(spuVo);
                goodsList.add(goods);
            }
            page++;
            size = spus.size();
        }while (size == 20);
    }
}