package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /**
     * 根据菜品id查询套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetMealIdsByDishIds(List<Long> dishIds);


    /**
     * 批量添加菜品
     */
    void insertBatch(List<SetmealDish> dishes);


    /**
     * 根据套餐id删除套餐关联菜品
     * @param setMealId
     */
    @Delete("delete from setmeal_dish where setmeal_dish.setmeal_id =#{setMealId}")
    void deleteBySetMealId(Long setMealId);

    /**
     * 根据套餐id查询关联的菜品
     * @param setMealId
     * @return
     */
    List<SetmealDish> getDishesBySetMealId(Long setMealId);

    /**
     * 根据套餐id批量删除套餐的关联菜品数据
     * @param setMealIds
     */
    void deleteBatchBySetMealIds(List<Long> setMealIds);
}
