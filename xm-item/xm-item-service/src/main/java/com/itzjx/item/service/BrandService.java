package com.itzjx.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itzjx.common.enums.ExceptionEnum;
import com.itzjx.common.exception.XmException;
import com.itzjx.common.vo.PageResult;
import com.itzjx.item.mapper.BrandMapper;
import com.itzjx.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;
import java.util.List;

/**
 * description
 */
@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    /**
     * select brand list by page
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @return PageResult<T>
     */
    public PageResult<Brand> queryBrandByPage(int page, int rows, String sortBy, Boolean desc, String key) {
        //开始分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Brand.class);

        //如果查询关键字不为空,则创建模糊查询条件 zhonw
        if(StringUtils.isNotBlank(key)){
            example.createCriteria().andLike("name","%" + key + "%").
                    orEqualTo("letter",key.toUpperCase());
        }
        //排序
        if(StringUtils.isNotBlank(sortBy)){
            String orderByClause = sortBy + (desc?" desc":" asc");
            example.setOrderByClause(orderByClause);
        }
        //查询
        Page<Brand> pageInfo = (Page<Brand>)brandMapper.selectByExample(example);
        return new PageResult<>(pageInfo.getTotal(),pageInfo);
    }

    /**
     * 新增品牌信息,品牌和分类中间表
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增品牌信息
        int count = this.brandMapper.insertSelective(brand);

        if (count != 1){
            throw new XmException(ExceptionEnum.BRAND_SAVE_ERROR);
        }

        //新增品牌和分类中间表
        for (Long cid : cids) {
            this.brandMapper.insertCategoryBrand(cid, brand.getId());
        }
    }

    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        //修改品牌信息
        this.brandMapper.updateByPrimaryKeySelective(brand);

        //修改品牌和分类中间表
        for (Long cid : cids) {
            this.brandMapper.updateCategoryBrand(cid, brand.getId());
        }
    }

    public List<Brand> queryBrandsByCid(Long cid) {
        return this.brandMapper.selectBrandByCid(cid);
    }

    public Brand queryBrandById(Long id){
        Brand brand = this.brandMapper.selectByPrimaryKey(id);
        if (brand == null){
            throw new XmException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)){
            throw new XmException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }

}

