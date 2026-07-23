package com.bookcycle.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressDTO {

    private Integer id;

    @NotBlank(message = "收货人姓名不能为空")
    private String name;

    @NotBlank(message = "收货人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    private Integer isDefault;
}
