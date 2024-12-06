package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags="套餐相关接口")
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

    @PostMapping
    @ApiOperation("新增套餐")
    public Result add(@RequestBody SetmealDTO setMealDTO){
        log.info("新增套餐:{}",setMealDTO);
        setMealService.addWithDish(setMealDTO);
        return Result.success();
    }

    /**
     * 套餐分类查询
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("page")
    @ApiOperation("套餐分类查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分类查询:{}",setmealPageQueryDTO);
        PageResult pageResult=setMealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
}
