package com.mr.controller.user;

import com.mr.common.context.BaseContext;
import com.mr.common.exception.BaseException;
import com.mr.common.result.Result;
import com.mr.entity.Address;
import com.mr.entity.Pet;
import com.mr.mapper.AddressMapper;
import com.mr.mapper.PetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户资产：地址 + 宠物（归属校验全部在 SQL 内）
 */
@RestController
@RequestMapping("/user")
public class UserAssetController {

    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private PetMapper petMapper;

    // ======= 宠物 =======

    /** 我的宠物列表 */
    @GetMapping("/pets")
    public Result<List<Pet>> pets() {
        return Result.success(petMapper.listByUser(BaseContext.getCurrentId()));
    }

    /** 新增宠物 */
    @PostMapping("/pets")
    public Result<Long> addPet(@RequestBody Pet pet) {
        pet.setUserId(BaseContext.getCurrentId());
        petMapper.insert(pet);
        return Result.success(pet.getId());
    }

    /** 修改宠物（归属在 SQL 内校验） */
    @PutMapping("/pets/{id}")
    public Result<Void> updatePet(@PathVariable Long id, @RequestBody Pet pet) {
        pet.setId(id);
        pet.setUserId(BaseContext.getCurrentId());
        int rows = petMapper.updateByUser(pet);
        if (rows == 0) throw new BaseException("宠物不存在或无权限");
        return Result.success();
    }

    /** 删除宠物（软删） */
    @DeleteMapping("/pets/{id}")
    public Result<Void> deletePet(@PathVariable Long id) {
        int rows = petMapper.deleteByUser(id, BaseContext.getCurrentId());
        if (rows == 0) throw new BaseException("宠物不存在或无权限");
        return Result.success();
    }

    // ======= 地址 =======

    /** 我的地址列表 */
    @GetMapping("/addresses")
    public Result<List<Address>> addresses() {
        return Result.success(addressMapper.listByUser(BaseContext.getCurrentId()));
    }

    /** 新增地址 */
    @PostMapping("/addresses")
    public Result<Long> addAddress(@RequestBody Address address) {
        Long userId = BaseContext.getCurrentId();
        address.setUserId(userId);
        // 新增时如果标为默认，先清掉旧默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.clearDefaultByUser(userId);
        }
        addressMapper.insert(address);
        return Result.success(address.getId());
    }

    /** 修改地址 */
    @PutMapping("/addresses/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody Address address) {
        Long userId = BaseContext.getCurrentId();
        address.setId(id);
        address.setUserId(userId);
        int rows = addressMapper.updateByUser(address);
        if (rows == 0) throw new BaseException("地址不存在或无权限");
        return Result.success();
    }

    /** 删除地址 */
    @DeleteMapping("/addresses/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        int rows = addressMapper.deleteByUser(id, BaseContext.getCurrentId());
        if (rows == 0) throw new BaseException("地址不存在或无权限");
        return Result.success();
    }

    /** 设为默认地址 */
    @PostMapping("/addresses/{id}/default")
    public Result<Void> setDefaultAddress(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        addressMapper.clearDefaultByUser(userId);
        int rows = addressMapper.markDefaultByUser(id, userId);
        if (rows == 0) throw new BaseException("地址不存在或无权限");
        return Result.success();
    }
}
