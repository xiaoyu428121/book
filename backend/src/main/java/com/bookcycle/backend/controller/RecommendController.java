package com.bookcycle.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bookcycle.backend.common.Result;
import com.bookcycle.backend.entity.Book;
import com.bookcycle.backend.entity.Category;
import com.bookcycle.backend.service.CategoryService;
import com.bookcycle.backend.service.RecommendService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {
    @Resource
    private RecommendService recommendService;
    @Resource
    private CategoryService categoryService;

    // ============================================
    // 原有接口 - 完全保留！
    // ============================================
    /**
     * 获取热门教材推荐
     */
    @GetMapping("/hot")
    public Result<List<Book>> getHotBooks() {
        List<Book> books = recommendService.getHotBooks(10);
        return Result.success(books);
    }

    /**
     * 获取按一级分类推荐的教材
     */
    @GetMapping("/category")
    public Result<List<Map<String, Object>>> getBooksByCategory() {
        List<Category> parentCategories = categoryService.listParentCategories();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Category category : parentCategories) {
            Map<String, Object> map = new HashMap<>();
            map.put("categoryId", category.getId());
            map.put("categoryName", category.getName());
            map.put("books", recommendService.getBooksByCategoryId(category.getId(), 4));
            result.add(map);
        }
        return Result.success(result);
    }

    // ============================================
    // ✅ 完美适配你的项目：直接用RecommendService自带的方法
    // ============================================
    /**
     * 买了这本书的人还买了…
     * 用于：书籍详情页 - 返回真实book表里的同分类书籍
     */
    @GetMapping("/similar/{bookId}")
    public Result<List<Book>> getSimilarBooks(
            @PathVariable Integer bookId,
            @RequestParam(defaultValue = "6") Integer limit) {

        // 1. 用RecommendService自带的getById（继承自IService）
        Book currentBook = recommendService.getById(bookId);
        if (currentBook == null || currentBook.getCategoryId() == null) {
            return Result.success(Collections.emptyList());
        }

        // 2. 用RecommendService自带的list方法（继承自IService）
        LambdaQueryWrapper<Book> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Book::getCategoryId, currentBook.getCategoryId())
               .ne(Book::getId, bookId) // 排除当前书籍
               .eq(Book::getStatus, 1) // 只查上架的
               .orderByDesc(Book::getTradeCount) // 按销量排序
               .last("LIMIT " + limit);

        List<Book> similarBooks = recommendService.list(wrapper);
        return Result.success(similarBooks);
    }

    /**
     * 【基于物品的协同过滤】—— 接上此前已实现但未暴露的 recommendByItemCF
     * 有订单数据时返回"买过此书的人还买了…"，无数据时自动 fallback 到同分类热门。
     * 用于：书籍详情页"相关推荐"升级版。
     */
    @GetMapping("/item-cf/{bookId}")
    public Result<List<Book>> getItemCF(
            @PathVariable Integer bookId,
            @RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(recommendService.recommendByItemCF(bookId, limit));
    }

    /**
     * 【基于用户的协同过滤】
     * 猜你喜欢 - 个性化推荐
     * 用于：首页
     */
    @GetMapping("/for-user/{userId}")
    public Result<List<Book>> getRecommendForUser(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "8") Integer limit) {
        List<Book> books = recommendService.recommendByUserCF(userId, limit);
        return Result.success(books);
    }
}