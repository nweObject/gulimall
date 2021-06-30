package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
             categoryEntity.getParentCid() == 0
        ).map((menu)->{
            menu.setChildren(getChildrens(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());




        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        String catalogJson = opsForValue.get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDb();
            catalogJson = JSON.toJSONString(catalogJsonFromDb);
            opsForValue.set("catalogJson", catalogJson);
        }
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
    }


    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {
        //查询所有
        List<CategoryEntity> cateLogList = baseMapper.selectList(new QueryWrapper<CategoryEntity>());
        //查询一级分类
        List<CategoryEntity> categoryLevel1 = getParentCategoryList(cateLogList, 0L);
        if (categoryLevel1 == null) {
            return null;
        }
        //遍历一级分类并生成Map
        return categoryLevel1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //获取二级分类
            List<CategoryEntity> categoryLevel2 = getParentCategoryList(cateLogList, v.getCatId());
            if (categoryLevel2 != null) {

                return categoryLevel2.stream().map(l2 -> {
                    //三级分类
                    List<CategoryEntity> categoryLevel3 = getParentCategoryList(cateLogList, l2.getCatId());
                    List<Catelog2Vo.Category3Vo> category3Vos = null;
                    if (categoryLevel3 != null) {
                        category3Vos = categoryLevel3.stream().map(l3 -> {
                            return new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        }).collect(Collectors.toList());
                    }
                    return new Catelog2Vo(v.getCatId().toString(), category3Vos, l2.getCatId().toString(), l2.getName());
                }).collect(Collectors.toList());
            }else {
                Catelog2Vo catelog2Vo = new Catelog2Vo();
                catelog2Vo.setCatalog1Id(v.getCatId().toString());
                return Arrays.asList(catelog2Vo);
            }
        }));
    }

    List<CategoryEntity> getParentCategoryList(List<CategoryEntity> categoryEntityList, Long parent_cid) {
        return categoryEntityList.stream().filter(item ->
             item.getParentCid().equals(parent_cid)
        ).collect(Collectors.toList());
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;

    }


    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity,all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            //2、菜单的排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }



}