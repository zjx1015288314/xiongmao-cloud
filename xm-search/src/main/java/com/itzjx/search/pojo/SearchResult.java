package com.itzjx.search.pojo;

import com.itzjx.common.vo.PageResult;
import com.itzjx.item.pojo.Brand;
import com.itzjx.item.pojo.Category;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SearchResult extends PageResult<Goods> {

    private List<Category> categories;
    private List<Brand> brands;
    private List<Map<String, Object>> specs;

    public SearchResult() {
    }

    public SearchResult(List<Category> categories, List<Brand> brands) {
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResult( Long total, List<Goods> items,List<Category> categories, List<Brand> brands) {
        super(total,items);
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResult( Long total, Integer totalPage, List<Goods> items,List<Category> categories, List<Brand> brands) {
        super(total, totalPage,items);
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResult(List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    public SearchResult(Long total, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    public SearchResult(Long total, Integer totalPage, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}
