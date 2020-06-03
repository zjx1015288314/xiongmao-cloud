package com.itzjx.search.repository;

import com.itzjx.common.vo.PageResult;
import com.itzjx.item.pojo.Spu;
import com.itzjx.item.vo.SpuVo;
import com.itzjx.search.client.GoodsClient;
import com.itzjx.search.pojo.Goods;
import com.itzjx.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {
    @Autowired
    private  GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void testCreateIndex() {
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData(){
        //
        int page = 1;
        int rows = 20;
        int size = 0;
        do {
            PageResult<SpuVo> result = goodsClient.querySpuByPage(page, rows, true, null);
            List<SpuVo> spus = result.getItems();
            System.out.println(spus);
            if (CollectionUtils.isEmpty(spus)){
                break;
            }
            List<Goods> goodsList = spus.stream().map(searchService::buildGoods).collect(Collectors.toList());
            //put into index database
            goodsRepository.saveAll(goodsList);
            page++;
            size = spus.size();
        }while (size == 20);



    }


}