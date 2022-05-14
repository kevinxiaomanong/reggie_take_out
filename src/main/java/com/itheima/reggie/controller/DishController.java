package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        return R.success("保存成功");
    }



    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Dish::getIsDeleted,0);

        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        pageInfo = dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }


    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("操作成功");
    }

    @GetMapping("/list")
    public R<List<DishDto>> listByCategoryId(Long categoryId,Integer status){
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getCategoryId,categoryId);
        wrapper.eq(Dish::getStatus,status);
        wrapper.orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(wrapper);
        List<DishDto> dishDtoList = new ArrayList<>();

        LambdaQueryWrapper<DishFlavor> wrapper1 = new LambdaQueryWrapper<>();
        //把集合中每个菜的口味查找出来
        for(Dish dish:list){
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish,dishDto);
            Long id = dish.getId();
            wrapper1.eq(DishFlavor::getDishId,id);
            List<DishFlavor> dishFlavors = dishFlavorService.list(wrapper1);
            dishDto.setFlavors(dishFlavors);
            dishDtoList.add(dishDto);
        }
        return R.success(dishDtoList);
    }

    @PostMapping("/status/{value}")
    public R<String> statusChange(@PathVariable int value,@RequestParam List<Long> ids){
        //要先判断当前ids所代表的菜品是否可以被操作
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus,value);
        wrapper.in(Dish::getId,ids);
        int count = dishService.count(wrapper);

        if(count>0){
            return value==0?R.error("所选菜品中已经停售"):R.error("所选菜品中已启售");
        }

        for(Long id:ids){
            Dish dish = dishService.getById(id);
            dish.setStatus(value);
            dishService.updateById(dish);
        }
        return R.success("修改成功");
    }

    @DeleteMapping
    public R<String> deleteByIds(@RequestParam List<Long> ids){
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus,1);
        wrapper.in(Dish::getId,ids);
        int count = dishService.count(wrapper);

        if(count>0){
            return R.error("不能删除已启售的菜品");
        }

        //对于菜品的删除 我们是修改是否删除属性为1
        for (Long id:ids){
            Dish dish = dishService.getById(id);
            dish.setIsDeleted(1);
            dishService.updateById(dish);
        }
        return R.success("删除成功");
    }


}
