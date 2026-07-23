package com.bookcycle.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("address")
public class Address {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String name;

    private String phone;

    private String detail;

    private Integer isDefault;

    private LocalDateTime createTime;
}
