### 项目描述

这是一款专门为餐饮企业（餐厅、饭店）定制的一款软件产品，包括系统管理后台和移动端应用两部分，其中系统管理后台是提供给餐饮企业内部员工使用的，可以对餐厅的菜品、套餐、订单等进行管理维护。而移动端应用主要提供给消费者使用，可以在线浏览菜品，添加购物车、下单等业务功能

### 功能清单

系统管理后台功能模块

[reggie_take_out/外卖后台功能导图.png at v1.2 · kevinxiaomanong/reggie_take_out (github.com)](https://github.com/kevinxiaomanong/reggie_take_out/blob/v1.2/外卖后台功能导图.png)



移动端应用模块

[reggie_take_out/外卖后台功能导图.png at v1.2 · kevinxiaomanong/reggie_take_out (github.com)](https://github.com/kevinxiaomanong/reggie_take_out/blob/v1.2/外卖移动端功能导图.png)



### 效果预览

[reggie_take_out/外卖后台功能导图.png at v1.2 · kevinxiaomanong/reggie_take_out (github.com)](https://github.com/kevinxiaomanong/reggie_take_out/blob/v1.2/外卖项目效果展示.png)




### 技术选型

[reggie_take_out/外卖后台功能导图.png at v1.2 · kevinxiaomanong/reggie_take_out (github.com)](https://github.com/kevinxiaomanong/reggie_take_out/blob/v1.2/外卖项目技术选型.png)



### 项目优化

#### 一、Redis缓存

**背景：**当用户数量多，系统访问量大，频繁访问数据库，系统性能下降，导致用户体验差

**优化方案**：

1.缓存短信验证码：登录时候的短信验证码之前我们是保存在HttpSession中的，现在改造为将验证码存放在Redis缓存中

2.缓存菜品和套餐数据：移动端菜品查看功能，对应服务端方法为DishController的list方法，此方法会根据前端提交的查询条件进行数据库查询操作，现在我们改造list方法，让它先从Redis中获取数据，如果没有获取到再去查询数据库



#### 二、MySQL读写分离

**背景**：读和写所有压力都由一台数据库承担，压力大数据库服务器磁盘损坏则数据丢失，单点故障

**优化方案**：利用MySQL主从复制的特性实现读写分离操作

即我们开设两台数据库服务，一台作主库专门负责写操作（insert、update、delete），另一台专门做从库负责读操作（select），同时利用主从复制的特性使得主库和从库之间保证数据同步



#### 三、Nginx部署静态资源

Nginx可以作为静态web服务器来部署静态资源，相比于Tomcat，Nginx处理静态资源的能力更加高效，所以我们将静态资源部署到Nginx中，只需要将文件复制到Nginx安装目录下的html目录中即可



