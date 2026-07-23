package com.bookcycle.backend.dto;

import lombok.Data;

@Data
public class UserResponseDAO {
    private Integer id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private String role;
    
    // 新增：交易统计字段（和前端完全对应）
    private Integer sellCount;    // 卖出教材数量
    private Double sellIncome;    // 总收益
    private Integer buyCount;     // 买入教材数量
    private Double buyExpense;    // 总花费
}