package com.itheima.reggie.controller;


        import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
        import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
        import com.itheima.reggie.common.BaseContext;
        import com.itheima.reggie.common.R;
        import com.itheima.reggie.entity.Orders;
        import com.itheima.reggie.service.OrdersService;
        import lombok.extern.slf4j.Slf4j;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info(orders.toString());
        ordersService.submit(orders);
        return R.success("下单成功");
    }
    @GetMapping("/userPage")
    public R<Page<Orders>> page(Integer page, Integer pageSize){
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId,userId);

        ordersService.page(ordersPage,wrapper);
        return R.success(ordersPage);
    }
}
