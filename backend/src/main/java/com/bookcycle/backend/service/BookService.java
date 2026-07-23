package com.bookcycle.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bookcycle.backend.entity.Book;
import java.util.List;

public interface BookService {
    
    IPage<Book> getBookPage(Integer page, Integer size, String keyword, Integer categoryId, Integer status);
    
    // ✅ 新增：获取所有待审核书籍
    List<Book> getPendingBooks();
    
    Book getById(Integer id);
    
    void add(Book book);
    
    void update(Book book);
    
    void offShelf(Integer id);
}