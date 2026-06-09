package com.qdbms.service;

import com.qdbms.entity.Role;

import java.util.List;

public interface RoleService {
    List<Role> listAll();
    Role getById(Long id);
    Role create(String roleName, String description);
    Role update(Long id, String roleName, String description);
    void delete(Long id);
    void assignPermissions(Long roleId, List<Long> permIds);
}
