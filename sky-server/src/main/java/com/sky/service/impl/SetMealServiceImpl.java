package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Employee;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
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
    @Autowired
    private DishMapper dishMapper;

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

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     */
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

    /**
     * 套餐起售停售
     * @param status
     * @param id
     */
    public void enbaleOrDisable(Integer status, Long id) {
        //考虑当套餐内包含停售的菜品，则不能起售
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(status == StatusConstant.ENABLE){
            //select d.* from dish d left join setmeal_dish s on d.id = s.dish_id where s.setmeal_id = #{setmealId}
            List<Dish> dishList = dishMapper.getBySetMealId(id);
            if(dishList != null && dishList.size() > 0){
                dishList.forEach(dish -> {
                    if(dish.getStatus() == StatusConstant.DISABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        Setmeal setmeal=Setmeal.builder()
                .status(status)
                .id(id)
                .build();

        setMealMapper.update(setmeal);


    }

    /**
     * 修改套餐（套餐中关联菜品也要更新）
     * @param setmealDTO
     */
    @Transactional
    public void updateWithDish(SetmealDTO setmealDTO) {
        Setmeal setMeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setMeal);
        //需要在前端回显
        //查setMeal表:SELECT * FROM set_meal s where s.id =#{id}
        setMealMapper.getById(setMeal.getId());

        //进行update修改基本信息
        //对setMeal表进行操作
        setMealMapper.update(setMeal);

        //对于关联的套餐菜品数据需要额外处理:调用setMealDishMapper的方法update? 并不是
        //对setMealDish表进行操作:先全部删除，再重新插入
        setMealDishMapper.deleteBySetMealId(setMeal.getId());
        List<SetmealDish> setMealDishList = setmealDTO.getSetmealDishes();
        if(setMealDishList.size()>0 && setMealDishList!=null ){
            setMealDishMapper.insertBatch(setMealDishList);
        }

    }

    /**
     * 根据id查询套餐（及其关联的菜品）
     * @param id
     * @return
     */
    @Transactional
    public SetmealVO getByIdWithDishes(Long id) {
        //很明显涉及两个表的数据
        //先查setMeal表
        Setmeal setmeal=setMealMapper.getById(id);

        //再根据套餐id查setMealDish表
        List<SetmealDish> setmealDishes=setMealDishMapper.getDishesBySetMealId(setmeal.getId());

        //将查询结果封装到VO
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //如果套餐处于起售状态则不能删除
        for(Long id:ids){
            Setmeal setmeal=setMealMapper.getById(id);
            if(setmeal.getStatus()==StatusConstant.ENABLE){
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //用循环删除
//        ids.forEach(setmealId -> {
//            //删除套餐表中的数据
//            setmealMapper.deleteById(setmealId);
//            //删除套餐菜品关系表中的数据
//            setmealDishMapper.deleteBySetmealId(setmealId);
//        });

        //批量删除，要注意有两个表数据要删除
        setMealMapper.deleteBatchByIds(ids);
        setMealDishMapper.deleteBatchBySetMealIds(ids);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setMealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setMealMapper.getDishItemBySetmealId(id);
    }
}
