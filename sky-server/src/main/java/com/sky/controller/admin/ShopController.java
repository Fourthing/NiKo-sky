package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags="店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺营业状态
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺营业状态:{}",status==1? MessageConstant.SHOP_OPENING :MessageConstant.SHOP_CLOSED);
        redisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS,String.valueOf(status));
        return Result.success();
    }

    /**
     * 获取店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus(){
        String statusString = (String) redisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS);
        Integer status = statusString != null ? Integer.valueOf(statusString) : null; // 处理可能的空值
        log.info("获取店铺营业状态:{}",status==1? MessageConstant.SHOP_OPENING :MessageConstant.SHOP_CLOSED);
        return Result.success(status);
    }
}
