package com.qdbms.controller;

import com.qdbms.common.Result;
import com.qdbms.entity.Role;
import com.qdbms.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public Result<List<Role>> listAll() {
        return Result.ok(roleService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @PostMapping
    public Result<Role> create(@RequestParam String roleName,
                                @RequestParam String description) {
        return Result.ok(roleService.create(roleName, description));
    }

    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id,
                                @RequestParam(required = false) String roleName,
                                @RequestParam(required = false) String description) {
        return Result.ok(roleService.update(id, roleName, description));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok("删除成功", null);
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id,
                                           @RequestBody List<Long> permIds) {
        roleService.assignPermissions(id, permIds);
        return Result.ok("权限分配成功", null);
    }
}
