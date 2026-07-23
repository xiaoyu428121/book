package com.bookcycle.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookcycle.backend.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    @Select("SELECT c.*, b.title book_title, b.images, b.price, b.condition_level, b.stock " +
            "FROM cart c LEFT JOIN book b ON c.book_id = b.id " +
            "WHERE c.user_id = #{userId}")
    List<Cart> listByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND book_id = #{bookId}")
    Cart findByUserIdAndBookId(@Param("userId") Integer userId, @Param("bookId") Integer bookId);

    @Update("UPDATE cart SET quantity = quantity + #{quantity} WHERE id = #{id}")
    void incrementQuantity(@Param("id") Integer id, @Param("quantity") Integer quantity);
}