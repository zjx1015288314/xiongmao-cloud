package com.itzjx.page.service;

import com.itzjx.item.pojo.*;
import com.itzjx.page.client.BrandClient;
import com.itzjx.page.client.CategoryClient;
import com.itzjx.page.client.GoodsClient;
import com.itzjx.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author zhaojiexiong
 * @create 2020/5/29
 * @since 1.0.0
 */
@Slf4j
@Service
public class PageService {
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${xm.page.destPath}")
    private String destPath;

    public Map<String, Object> loadModel(Long spuId) {
        Map<String,Object> model = new HashMap<>();

        //query spu
        Spu spu = goodsClient.querySpuById(spuId);
        //get skus
        List<Sku> skus = spu.getSkus();
        //get detail
        SpuDetail spuDetail = spu.getSpuDetail();
        //query brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //query category
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //query spec params
        List<SpecGroup> specs = specificationClient.querySpecsByCid(spu.getCid3());

        model.put("title",spu.getTitle());
        model.put("subTitle",spu.getSubTitle());
        model.put("skus",skus);
        model.put("spuDetail",spuDetail);
        model.put("brand",brand);
        model.put("categories",categories);
        model.put("specs",specs);
        return model;
    }

    public void createHtml(Long spuId){
        //
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        //outputstream
        File dest = new File(destPath, spuId + ".html");
        //delete old html when re-new static html
        if (dest.exists()){
            dest.delete();
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(dest, "UTF-8");
            templateEngine.process("item",context,writer);
        } catch (Exception e) {
            log.error("[静态页服务] 生成静态页异常",e);
        }finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void deleteHtml(Long spuId){
        File dest = new File(destPath, spuId + ".html");
        if (dest.exists()){
            dest.delete();
        }
    }
}
