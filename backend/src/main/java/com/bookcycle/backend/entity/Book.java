package com.bookcycle.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private String isbn;

    private String author;

    private Integer categoryId;

    private BigDecimal originalPrice;

    private BigDecimal price;

    private String conditionLevel;

    private String images;

    private String description;

    private Integer sellerId;

    private Integer status;

    private Integer tradeCount;

    private Integer stock;

    private Integer viewCount;

    private LocalDateTime createTime;
}
