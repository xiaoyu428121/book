package com.bookcycle.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bookcycle.backend.entity.Order;
import java.util.List;

public interface OrderService extends IService<Order> {
    String generateOrderNo();
    List<Order> getBuyOrders(Integer userId);
    List<Order> getSellOrders(Integer userId);
}
