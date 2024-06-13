package com.kevin.data_annotation_backendmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kevin.data_annotation_backendmaster.entity.Role;
import com.kevin.data_annotation_backendmaster.entity.User;
import com.kevin.data_annotation_backendmaster.entity.UserRole;
import com.kevin.data_annotation_backendmaster.exception.ServiceException;
import com.kevin.data_annotation_backendmaster.mapper.RoleMapper;
import com.kevin.data_annotation_backendmaster.mapper.UserMapper;
import com.kevin.data_annotation_backendmaster.mapper.UserRoleMapper;
import com.kevin.data_annotation_backendmaster.service.IUserService;
import com.kevin.data_annotation_backendmaster.utils.TokenUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    UserMapper userMapper;

    @Resource
    UserRoleMapper userRoleMapper;

    @Resource
    RoleMapper roleMapper;

    public User selectByUsername(String username) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }

    private Role selectRoleByUerId(Integer id) {
        // 先根据user的id在user_role表里查询出一条userRole
        UserRole userRole = userRoleMapper.selectOne(new QueryWrapper<UserRole>().eq("uid", id));
        // 拿到userRole的rid并在role里查出对应的role
        return roleMapper.selectById(userRole.getRid());
    }

    // 验证用户账户是否合法
    public User login(User user) {
        // 查询登录传过来的user对象在不在数据库
        User dbUser = selectByUsername(user.getUsername());
        if (dbUser == null) {  // 查不到
            throw new ServiceException("用户名或密码错误");
        }
        if (!user.getPassword().equals(dbUser.getPassword())) {  // 密码不对
            throw new ServiceException("用户名或密码错误");
        }

        // 根据user的id查询user对应的role。具体过程：先根据user的id在user_role表里查询对应uid对应的rid，再通过查到的rid查询role表里的role角色
        // 拿到role表查到的字段role并赋值给dbUser
        String role = selectRoleByUerId(dbUser.getId()).getRole();
        dbUser.setRole(role);

        // 生成token并赋值给dbUser
        String token = TokenUtils.createToken(dbUser.getId().toString(), dbUser.getPassword());
        dbUser.setToken(token);
        return dbUser;
    }

    public User register(User user) {
        User dbUser = selectByUsername(user.getUsername());
        if (dbUser != null) {
            throw new ServiceException("用户名已存在");
        }
        user.setName(user.getUsername());
        userMapper.insert(user);
        System.out.println("插入后的用户ID：" + user.getId());
        // 别忘了添加默认赋予的标注者角色信息
        UserRole userRole = new UserRole();
        Integer rid = roleMapper.selectOne(new QueryWrapper<Role>().eq("role", "标注者")).getId();
        userRole.setUid(user.getId());
        userRole.setRid(rid);
        userRoleMapper.insert(userRole);
        return user;
    }

    @Override
    public List<String> getUsernames() {
        return userMapper.getUsernames();
    }

    @Override
    public Integer getIdByUsername(String username) {
        return userMapper.getIdByUsername(username);
    }
}
