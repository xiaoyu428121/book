package com.bookcycle.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookcycle.backend.dto.AddressDTO;
import com.bookcycle.backend.entity.Address;
import com.bookcycle.backend.mapper.AddressMapper;
import com.bookcycle.backend.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> getAddressList(Integer userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreateTime);
        return addressMapper.selectList(wrapper);
    }

    @Override
    public void addAddress(Integer userId, AddressDTO dto) {
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefaultAddress(userId);
        }

        Address address = new Address();
        address.setUserId(userId);
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setDetail(dto.getDetail());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        addressMapper.insert(address);
    }

    @Override
    public void updateAddress(Integer userId, AddressDTO dto) {
        Address existingAddress = addressMapper.selectById(dto.getId());
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权限修改");
        }

        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefaultAddress(userId);
        }

        Address address = new Address();
        address.setId(dto.getId());
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setDetail(dto.getDetail());
        address.setIsDefault(dto.getIsDefault());
        addressMapper.updateById(address);
    }

    @Override
    public void deleteAddress(Integer userId, Integer addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权限删除");
        }
        addressMapper.deleteById(addressId);
    }

    @Override
    public void setDefaultAddress(Integer userId, Integer addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权限操作");
        }

        clearDefaultAddress(userId);

        Address updateAddress = new Address();
        updateAddress.setId(addressId);
        updateAddress.setIsDefault(1);
        addressMapper.updateById(updateAddress);
    }

    private void clearDefaultAddress(Integer userId) {
        LambdaUpdateWrapper<Address> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0);
        addressMapper.update(null, updateWrapper);
    }
}
