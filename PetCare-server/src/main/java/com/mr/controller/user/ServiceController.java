package com.mr.controller.user;

import com.mr.common.result.Result;
import com.mr.service.ShopService;
import com.mr.vo.ServiceCardVO;
import com.mr.vo.ServiceDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/services")
public class ServiceController {

    @Autowired
    private ShopService shopService;

    /** 列表：/user/services 或 /user/services?category=FEEDING */
    @GetMapping
    public Result<List<ServiceCardVO>> list(
            @RequestParam(required = false) String category) {
        return Result.success(shopService.listServices(category));
    }

    /** 详情：/user/services/1 */
    @GetMapping("/{id}")
    public Result<ServiceDetailVO> detail(@PathVariable Long id) {
        return Result.success(shopService.getServiceDetail(id));
    }
}
