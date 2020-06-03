package com.itzjx.search.controller;

import com.itzjx.common.vo.PageResult;
import com.itzjx.search.pojo.Goods;
import com.itzjx.search.pojo.SearchRequest;
import com.itzjx.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhaojiexiong
 * @create 2020/5/28
 * @since 1.0.0
 */
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest request){
        return ResponseEntity.ok(searchService.search(request));
    }
}
