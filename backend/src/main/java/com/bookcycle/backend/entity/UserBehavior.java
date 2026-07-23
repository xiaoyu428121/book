package com.bookcycle.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_behavior")
public class UserBehavior {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer bookId;

    /**
     * 行为类型：1=浏览, 2=加购, 3=购买
     */
    private Integer behaviorType;

    private LocalDateTime behaviorTime;
}