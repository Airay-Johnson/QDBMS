package com.qdbms.controller;

import com.qdbms.common.PageResult;
import com.qdbms.common.Result;
import com.qdbms.entity.User;
import com.qdbms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<PageResult<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        var ipage = userService.list(page, size, keyword);
        return Result.ok(PageResult.of(ipage));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) Boolean isActive) {
        return Result.ok(userService.update(id, email, isActive));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok("删除成功", null);
    }

    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id,
                                    @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.ok("角色分配成功", null);
    }

    @GetMapping("/{id}/roles")
    public Result<List<String>> getRoles(@PathVariable Long id) {
        return Result.ok(userService.getRoleNames(id));
    }
}
