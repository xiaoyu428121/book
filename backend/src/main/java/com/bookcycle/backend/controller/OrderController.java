package com.bookcycle.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bookcycle.backend.common.Result;
import com.bookcycle.backend.entity.Address;
import com.bookcycle.backend.entity.Book;
import com.bookcycle.backend.entity.Cart;
import com.bookcycle.backend.entity.Order;
import com.bookcycle.backend.mapper.AddressMapper;
import com.bookcycle.backend.mapper.BookMapper;
import com.bookcycle.backend.mapper.CartMapper;
import com.bookcycle.backend.service.OrderService;
import com.bookcycle.backend.util.UserContext;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Resource
    private OrderService orderService;
    @Resource
    private CartMapper cartMapper;
    @Resource
    private BookMapper bookMapper;
    @Resource
    private AddressMapper addressMapper;

    /**
     * 单个创建订单
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<?> createOrder(@RequestBody CreateOrderRequest request) {
        // 1. 获取当前登录用户
        Integer buyerId = UserContext.getCurrentUserId();
        if (buyerId == null) {
            return Result.error(401, "未登录，请先登录后再下单");
        }

        // 2. 参数校验
        if (request.getBookId() == null) {
            return Result.error(400, "教材ID不能为空");
        }
        if (request.getAddressId() == null) {
            return Result.error(400, "请选择收货地址");
        }

        // 3. 校验收货地址
        Address address = validateAddress(buyerId, request.getAddressId());
        if (address == null) {
            return Result.error(400, "收货地址无效或不属于当前用户");
        }

        // 4. 校验书籍状态
        Book book = bookMapper.selectById(request.getBookId());
        String bookCheckMsg = validateBookStatus(book);
        if (bookCheckMsg != null) {
            return Result.error(400, bookCheckMsg);
        }

        // 5. 创建订单
        Order order = buildOrder(buyerId, address, book);
        orderService.save(order);
        Order savedOrder = orderService.getById(order.getId());

        // 6. 移除购物车
        cartMapper.delete(new QueryWrapper<Cart>()
                .eq("user_id", buyerId)
                .eq("book_id", request.getBookId()));

        return Result.success("下单成功", savedOrder);
    }

    /**
     * 批量创建订单（从购物车）
     */
    @PostMapping("/batch")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> createBatchOrder(@RequestBody CreateBatchOrderRequest request) {
        // 1. 获取当前登录用户
        Integer buyerId = UserContext.getCurrentUserId();
        if (buyerId == null) {
            return Result.error(401, "未登录，请先登录后再下单");
        }

        // 2. 参数校验
        if (request.getAddressId() == null) {
            return Result.error(400, "请选择收货地址");
        }
        if (request.getCartIds() == null || request.getCartIds().isEmpty()) {
            return Result.error(400, "请选择要购买的教材");
        }

        // 3. 校验收货地址
        Address address = validateAddress(buyerId, request.getAddressId());
        if (address == null) {
            return Result.error(400, "收货地址无效或不属于当前用户");
        }

        // 4. 查询购物车数据
        List<Cart> carts = cartMapper.selectList(new QueryWrapper<Cart>()
                .eq("user_id", buyerId)
                .in("id", request.getCartIds()));

        if (carts.isEmpty()) {
            return Result.error(400, "购物车为空或所选数据无效");
        }

        // 5. 批量处理订单
        List<Order> successOrders = new ArrayList<>();
        List<String> skippedBooks = new ArrayList<>();

        for (Cart cart : carts) {
            Book book = bookMapper.selectById(cart.getBookId());
            String bookCheckMsg = validateBookStatus(book);
            if (bookCheckMsg != null) {
                skippedBooks.add(bookCheckMsg);
                continue;
            }

            // 检查书籍是否被其他订单锁定（防并发）
            if (!checkBookLock(book.getId())) {
                skippedBooks.add("《" + book.getTitle() + "》已被其他用户下单，无法购买");
                continue;
            }

            // 创建订单
            Order order = buildOrder(buyerId, address, book);
            orderService.save(order);
            successOrders.add(orderService.getById(order.getId()));
        }

        // 6. 移除已处理的购物车
        if (!successOrders.isEmpty()) {
            List<Integer> bookIds = successOrders.stream()
                    .map(Order::getBookId)
                    .collect(Collectors.toList());
            cartMapper.delete(new QueryWrapper<Cart>()
                    .eq("user_id", buyerId)
                    .in("book_id", bookIds));
        }

        // 7. 构建返回结果
        BatchOrderResult result = new BatchOrderResult();
        result.setSuccessCount(successOrders.size());
        result.setSkippedBooks(skippedBooks);
        result.setSuccessOrders(successOrders);

        String message = successOrders.size() > 0
                ? "成功下单 " + successOrders.size() + " 本教材"
                : "全部下单失败";
        if (!skippedBooks.isEmpty()) {
            message += "，" + skippedBooks.size() + " 本教材因以下原因被跳过：\n" + String.join("\n", skippedBooks);
        }

        return Result.success(message, result);
    }

    /**
     * 订单支付
     */
    @PutMapping("/{id}/pay")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> pay(@PathVariable Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录，请先登录后操作");
        }

        // 1. 查询订单并校验
        Order order = orderService.getById(id);
        String orderCheckMsg = validateOrder(userId, order, 0);
        if (orderCheckMsg != null) {
            return Result.error(400, orderCheckMsg);
        }

        // 2. 再次校验书籍状态（防并发）
        Book book = bookMapper.selectById(order.getBookId());
        String bookCheckMsg = validateBookStatus(book);
        if (bookCheckMsg != null) {
            return Result.error(400, bookCheckMsg);
        }

        // 3. 更新订单状态 + 标记书籍为已售出
        order.setStatus(1);
        orderService.updateById(order);

        book.setStatus(2);
        book.setTradeCount(book.getTradeCount() == null ? 1 : book.getTradeCount() + 1);
        bookMapper.updateById(book);

        return Result.success("支付成功", orderService.getById(id));
    }

    /**
     * 卖家发货
     */
    @PutMapping("/{id}/ship")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> ship(@PathVariable Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录，请先登录后操作");
        }

        // 1. 查询订单并校验（卖家权限 + 订单状态）
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error(400, "订单不存在");
        }
        if (!order.getSellerId().equals(userId)) {
            return Result.error(403, "无权操作此订单");
        }
        if (order.getStatus() != 1) {
            return Result.error(400, "仅能对已付款的订单进行发货操作");
        }

        // 2. 更新订单状态
        order.setStatus(2);
        orderService.updateById(order);

        return Result.success("发货成功", orderService.getById(id));
    }

    /**
     * 买家确认收货
     */
    @PutMapping("/{id}/complete")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> complete(@PathVariable Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录，请先登录后操作");
        }

        // 1. 查询订单并校验（买家权限 + 订单状态）
        Order order = orderService.getById(id);
        String orderCheckMsg = validateOrder(userId, order, 2);
        if (orderCheckMsg != null) {
            return Result.error(400, orderCheckMsg);
        }

        // 2. 更新订单状态
        order.setStatus(3);
        orderService.updateById(order);

        return Result.success("确认收货成功", orderService.getById(id));
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> cancel(@PathVariable Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录，请先登录后操作");
        }

        // 1. 查询订单并校验（买家权限 + 订单状态）
        Order order = orderService.getById(id);
        String orderCheckMsg = validateOrder(userId, order, 0);
        if (orderCheckMsg != null) {
            return Result.error(400, orderCheckMsg);
        }

        // 2. 更新订单状态为已取消
        order.setStatus(4);
        orderService.updateById(order);

        // 3. 恢复书籍状态（仅当书籍被当前订单标记为已售出时）
        Book book = bookMapper.selectById(order.getBookId());
        if (book != null && book.getStatus() == 2) {
            book.setStatus(1);
            bookMapper.updateById(book);
        }

        return Result.success("取消订单成功", orderService.getById(id));
    }

    // ------------------------- 私有工具方法 -------------------------

    /**
     * 校验收货地址有效性
     */
    private Address validateAddress(Integer userId, Integer addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            return null;
        }
        return address;
    }

    /**
     * 校验书籍状态
     */
    private String validateBookStatus(Book book) {
        if (book == null) {
            return "教材不存在";
        }
        if (book.getStatus() == null || book.getStatus() != 1) {
            return "《" + book.getTitle() + "》未上架、已售出或已下架";
        }
        return null;
    }

    /**
     * 校验订单权限和状态
     */
    private String validateOrder(Integer userId, Order order, Integer expectedStatus) {
        if (order == null) {
            return "订单不存在";
        }
        if (!order.getBuyerId().equals(userId)) {
            return "无权操作此订单";
        }
        if (!order.getStatus().equals(expectedStatus)) {
            String statusDesc = switch (expectedStatus) {
                case 0 -> "待付款";
                case 2 -> "已发货";
                default -> "指定状态";
            };
            return "订单状态不是" + statusDesc;
        }
        return null;
    }

    /**
     * 构建订单对象
     */
    private Order buildOrder(Integer buyerId, Address address, Book book) {
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setBookId(book.getId());
        order.setOrderNo(orderService.generateOrderNo());
        order.setStatus(0); // 待付款
        order.setAddressName(address.getName());
        order.setAddressPhone(address.getPhone());
        order.setAddressDetail(address.getDetail());
        order.setSellerId(book.getSellerId());
        order.setBookTitle(book.getTitle());
        order.setPrice(book.getPrice() != null ? book.getPrice().doubleValue() : 0.0);
        return order;
    }

    /**
     * 检查书籍是否被锁定（防并发下单）
     * 实际场景可结合Redis分布式锁/数据库乐观锁实现
     */
    private boolean checkBookLock(Integer bookId) {
        // 简化实现：查询书籍最新状态是否为1（在售）
        Book latestBook = bookMapper.selectById(bookId);
        return latestBook != null && latestBook.getStatus() == 1;
    }

    // ------------------------- 请求/响应实体类 -------------------------

    /**
     * 单个下单请求
     */
    public static class CreateOrderRequest {
        private Integer bookId;
        private Integer addressId;

        public Integer getBookId() {
            return bookId;
        }

        public void setBookId(Integer bookId) {
            this.bookId = bookId;
        }

        public Integer getAddressId() {
            return addressId;
        }

        public void setAddressId(Integer addressId) {
            this.addressId = addressId;
        }
    }

    /**
     * 批量下单请求
     */
    public static class CreateBatchOrderRequest {
        private Integer addressId;
        private List<Integer> cartIds;

        public Integer getAddressId() {
            return addressId;
        }

        public void setAddressId(Integer addressId) {
            this.addressId = addressId;
        }

        public List<Integer> getCartIds() {
            return cartIds;
        }

        public void setCartIds(List<Integer> cartIds) {
            this.cartIds = cartIds;
        }
    }

    /**
     * 批量下单结果
     */
    public static class BatchOrderResult {
        private int successCount;
        private List<String> skippedBooks;
        private List<Order> successOrders;

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public List<String> getSkippedBooks() {
            return skippedBooks;
        }

        public void setSkippedBooks(List<String> skippedBooks) {
            this.skippedBooks = skippedBooks;
        }

        public List<Order> getSuccessOrders() {
            return successOrders;
        }

        public void setSuccessOrders(List<Order> successOrders) {
            this.successOrders = successOrders;
        }
    }

    /**
     * 订单响应DTO（原toOrderResponse方法可保留，此处省略，如需可补充）
     */
    public static class OrderResponse {
        private Integer id;
        private String orderNo;
        private Integer bookId;
        private String bookTitle;
        private Double price;
        private Integer buyerId;
        private Integer sellerId;
        private String addressName;
        private String addressPhone;
        private String addressDetail;
        private String createTime;
        private String status; // 状态描述，如"待付款"、"已付款"等

        // 状态转换方法
        public void setStatusFromInt(Integer status) {
            this.status = switch (status) {
                case 0 -> "待付款";
                case 1 -> "已付款";
                case 2 -> "已发货";
                case 3 -> "已完成";
                case 4 -> "已取消";
                default -> "未知状态";
            };
        }

        // Getter & Setter
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public Integer getBookId() {
            return bookId;
        }

        public void setBookId(Integer bookId) {
            this.bookId = bookId;
        }

        public String getBookTitle() {
            return bookTitle;
        }

        public void setBookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Integer getBuyerId() {
            return buyerId;
        }

        public void setBuyerId(Integer buyerId) {
            this.buyerId = buyerId;
        }

        public Integer getSellerId() {
            return sellerId;
        }

        public void setSellerId(Integer sellerId) {
            this.sellerId = sellerId;
        }

        public String getAddressName() {
            return addressName;
        }

        public void setAddressName(String addressName) {
            this.addressName = addressName;
        }

        public String getAddressPhone() {
            return addressPhone;
        }

        public void setAddressPhone(String addressPhone) {
            this.addressPhone = addressPhone;
        }

        public String getAddressDetail() {
            return addressDetail;
        }

        public void setAddressDetail(String addressDetail) {
            this.addressDetail = addressDetail;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    // 保留原有的toOrderResponse方法
    private OrderResponse toOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setBookId(order.getBookId());
        response.setBookTitle(order.getBookTitle());
        response.setPrice(order.getPrice());
        response.setBuyerId(order.getBuyerId());
        response.setSellerId(order.getSellerId());
        response.setAddressName(order.getAddressName());
        response.setAddressPhone(order.getAddressPhone());
        response.setAddressDetail(order.getAddressDetail());
        if (order.getCreateTime() != null) {
            response.setCreateTime(order.getCreateTime().toString());
        }
        response.setStatusFromInt(order.getStatus());
        return response;
    }

        /**
     * 查询我买到的订单
     */
    @GetMapping("/buy")
    public Result<?> getMyBuyOrders() {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        List<Order> orders = orderService.getBuyOrders(userId);
        List<OrderResponse> responses = orders.stream().map(this::toOrderResponse).toList();
        return Result.success(responses);
    }

    /**
     * 查询我卖出的订单
     */
    @GetMapping("/sell")
    public Result<?> getMySellOrders() {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        List<Order> orders = orderService.getSellOrders(userId);
        List<OrderResponse> responses = orders.stream().map(this::toOrderResponse).toList();
        return Result.success(responses);
    }
}