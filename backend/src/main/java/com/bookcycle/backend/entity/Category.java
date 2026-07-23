package com.bookcycle.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教材分类实体类
 * 支持一级和二级分类
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 父分类ID，NULL表示一级分类
     */
    private Integer parentId;
    
    /**
     * 排序字段，数字越小越靠前
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    // ========================
    // 辅助方法（前端展示用）
    // ========================
    
    /**
     * 判断是否是一级分类
     */
    public boolean isParent() {
        return this.parentId == null || this.parentId == 0;
    }
    
    /**
     * 判断是否是二级分类
     */
    public boolean isChild() {
        return !isParent();
    }
}
