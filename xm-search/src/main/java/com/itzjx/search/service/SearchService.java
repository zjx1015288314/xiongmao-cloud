package com.itzjx.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.itzjx.common.utils.JsonUtils;
import com.itzjx.common.vo.PageResult;
import com.itzjx.item.pojo.*;
import com.itzjx.search.client.BrandClient;
import com.itzjx.search.client.CategoryClient;
import com.itzjx.search.client.GoodsClient;
import com.itzjx.search.client.SpecificationClient;
import com.itzjx.search.pojo.Goods;
import com.itzjx.search.pojo.SearchRequest;
import com.itzjx.search.pojo.SearchResult;
import com.itzjx.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MatchQuery;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhaojiexiong
 * @create 2020/5/27
 * @since 1.0.0
 */
@Slf4j
@Service
public class SearchService {
    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu) {
        //get spu id
        Long spuId = spu.getId();

        //TODO   search field :title,category,brand,spec
        //query category names by cids
        List<String> categories = categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //query brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());

        //TODO the price collection of all skus    and     json of  sku list
        List<Sku> skuList = goodsClient.querySkuBySpuId(spuId);
        // 遍历skus，获取价格集合
        List<Long> prices = new ArrayList<>();
        //only need some fileds for Sku ex:id,title,price,image
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        skuList.forEach(sku ->{
            prices.add(sku.getPrice());
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", sku.getId());
            skuMap.put("title", sku.getTitle());
            skuMap.put("price", sku.getPrice());
            skuMap.put("image", StringUtils.isNotBlank(sku.getImages()) ? StringUtils.split(sku.getImages(), ",")[0] : "");
            skuMapList.add(skuMap);
        });

        //TODO all searchable spec params
        List<SpecParam> params = specificationClient.queryParams(null, spu.getCid3(), null, true);
        //query spec detail
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spuId);
        //get generic spec params
        Map<Long, String> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //get special spec params
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {});
        //spec map:  key means name of spec params, value means value of spec params
        Map<String,Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            String key = param.getName();
            Object value = "";
            if (param.getGeneric()){
                value = genericSpec.get(param.getId());
                //for num filed, we usual qurey by range(ex: phone size : less than 5.7 , 5.7-6.5   more than 6.5 ),
                // so we put a range value when inflate index
                if (param.getNumeric()){
                    // num to segment
                    value = chooseSegment(value.toString(),param);
                }
            }else{
                value = specialSpec.get(param.getId());
            }
            specs.put(key,value);
        }
        //query field
        String all = spu.getTitle() + StringUtils.join(categories,"") + brand.getName();
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spuId);
        goods.setAll(all);  //TODO   search field :title,category,brand,spec
        goods.setPrice(prices); //TODO the price collection of all skus
        goods.setSkus(JsonUtils.serialize(skuMapList)); //TODO json of  sku list
        goods.setSpecs(specs); //TODO all searchable spec params
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 从elasticsearch中搜索数据
     * 一定是将通过搜索关键字以及过滤条件查询后的结果数据进行聚合分类，而不是将所有的品牌或分类进行聚合。
     * 这样的话那么会有非常多的聚合结果（并且这些聚合结果并不全是用户需要的）
     * 将搜索到的数据按照分类id，品牌id进行聚合，然后通过聚合的一个个分类id、品牌id去数据库查询对应的分类
     * 和品牌，组成一个集合返回给前端进行渲染。
     * @param request
     * @return
     */
    public PageResult<Goods> search(SearchRequest request){
        String key = request.getKey();
        //key means query condition,return null if key is empty
        if (StringUtils.isEmpty(key)){
            return null;
        }
        int page = request.getPage() - 1;
        int size = request.getSize();
        //new
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //0.result filter
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subtitle","skus"},null));
        //1.page helper
        queryBuilder.withPageable(PageRequest.of(page,size));
        //2.query filter
        QueryBuilder basicQuery= buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);
        //3.aggregation brand and category
        //3.1 agg catogory
        String categoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //3.2 agg brand
        String brandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //4.query    query aggregation through template instead of goodsRepository
//        Page<Goods> result = goodsRepository.search(queryBuilder.build());
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

        //5.resolve result
        //5.1 resolve page result
        long total = result.getTotalElements();
        int totalPage = result.getTotalPages();
        List<Goods> goodsList = result.getContent();
        //5.2 resolve aggregation result
        Aggregations aggs = result.getAggregations();
        List<Category> categories = parseCategoryAgg(aggs.get(categoryAggName));
        List<Brand> brands = parseBrandAgg(aggs.get(brandAggName));

        //6.agg specs
        List<Map<String,Object>> specs = null;
        if (categories != null  && categories.size() == 1){
            //agg specs when category exist  and count == 1
            specs = buildSpecificationAgg(categories.get(0).getId(),basicQuery);
        }
//        return new PageResult<>(total,totalPage,goodsList);
        return new SearchResult(total,totalPage,goodsList,categories,brands,specs);  //SearchResult extend PageResult
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        //create bool query
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //query condition
        queryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()));
        //filter condition
        Map<String,String> map = request.getFilter();
        for (Map.Entry<String,String> entry : map.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            //the keys of category or brand is diffrent from the key of spec ex.   spec.内存.keyword
            if (!"cid".equals(key) && !"brand".equals(key)){
                key = "specs." + key + ".keyword";
            }
            queryBuilder.filter(QueryBuilders.termQuery(key,value));
        }
        return queryBuilder;
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = null;
        //1.查询需要聚合的规格参数
        List<SpecParam> params = specificationClient.queryParams(null, cid, true, null);
        //2.聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //2.1
        queryBuilder.withQuery(basicQuery);
        //2.2
        for (SpecParam param : params) {
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name)
                    .field("specs." + name + ".keyword"));
        }
        //获取结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //解析结果
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : params) {
            //get spec name
            String name = param.getName();
            StringTerms terms = aggs.get(name);
            //put option into map
            Map<String,Object> map = new HashMap<>();
            map.put("k",name);
            map.put("option",terms.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList()));

            specs.add(map);
        }
        return specs;
    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().
                    map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(ids);
            return brands;
        }catch (Exception e){
            log.error("[搜索服务] 查询品牌异常:",e);
            return null;
        }
    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().
                    map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
            return categories;
        }catch (Exception e){
            log.error("[搜索服务] 查询分类异常:",e);
            return null;
        }

    }

    /**
     * search-service needs to update index when item-service create/update goods by goodsRepository.save again
     * @param spuId
     */
    public void  createOrUpdateIndex(Long spuId){
        //query spu
        Spu spu = goodsClient.querySpuById(spuId);
        //build goods
        Goods goods = buildGoods(spu);
        //put into index
        goodsRepository.save(goods);
    }

    /**
     * similar to createOrUpdateIndex()
     * @param spuId
     */
    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
