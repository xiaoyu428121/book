package com.bookcycle.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bookcycle.backend.entity.Book;
import com.bookcycle.backend.entity.Order;
import com.bookcycle.backend.entity.User;
import java.util.Map;

public interface AdminService {
    IPage<User> getUserList(Integer page, Integer size);
    void updateUserStatus(Integer userId, Integer status);
    IPage<Book> getBookReviewList(Integer page, Integer size);
    void approveBook(Integer bookId);
    void rejectBook(Integer bookId);
    IPage<Order> getOrderList(Integer page, Integer size);
    Map<String, Object> getStatistics();
}
