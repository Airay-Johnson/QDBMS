package com.qdbms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qdbms.entity.User;

import java.util.List;

public interface UserService {
    IPage<User> list(int page, int size, String keyword);
    User getById(Long id);
    User update(Long id, String email, Boolean isActive);
    void delete(Long id);
    void assignRoles(Long userId, List<Long> roleIds);
    List<String> getRoleNames(Long userId);
}
