package com.bookcycle.backend.service;

import com.bookcycle.backend.entity.Category;
import java.util.List;
import java.util.Map;

public interface CategoryService {
    
    /**
     * 查询所有分类
     */
    List<Category> listAll();
    
    /**
     * 查询一级分类（父分类）
     */
    List<Category> listParentCategories();
    
    /**
     * 根据父分类ID查询二级分类
     */
    List<Category> listChildCategories(Integer parentId);
    
    /**
     * 获取级联分类树（用于前端级联选择器）
     * 返回格式：[{"value":1,"label":"分类名","children":[...]}]
     */
    List<Map<String, Object>> getCategoryTree();
}
