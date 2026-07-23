package com.bookcycle.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookcycle.backend.entity.Category;
import com.bookcycle.backend.mapper.CategoryMapper;
import com.bookcycle.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> listAll() {
        // 按 sort_order 排序
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order");
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<Category> listParentCategories() {
        // 查询 parent_id 为 NULL 或 0 的记录
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.isNull("parent_id")
              .or()
              .eq("parent_id", 0)
              .orderByAsc("sort_order");
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<Category> listChildCategories(Integer parentId) {
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId)
               .orderByAsc("sort_order");
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        // 1. 查询所有一级分类
        List<Category> parentCategories = listParentCategories();
        
        // 2. 查询所有二级分类
        List<Category> allChildCategories = categoryMapper.selectList(
            new QueryWrapper<Category>().orderByAsc("sort_order")
        );
        
        // 3. 按 parentId 分组
        Map<Integer, List<Category>> childMap = allChildCategories.stream()
            .filter(c -> c.getParentId() != null && c.getParentId() > 0)
            .collect(Collectors.groupingBy(Category::getParentId));
        
        // 4. 构建树形结构
        List<Map<String, Object>> tree = new ArrayList<>();
        
        for (Category parent : parentCategories) {
            Map<String, Object> node = new HashMap<>();
            node.put("value", parent.getId());
            node.put("label", parent.getName());
            
            // 添加子分类
            List<Category> children = childMap.get(parent.getId());
            if (children != null && !children.isEmpty()) {
                List<Map<String, Object>> childNodes = children.stream()
                    .map(child -> {
                        Map<String, Object> childNode = new HashMap<>();
                        childNode.put("value", child.getId());
                        childNode.put("label", child.getName());
                        return childNode;
                    })
                    .collect(Collectors.toList());
                node.put("children", childNodes);
            }
            
            tree.add(node);
        }
        
        return tree;
    }
}
