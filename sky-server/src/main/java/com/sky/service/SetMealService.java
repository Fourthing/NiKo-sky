package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;

public interface SetMealService {

    /**
     * 新增套餐
     * @param setMealDTO
     */
    void addWithDish(SetmealDTO setMealDTO);

    /**
     * 套餐分类查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 套餐起售停售
     * @param status
     * @param id
     */
    void enbaleOrDisable(Integer status, Long id);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void updateWithDish(SetmealDTO setmealDTO);

    /**
     * 根据id查询套餐（及其关联的菜品）
     * @param id
     * @return
     */
    SetmealVO getByIdWithDishes(Long id);
}