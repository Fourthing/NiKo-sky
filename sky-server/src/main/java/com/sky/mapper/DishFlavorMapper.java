package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味数据
     * @param dishFlavors
     */
    void insertBatch(List<DishFlavor> dishFlavors);

    /**
     * 根据菜品id删除口味
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id=#{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 根据菜品id批量删除口味
     * @param dishIds
     */
    void deleteBatchByDishIds(List<Long> dishIds);
}
