package com.bookcycle.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bookcycle.backend.entity.Book;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BookMapper extends BaseMapper<Book> {

    IPage<Book> findBookPage(IPage<Book> page, 
                             @Param("keyword") String keyword, 
                             @Param("categoryId") Integer categoryId,
                             @Param("status") Integer status);

    void insertBook(Book book);

    // ✅ 新增：查询待审核书籍
    List<Book> findPendingBooks();
}