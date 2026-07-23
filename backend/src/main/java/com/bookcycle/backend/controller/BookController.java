package com.bookcycle.backend.controller;
import com.bookcycle.backend.common.Result;
import com.bookcycle.backend.service.BookService;
import com.bookcycle.backend.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    private BookService bookService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "1000") Integer size,
            String keyword,
            Integer categoryId) {

        // ✅ 关键修改：查询 status=1（在售）和 status=2（已售出）
        // ✅ 安全加固：全部使用 ? 占位符参数化，杜绝 SQL 注入
        StringBuilder sql = new StringBuilder("SELECT * FROM book WHERE status IN (1, 2)");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (title LIKE ? OR isbn LIKE ? OR author LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }

        // ✅ 排序：在售在前，已售出在后；同状态按时间倒序
        sql.append(" ORDER BY status ASC, create_time DESC");

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        // BLOB转String
        for (Map<String, Object> row : list) {
            convertBlobToString(row, "images");
            convertBlobToString(row, "description");
        }

        return Result.success(Map.of(
                "records", list,
                "total", list.size()
        ));
    }
    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> getPendingBooks() {
        String sql = "SELECT * FROM book WHERE status = 0 ORDER BY create_time DESC";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : list) {
            convertBlobToString(row, "images");
            convertBlobToString(row, "description");
        }
        return Result.success(list);
    }
    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Integer id) {
        String sql = "SELECT * FROM book WHERE id = ?";
        Map<String, Object> book = jdbcTemplate.queryForMap(sql, id);
        convertBlobToString(book, "images");
        convertBlobToString(book, "description");
        jdbcTemplate.update("UPDATE book SET view_count = view_count + 1 WHERE id = ?", id);
        return Result.success(book);
    }
    @PostMapping
    public Result<String> add(@RequestBody Map<String, Object> book) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        Object originalPrice = book.get("originalPrice");
        if (originalPrice == null || "".equals(originalPrice)) {
            originalPrice = null;
        }
        
        // ✅ 强制转数字，避免类型问题
        Object categoryId = book.get("categoryId");
        if (categoryId != null && !"".equals(categoryId)) {
            categoryId = Integer.valueOf(String.valueOf(categoryId));
        } else {
            categoryId = null;
        }
        
        jdbcTemplate.update(
            "INSERT INTO book (title, isbn, author, category_id, original_price, price, condition_level, images, description, seller_id, status, trade_count, stock, view_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            book.get("title"),
            book.get("isbn"),
            book.get("author"),
            categoryId,
            originalPrice,
            book.get("price"),
            book.get("conditionLevel"),
            book.get("images"),
            book.get("description"),
            userId,
            0,
            0,
            1,
            0
        );
        
        return Result.success("发布成功，等待管理员审核");
    }
    @PutMapping("/{id}/audit")
    public Result<String> audit(@PathVariable Integer id) {
        jdbcTemplate.update("UPDATE book SET status = 1 WHERE id = ?", id);
        return Result.success("审核通过，书籍已上架");
    }
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody Map<String, Object> book) {
        
        // ✅ 空值处理：空字符串转null
        Object originalPrice = book.get("originalPrice");
        if (originalPrice == null || "".equals(originalPrice)) {
            originalPrice = null;
        }
        
        Object categoryId = book.get("categoryId");
        if (categoryId == null || "".equals(categoryId)) {
            categoryId = null;
        }
        
        jdbcTemplate.update(
            "UPDATE book SET title = ?, isbn = ?, author = ?, price = ?, original_price = ?, category_id = ?, condition_level = ?, images = ?, description = ?, status = ? WHERE id = ?",
            book.get("title"),
            book.get("isbn"),
            book.get("author"),
            book.get("price"),
            originalPrice,      // ✅ 处理后的值
            categoryId,         // ✅ 处理后的值
            book.get("conditionLevel"),
            book.get("images"),
            book.get("description"),
            book.get("status"),
            id
        );
        
        return Result.success("修改成功");
    }
    @PutMapping("/{id}/off-shelf")
    public Result<String> offShelf(@PathVariable Integer id) {
        jdbcTemplate.update("UPDATE book SET status = 2 WHERE id = ?", id);
        return Result.success("下架成功");
    }

    // ✅ 新增：删除书籍接口（完全匹配前端DELETE请求）
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        jdbcTemplate.update("DELETE FROM book WHERE id = ?", id);
        return Result.success("删除成功");
    }

    private void convertBlobToString(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof byte[]) {
            row.put(key, new String((byte[]) value, StandardCharsets.UTF_8));
        }
    }
}