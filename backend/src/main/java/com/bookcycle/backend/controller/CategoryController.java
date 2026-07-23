package com.bookcycle.backend.controller;

import com.bookcycle.backend.common.Result;
import com.bookcycle.backend.entity.Category;
import com.bookcycle.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有分类（平铺结构）
     */
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }

    /**
     * 查询一级分类（父分类）
     */
    @GetMapping("/parents")
    public Result<List<Category>> listParents() {
        return Result.success(categoryService.listParentCategories());
    }

    /**
     * 根据父分类ID查询二级分类
     */
    @GetMapping("/children")
    public Result<List<Category>> listChildren(@RequestParam(required = false) Integer parentId) {
        if (parentId == null) {
            return Result.success(categoryService.listParentCategories());
        }
        return Result.success(categoryService.listChildCategories(parentId));
    }

    /**
     * 获取级联分类树（用于前端级联选择器）
     * 返回格式符合 Element Plus Cascader 组件
     */
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getTree() {
        return Result.success(categoryService.getCategoryTree());
    }
}
