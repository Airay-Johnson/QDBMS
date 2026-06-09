package com.qdbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qdbms.entity.Role;
import com.qdbms.entity.UserRole;
import com.qdbms.mapper.RoleMapper;
import com.qdbms.mapper.RolePermissionMapper;
import com.qdbms.mapper.UserRoleMapper;
import com.qdbms.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public List<Role> listAll() {
        return roleMapper.selectList(null);
    }

    @Override
    public Role getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        return role;
    }

    @Override
    @Transactional
    public Role create(String roleName, String description) {
        Role existing = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleName, roleName));
        if (existing != null) {
            throw new IllegalArgumentException("角色名已存在");
        }
        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(description);
        roleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional
    public Role update(Long id, String roleName, String description) {
        Role role = getById(id);
        if (roleName != null) role.setRoleName(roleName);
        if (description != null) role.setDescription(description);
        roleMapper.updateById(role);
        return role;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = getById(id);
        // 检查是否有用户关联
        Long count = userRoleMapper.selectCount(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        if (count > 0) {
            throw new IllegalArgumentException("该角色下还有 " + count + " 个用户，无法删除");
        }
        rolePermissionMapper.deleteByRoleId(id);
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permIds) {
        getById(roleId);
        rolePermissionMapper.deleteByRoleId(roleId);
        rolePermissionMapper.batchInsert(roleId, permIds);
    }
}
