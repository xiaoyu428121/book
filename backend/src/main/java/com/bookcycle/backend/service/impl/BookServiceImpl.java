package com.bookcycle.backend.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookcycle.backend.entity.Book;
import com.bookcycle.backend.mapper.BookMapper;
import com.bookcycle.backend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Override
    public IPage<Book> getBookPage(Integer page, Integer size, String keyword, Integer categoryId, Integer status) {
        Page<Book> pageParam = new Page<>(page, size);
        return bookMapper.findBookPage(pageParam, keyword, categoryId, status);
    }

    @Override
    public List<Book> getPendingBooks() {
        // ✅ 改用自己写的查询，用ResultMap
        return bookMapper.findPendingBooks();
    }

    @Override
    public Book getById(Integer id) {
        Book book = bookMapper.selectById(id);
        book.setViewCount(book.getViewCount() + 1);
        bookMapper.updateById(book);
        System.out.println("查询详情 images = " + book.getImages());
        return book;
    }

    @Override
    public void add(Book book) {
        book.setStatus(0);
        book.setViewCount(0);
        book.setTradeCount(0);
        if (book.getStock() == null) {
            book.setStock(1);
        }
        
        System.out.println("保存前 images = " + book.getImages());
        bookMapper.insertBook(book);
        System.out.println("保存成功，书籍ID = " + book.getId());
    }

    @Override
    public void update(Book book) {
        bookMapper.updateById(book);
    }

    @Override
    public void offShelf(Integer id) {
        Book book = new Book();
        book.setId(id);
        book.setStatus(2);
        bookMapper.updateById(book);
    }
}