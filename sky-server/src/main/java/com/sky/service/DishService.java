package com.sky.service;

import com.sky.dto.DishDTO;

public interface DishService {

    /**
     * 新增菜品（含口味）
     * @param dishDTO
     */
    void addWithFlavor(DishDTO dishDTO);
}
