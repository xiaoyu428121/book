package com.bookcycle.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookcycle.backend.entity.Order;
import com.bookcycle.backend.mapper.OrderMapper;
import com.bookcycle.backend.service.OrderService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Override
    public String generateOrderNo() {
        return "ORDER" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    @Override
    public List<Order> getBuyOrders(Integer userId) {
        return orderMapper.listByBuyerId(userId);
    }

    @Override
    public List<Order> getSellOrders(Integer userId) {
        return orderMapper.listBySellerId(userId);
    }
}
