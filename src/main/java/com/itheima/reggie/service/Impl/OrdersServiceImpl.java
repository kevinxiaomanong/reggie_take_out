package com.itheima.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper,Orders> implements OrdersService {

    @Autowired
    private UserService userService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;


    @Override
    @Transactional
    public void submit(Orders orders) {
        //获取当前用户id和地址id
        Long userId = BaseContext.getCurrentId();
        Long addressBookId = orders.getAddressBookId();

        //查询当前用户购物车数据
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartLambdaQueryWrapper);

        //查询当前用户信息
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getId,userId);
        User user = userService.getOne(userLambdaQueryWrapper);

        //根据地址orders里封装的id查询地址信息
        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper.eq(AddressBook::getId,addressBookId);
        AddressBook addressBook = addressBookService.getOne(addressBookLambdaQueryWrapper);

        //封装好订单数据 往订单表中存入数据
        long orderId = IdWorker.getId();//订单号
        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserId(userId);
        orders.setAddressBookId(addressBookId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());

        //计算总金额 利用购物车里的集合运算
        BigDecimal amount = new BigDecimal(0);
        for (ShoppingCart s:shoppingCartList){
            BigDecimal multiply = s.getAmount().multiply(BigDecimal.valueOf(s.getNumber()));
            amount.add(multiply);
        }
        orders.setAmount(amount);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        //到这里对于一条订单信息全部封装完成 填入表中即可
        this.save(orders);

        //往订单明细表中加入订单菜品信息
        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart s:shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(s.getName());
            orderDetail.setImage(s.getImage());
            orderDetail.setOrderId(orderId);
            orderDetail.setDishId(s.getDishId());
            orderDetail.setSetmealId(s.getSetmealId());
            orderDetail.setDishFlavor(s.getDishFlavor());
            orderDetail.setNumber(s.getNumber());
            orderDetail.setAmount(s.getAmount());
            orderDetailList.add(orderDetail);
        }
        orderDetailService.saveBatch(orderDetailList);

        //清空购物车
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    }
}
