package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //查看当前用户购物车是否已经有了该菜品/套餐
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userId);

        Long dishId = shoppingCart.getDishId();
        if(dishId!=null){
            wrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            wrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(wrapper);

        //如果有记录 将当前记录数量加一 否则新增一条记录
        if(one!=null){
            Integer number = one.getNumber();
            one.setNumber(number+1);
            shoppingCartService.updateById(one);
        }else{
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one=shoppingCart;
        }

        return R.success(one);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ShoppingCart::getCreateTime);
        wrapper.eq(ShoppingCart::getUserId,userId);

        List<ShoppingCart> list = shoppingCartService.list(wrapper);
        return R.success(list);
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userId);
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        if(dishId!=null){
            //要减少的是菜品
            wrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            //要减少的是套餐数量
            wrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }
        ShoppingCart one = shoppingCartService.getOne(wrapper);
        Integer number = one.getNumber();
        if(number==1){
            shoppingCartService.remove(wrapper);
        }else{
            one.setNumber(number-1);
            shoppingCartService.updateById(one);
        }
        return R.success(one);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(wrapper);
        return R.success("清空购物车成功");
    }

}
