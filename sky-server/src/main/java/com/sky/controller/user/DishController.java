package com.sky.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.UserService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper; // 使用Jackson的ObjectMapper

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        //查询Redis中是否存在缓存数据
        String key = "dish_"+categoryId;//首先构造Redis的key，命名规则为：dish_categoryID
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);

        //如果存在，无需查表，直接读取缓存
        if (list!=null && list.size()>0) {
            return Result.success(list);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        //如果不存在，查询数据库，并将查询到的数据放入Redis中
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key,list,15, TimeUnit.MINUTES);

        return Result.success(list);
    }

}