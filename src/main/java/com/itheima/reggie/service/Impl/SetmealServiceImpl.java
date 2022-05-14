package com.itheima.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;


@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //1.存储套餐信息
        this.save(setmealDto);

        //2.把套餐中相关的菜品关系加入进去
        //因为只有先存入套餐之后 套餐的id才知道 因为我们使用的是雪花算法
        //那么之后存储菜品的时候 要先把套餐的id赋值进去
        Long id = setmealDto.getId();

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for(SetmealDish setmealDish:setmealDishes){
            setmealDish.setSetmealId(id);
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //这里我们是不能删除已启动的套餐 要判断
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Setmeal::getId,ids);
        lambdaQueryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(lambdaQueryWrapper);
        if(count>0){
            throw new CustomException("套餐正在售卖中 不能删除");
        }

        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(wrapper);

        //2.再从套餐表中删除
        this.removeByIds(ids);
    }

    @Override
    public void statusChange(int value, List<Long> ids) {
        for(Long l:ids){
            Setmeal setmeal = this.getById(l);
            setmeal.setStatus(value);
            this.updateById(setmeal);
        }
    }

    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        //先删除菜品
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(wrapper);

        //然后给菜品附上套餐id后保存
        List<SetmealDish> list = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish:list){
            setmealDish.setSetmealId(setmealDto.getId());
        }

        setmealDishService.saveBatch(list);
    }

}
