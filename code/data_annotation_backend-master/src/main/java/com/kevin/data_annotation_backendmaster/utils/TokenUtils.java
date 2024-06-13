package com.kevin.data_annotation_backendmaster.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.kevin.data_annotation_backendmaster.entity.User;
import com.kevin.data_annotation_backendmaster.mapper.UserMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class TokenUtils {
    private static UserMapper staticUserMapper;

    @Resource
    UserMapper userMapper;

    @PostConstruct
    public void setUserService() {
        staticUserMapper = userMapper;
    }

    // 生成token
    public static String createToken(String uid, String sign) {
        return JWT.create().withAudience(uid)  // 将uid保存到 token 里面,作为载荷
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2))  // 2小时后token过期
                .sign(Algorithm.HMAC256(sign));  // 以 password 作为 token 的密钥
    }

    // 获取当前登陆的用户信息
    public static User getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if (StrUtil.isNotBlank(token)) {
                String uid = JWT.decode(token).getAudience().get(0);
                return staticUserMapper.selectById(Integer.valueOf(uid));
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
