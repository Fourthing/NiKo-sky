package com.sky.controller.user;

import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags="店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

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
