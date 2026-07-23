package com.bookcycle.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookcycle.backend.entity.Cart;
import com.bookcycle.backend.mapper.CartMapper;
import com.bookcycle.backend.service.CartService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Resource
    private CartMapper cartMapper;

    @Override
    public List<Cart> getMyCart(Integer userId) {
        return cartMapper.listByUserId(userId);
    }

    @Override
    public void addOrIncrement(Integer userId, Integer bookId, Integer quantity) {
        Cart existing = cartMapper.findByUserIdAndBookId(userId, bookId);
        if (existing != null) {
            cartMapper.incrementQuantity(existing.getId(), quantity);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setBookId(bookId);
            cart.setQuantity(quantity != null ? quantity : 1);
            cart.setCreateTime(LocalDateTime.now());
            cartMapper.insert(cart);
        }
    }
}