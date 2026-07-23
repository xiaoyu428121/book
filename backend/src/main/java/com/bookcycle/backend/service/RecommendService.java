package com.bookcycle.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bookcycle.backend.entity.Book;
import java.util.List;

public interface RecommendService extends IService<Book> {
    
    // ========== 原有的方法保留 ==========
    List<Book> getHotBooks(int limit);
    List<Book> getBooksByCategory(Integer categoryId, int limit);
    List<Book> getBooksByCategoryId(Integer categoryId, int limit);
    
    // ========== 新增：协同过滤智能推荐 ==========
    /**
     * 【基于物品的协同过滤】
     * 买了这本书的人还买了…
     * 用于：书籍详情页
     */
    List<Book> recommendByItemCF(Integer bookId, int limit);
    
    /**
     * 【基于用户的协同过滤】
     * 和你品味相似的用户也喜欢…
     * 用于：首页"猜你喜欢"
     */
    List<Book> recommendByUserCF(Integer userId, int limit);
}