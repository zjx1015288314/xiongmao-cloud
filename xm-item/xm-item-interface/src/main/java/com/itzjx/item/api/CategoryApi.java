package com.itzjx.item.api;

import com.itzjx.item.pojo.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RequestMapping("category")
public interface CategoryApi {

    @GetMapping("names")
    List<String> queryNameByIds(@RequestParam("ids") List<Long> ids);

    /**
     * select categoryList by categoryIds
     * @param ids
     * @return
     */
    @GetMapping("list/ids")
    List<Category> queryCategoryByIds(@RequestParam("ids") List<Long> ids);
}
