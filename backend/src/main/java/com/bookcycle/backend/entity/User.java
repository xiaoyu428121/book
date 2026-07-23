package com.bookcycle.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String avatar;

    private String role;

    private BigDecimal balance;

    private Integer status;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String token;
    
    // 新增：交易统计字段（不映射数据库，和前端完全对应）
    @TableField(exist = false)
    private Integer sellCount;    // 卖出教材数量
    
    @TableField(exist = false)
    private BigDecimal sellIncome;    // 总收益（和balance同类型BigDecimal）
    
    @TableField(exist = false)
    private Integer buyCount;     // 买入教材数量
    
    @TableField(exist = false)
    private BigDecimal buyExpense;    // 总花费（和balance同类型BigDecimal）
}