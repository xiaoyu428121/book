package com.bookcycle.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bookcycle.backend.entity.Cart;
import java.util.List;

public interface CartService extends IService<Cart> {
    List<Cart> getMyCart(Integer userId);
    void addOrIncrement(Integer userId, Integer bookId, Integer quantity);
}