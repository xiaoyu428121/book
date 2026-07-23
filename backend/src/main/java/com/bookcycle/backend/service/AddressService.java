package com.bookcycle.backend.service;

import com.bookcycle.backend.dto.AddressDTO;
import com.bookcycle.backend.entity.Address;
import java.util.List;

public interface AddressService {
    List<Address> getAddressList(Integer userId);
    void addAddress(Integer userId, AddressDTO dto);
    void updateAddress(Integer userId, AddressDTO dto);
    void deleteAddress(Integer userId, Integer addressId);
    void setDefaultAddress(Integer userId, Integer addressId);
}
