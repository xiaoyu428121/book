package com.bookcycle.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookcycle.backend.entity.Book;
import com.bookcycle.backend.entity.Order;
import com.bookcycle.backend.entity.User;
import com.bookcycle.backend.mapper.BookMapper;
import com.bookcycle.backend.mapper.OrderMapper;
import com.bookcycle.backend.mapper.UserMapper;
import com.bookcycle.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl extends ServiceImpl<UserMapper, User> implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public IPage<User> getUserList(Integer page, Integer size) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> result = userMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(user -> user.setPassword(null));
        return result;
    }

    @Override
    public void updateUserStatus(Integer userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public IPage<Book> getBookReviewList(Integer page, Integer size) {
        Page<Book> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 0)
                .orderByDesc(Book::getCreateTime);
        return bookMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public void approveBook(Integer bookId) {
        Book book = new Book();
        book.setId(bookId);
        book.setStatus(1);
        bookMapper.updateById(book);
    }

    @Override
    public void rejectBook(Integer bookId) {
        Book book = new Book();
        book.setId(bookId);
        book.setStatus(4);
        bookMapper.updateById(book);
    }

    @Override
    public IPage<Order> getOrderList(Integer page, Integer size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        Long userCount = userMapper.selectCount(null);
        stats.put("totalUsers", userCount);

        LambdaQueryWrapper<Book> bookWrapper = new LambdaQueryWrapper<>();
        bookWrapper.eq(Book::getStatus, 1);
        Long bookCount = bookMapper.selectCount(bookWrapper);
        stats.put("totalBooks", bookCount);

        Long orderCount = orderMapper.selectCount(null);
        stats.put("totalOrders", orderCount);

        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getStatus, 3);
        List<Order> completedOrders = orderMapper.selectList(orderWrapper);
        double totalAmount = completedOrders.stream()
                .map(order -> order.getPrice() != null ? order.getPrice().doubleValue() : 0.0)
                .mapToDouble(Double::doubleValue)
                .sum();
        stats.put("totalAmount", String.format("%.2f", totalAmount));

        return stats;
    }
}
