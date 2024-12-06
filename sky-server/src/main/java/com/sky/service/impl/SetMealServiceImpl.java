package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Employee;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;

    /**
     * 新增套餐
     * @param setMealDTO
     */
    @Transactional
    public void addWithDish(SetmealDTO setMealDTO) {
        //在套餐表里加入数据
        Setmeal setMeal = new Setmeal();
        BeanUtils.copyProperties(setMealDTO, setMeal);
        setMealMapper.insert(setMeal);

        //获取生成的套餐id
        Long setMealId = setMeal.getId();

        List<SetmealDish> setMealDishList = setMealDTO.getSetmealDishes();

        //套餐id不是传入的数据，而是需要赋值的自增数据，对每个菜品都赋值套餐id
        setMealDishList.forEach(setMealDish -> {
            setMealDish.setSetmealId(setMealId);
        });

        //在套餐的菜品表中加入关联的数据
        setMealDishMapper.insertBatch(setMealDishList);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //基于SQL的limit关键字实现分页查询，后面的数字是具体的参数
        // select * from employee limit 0,10

        //开始分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page=setMealMapper.pageQuery(setmealPageQueryDTO);

        //加工成期望的返回结果
        long total=page.getTotal();
        List<SetmealVO> records=page.getResult();
        return new PageResult(total,records);
    }
}
