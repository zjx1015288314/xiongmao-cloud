package com.itzjx.page.controller;

import com.itzjx.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author zhaojiexiong
 * @create 2020/5/28
 * @since 1.0.0
 */
@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id") Long spuId, Model model){
        //get model data
        Map<String,Object> attribute = pageService.loadModel(spuId);
        model.addAllAttributes(attribute);
        //将商品详情页进行静态化处理
        pageService.createHtml(spuId);
        return "item";
    }
}
