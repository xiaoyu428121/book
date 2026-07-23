package com.bookcycle.backend.controller;

import lombok.Data;

@Data
public class OrderResponse {
    private Integer id;
    private String orderNo;
    private Integer bookId;
    private String bookTitle;
    private Double price;
    private Integer status;
    private String statusText;
    private Integer buyerId;
    private Integer sellerId;
    private String addressName;
    private String addressPhone;
    private String addressDetail;
    private String createTime;
    private String updateTime;

    private static final String[] STATUS_TEXT = {
        "待付款",
        "已付款",
        "已发货",
        "已完成",
        "已取消"
    };

    public void setStatusFromInt(Integer status) {
        this.status = status;
        if (status != null && status >= 0 && status < STATUS_TEXT.length) {
            this.statusText = STATUS_TEXT[status];
        } else {
            this.statusText = "未知";
        }
    }

    public static String getStatusText(Integer status) {
        if (status != null && status >= 0 && status < STATUS_TEXT.length) {
            return STATUS_TEXT[status];
        }
        return "未知";
    }
}
