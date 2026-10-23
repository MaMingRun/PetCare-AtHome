package com.mr.mapper;

import com.mr.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressMapper {

    Address getByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    Address getById(@Param("id") Long id);

    /** 我的地址列表：默认地址排最前 */
    List<Address> listByUser(@Param("userId") Long userId);

    void insert(Address address);

    int updateByUser(Address address);

    int deleteByUser(@Param("id") Long id, @Param("userId") Long userId);

    /** 设为默认：先把该用户所有地址 is_default 置 0，再置 1 */
    void clearDefaultByUser(@Param("userId") Long userId);
    int markDefaultByUser(@Param("id") Long id, @Param("userId") Long userId);
}