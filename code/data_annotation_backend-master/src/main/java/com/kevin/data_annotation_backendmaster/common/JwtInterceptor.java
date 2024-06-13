package com.kevin.data_annotation_backendmaster.common;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.kevin.data_annotation_backendmaster.entity.User;
import com.kevin.data_annotation_backendmaster.exception.ServiceException;
import com.kevin.data_annotation_backendmaster.mapper.UserMapper;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtInterceptor implements HandlerInterceptor {
    @Resource
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取token
        String token = request.getHeader("token");  //  header里面传过来的参数
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");  // url参数  ?token=xxxx
        }

        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        // 执行认证
        if (StrUtil.isBlank(token)) {
            throw new ServiceException("401", "请登录");
        }

        // 获取 token 中的 user id
        String uid;

        try {
            uid = JWT.decode(token).getAudience().get(0);   // JWT.decode(token) 解码  jwt token
        } catch (JWTDecodeException j) {
            throw new ServiceException("401", "请登录");
        }

        // 根据token中的uid查询数据库，拿到一个user对象
        User user = userMapper.selectById(Integer.valueOf(uid));
        if (user == null) {
            throw new ServiceException("401", "请登录");
        }

        // 通过用户密码加密之后生成一个验证器
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
        try {
            jwtVerifier.verify(token); // 验证token
        } catch (JWTVerificationException e) {
            throw new ServiceException("401", "请登录");
        }
        return true;
    }
}
