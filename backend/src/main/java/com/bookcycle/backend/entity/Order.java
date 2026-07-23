package com.bookcycle.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String orderNo;
    private Integer buyerId;
    private Integer sellerId;
    private Integer bookId;
    private String bookTitle;
    private Double price;
    private Integer status;
    private String addressName;
    private String addressPhone;
    private String addressDetail;
    private LocalDateTime createTime;
}
