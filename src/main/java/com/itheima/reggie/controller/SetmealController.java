package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("添加成功");
    }

    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name){
        //这里之所以使用dtoPage是因为我们在显示页面上要有该套餐所属的分类名
        Page<Setmeal> ipage = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>(page,pageSize);

        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name!=null,Setmeal::getName,name);
        wrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(ipage,wrapper);

        BeanUtils.copyProperties(ipage,dtoPage,"records");
        List<SetmealDto> list = new ArrayList<>();
        List<Setmeal> records = ipage.getRecords();

        for(Setmeal setmeal:records){
            Category category = categoryService.getById(setmeal.getCategoryId());
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto);
            setmealDto.setCategoryName(category.getName());
            list.add(setmealDto);
        }
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    @DeleteMapping
    public R<String> delete(@RequestBody List<Long> ids){

        setmealService.removeWithDish(ids);
        return R.success("删除成功");

    }

    @PostMapping("/status/{value}")
    public R<String> statusChange(@RequestParam List<Long> ids,@PathVariable Integer value){
        setmealService.statusChange(value,ids);
        return R.success("操作成功");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        Setmeal setmeal = setmealService.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(wrapper);

        setmealDto.setSetmealDishes(list);
        return R.success(setmealDto);

    }

    @PutMapping
    public R<String> updateWithDish(@RequestBody SetmealDto setmealDto){

        log.info(setmealDto.toString());
        setmealService.updateWithDish(setmealDto);

        return R.success("修改成功");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Long categoryId,Integer status){
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getCategoryId,categoryId);
        wrapper.eq(Setmeal::getStatus,status);
        wrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(wrapper);
        return R.success(setmealList);
    }

}
