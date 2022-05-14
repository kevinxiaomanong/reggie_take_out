package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 */

@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.定义不需要处理的请求路径
        String requestURI = request.getRequestURI();

        log.info("拦截到请求：{}",requestURI);

        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
        };

        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3.不需要处理
        if(check){
            filterChain.doFilter(request,response);
            return;
        }

        //4.需要处理时我们先去看是否已经登录
        if(request.getSession().getAttribute("employee")!=null){
            //已经登录了 放行
            Long id = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(id);

            filterChain.doFilter(request,response);
            return;
        }

        //查看移动端是否登录
        if(request.getSession().getAttribute("user")!=null){
            //已经登录了 放行
            Long id = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(id);

            filterChain.doFilter(request,response);
            return;
        }

        //5.报错误信息 前端js控制跳转 通过输出流response回写数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    public boolean check(String[] uris,String requestURI){
        for (String uri:uris){
            boolean match = PATH_MATCHER.match(uri, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
