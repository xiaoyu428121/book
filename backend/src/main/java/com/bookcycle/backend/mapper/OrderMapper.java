package com.bookcycle.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bookcycle.backend.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM order_info WHERE buyer_id = #{userId} ORDER BY create_time DESC")
    List<Order> listByBuyerId(@Param("userId") Integer userId);

    @Select("SELECT * FROM order_info WHERE seller_id = #{userId} ORDER BY create_time DESC")
    List<Order> listBySellerId(@Param("userId") Integer userId);

    // 新增：自定义求和方法，解决MyBatis-Plus版本差异问题
    @Select("SELECT SUM(price) FROM `order_info` ${ew.customSqlSegment}") // 修复：表名统一为order_info
    BigDecimal selectSumPrice(@Param("ew") QueryWrapper<Order> queryWrapper);
}