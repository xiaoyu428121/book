package com.bookcycle.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookcycle.backend.entity.Book;
import com.bookcycle.backend.entity.Category;
import com.bookcycle.backend.entity.Order;
import com.bookcycle.backend.mapper.BookMapper;
import com.bookcycle.backend.mapper.CategoryMapper;
import com.bookcycle.backend.mapper.OrderMapper;
import com.bookcycle.backend.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl extends ServiceImpl<BookMapper, Book> implements RecommendService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private BookMapper bookMapper;

    // ============================================
    // 原有方法
    // ============================================

    @Override
    public List<Book> getHotBooks(int limit) {
        QueryWrapper<Book> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
               .orderByDesc("trade_count", "view_count")
               .last("LIMIT " + limit);
        return list(wrapper);
    }

    @Override
    public List<Book> getBooksByCategory(Integer categoryId, int limit) {
        QueryWrapper<Book> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
               .eq("category_id", categoryId)
               .orderByDesc("trade_count", "view_count")
               .last("LIMIT " + limit);
        return list(wrapper);
    }

    @Override
    public List<Book> getBooksByCategoryId(Integer categoryId, int limit) {
        return getBooksByCategory(categoryId, limit);
    }

    // ============================================
    // ✅ 修改：基于物品的协同过滤 + 无数据时fallback同分类
    // ============================================
    @Override
    public List<Book> recommendByItemCF(Integer bookId, int limit) {
        // 1. 先尝试协同过滤（有订单数据时生效）
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBookId, bookId);
        List<Order> orders = orderMapper.selectList(wrapper);
        
        // 2. ✅ 没有订单数据时，直接返回同分类的真实教材（最关键的修改！）
        if (orders.isEmpty()) {
            Book currentBook = this.getById(bookId);
            if (currentBook != null && currentBook.getCategoryId() != null) {
                return getBooksByCategory(currentBook.getCategoryId(), limit);
            }
            return getHotBooks(limit);
        }

        // 3. 有订单数据时，走正常的协同过滤逻辑
        Set<Integer> userIds = orders.stream()
                .map(Order::getBuyerId)
                .collect(Collectors.toSet());

        Map<Integer, Integer> bookCount = new HashMap<>();
        for (Integer userId : userIds) {
            LambdaQueryWrapper<Order> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(Order::getBuyerId, userId);
            List<Order> userOrders = orderMapper.selectList(userWrapper);
            
            for (Order order : userOrders) {
                if (!order.getBookId().equals(bookId)) {
                    bookCount.merge(order.getBookId(), 1, Integer::sum);
                }
            }
        }

        List<Integer> similarBookIds = bookCount.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (similarBookIds.isEmpty()) {
            Book currentBook = this.getById(bookId);
            if (currentBook != null && currentBook.getCategoryId() != null) {
                return getBooksByCategory(currentBook.getCategoryId(), limit);
            }
            return getHotBooks(limit);
        }

        List<Book> books = bookMapper.selectBatchIds(similarBookIds);
        return books.stream()
                .filter(book -> book.getStatus() == 1)
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ============================================
    // 基于用户的协同过滤
    // ============================================
    @Override
    public List<Book> recommendByUserCF(Integer userId, int limit) {
        LambdaQueryWrapper<Order> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(Order::getBuyerId, userId);
        List<Order> userOrders = orderMapper.selectList(userWrapper);

        if (userOrders.isEmpty()) {
            return getHotBooks(limit);
        }

        Set<Integer> userBookIds = userOrders.stream()
                .map(Order::getBookId)
                .collect(Collectors.toSet());

        Map<Integer, Integer> userSimilarity = new HashMap<>();
        for (Integer bookId : userBookIds) {
            LambdaQueryWrapper<Order> bookWrapper = new LambdaQueryWrapper<>();
            bookWrapper.eq(Order::getBookId, bookId);
            List<Order> bookOrders = orderMapper.selectList(bookWrapper);
            
            for (Order order : bookOrders) {
                if (!order.getBuyerId().equals(userId)) {
                    userSimilarity.merge(order.getBuyerId(), 1, Integer::sum);
                }
            }
        }

        List<Integer> similarUserIds = userSimilarity.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (similarUserIds.isEmpty()) {
            return getHotBooks(limit);
        }

        Map<Integer, Integer> recommendBooks = new HashMap<>();
        for (Integer similarUserId : similarUserIds) {
            LambdaQueryWrapper<Order> simWrapper = new LambdaQueryWrapper<>();
            simWrapper.eq(Order::getBuyerId, similarUserId);
            List<Order> simOrders = orderMapper.selectList(simWrapper);
            
            for (Order order : simOrders) {
                if (!userBookIds.contains(order.getBookId())) {
                    recommendBooks.merge(order.getBookId(), 1, Integer::sum);
                }
            }
        }

        List<Integer> recommendBookIds = recommendBooks.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (recommendBookIds.isEmpty()) {
            return getHotBooks(limit);
        }

        List<Book> books = bookMapper.selectBatchIds(recommendBookIds);
        return books.stream()
                .filter(book -> book.getStatus() == 1)
                .limit(limit)
                .collect(Collectors.toList());
    }
}