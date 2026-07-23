package com.bookcycle.backend.controller;

import com.bookcycle.backend.common.Result;
import com.bookcycle.backend.entity.Cart;
import com.bookcycle.backend.entity.Book;
import com.bookcycle.backend.service.CartService;
import com.bookcycle.backend.service.BookService;
import com.bookcycle.backend.util.UserContext;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Resource
    private CartService cartService;
    
    @Resource
    private BookService bookService;

    // ✅ 修复1：路径改成和前端一致 /api/cart/list
    @GetMapping("/list")
    public Result list() {
        try {
            Integer userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            List<Cart> list = cartService.getMyCart(userId);
            
            // ✅ 修复2：处理book为null的情况，避免Map.of抛空指针
            List<Map<String, Object>> result = list.stream().map(cart -> {
                Book book = bookService.getById(cart.getBookId());
                Map<String, Object> map = new HashMap<>();
                map.put("id", cart.getId());
                map.put("bookId", cart.getBookId());
                map.put("userId", cart.getUserId());
                map.put("quantity", cart.getQuantity());
                map.put("createTime", cart.getCreateTime());
                map.put("book", book); // book为null也不会报错
                return map;
            }).collect(Collectors.toList());
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "加载暂存架失败：" + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result add(@RequestBody Cart cart) {
        try {
            Integer userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            cartService.addOrIncrement(userId, cart.getBookId(), cart.getQuantity() == null ? 1 : cart.getQuantity());
            return Result.success("添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "添加失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            Integer userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            Cart cart = cartService.getById(id);
            if (cart == null) {
                return Result.error(400, "暂存架项不存在");
            }
            if (!cart.getUserId().equals(userId)) {
                return Result.error(403, "无权操作");
            }
            cartService.removeById(id);
            return Result.success("移除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }
}