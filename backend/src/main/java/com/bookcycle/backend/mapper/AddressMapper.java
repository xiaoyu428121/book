package com.bookcycle.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookcycle.backend.entity.Address;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}
