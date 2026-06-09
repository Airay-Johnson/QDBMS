package com.qdbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qdbms.entity.User;
import com.qdbms.entity.UserRole;
import com.qdbms.mapper.UserMapper;
import com.qdbms.mapper.UserRoleMapper;
import com.qdbms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public IPage<User> list(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(User::getUsername, keyword).or().like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        IPage<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public User update(Long id, String email, Boolean isActive) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (email != null) user.setEmail(email);
        if (isActive != null) user.setIsActive(isActive);
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        userMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 清除旧角色
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));
        // 分配新角色
        for (Long roleId : roleIds) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    @Override
    public List<String> getRoleNames(Long userId) {
        return userRoleMapper.findRoleNamesByUserId(userId);
    }
}
