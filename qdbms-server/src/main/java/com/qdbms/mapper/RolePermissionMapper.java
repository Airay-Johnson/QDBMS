package com.qdbms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<com.qdbms.entity.RolePermission> {

    @Select("SELECT rp.role_id as roleId, rp.perm_id as permId, p.perm_name as permName FROM role_permission rp JOIN permission p ON rp.perm_id = p.id WHERE rp.role_id = #{roleId}")
    List<java.util.Map<String, Object>> findByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM role_permission WHERE role_id = #{roleId}")
    void deleteByRoleId(@Param("roleId") Long roleId);

    @Insert("<script>" +
            "INSERT INTO role_permission (role_id, perm_id) VALUES " +
            "<foreach collection='permIds' item='pid' separator=','>" +
            "(#{roleId}, #{pid})" +
            "</foreach>" +
            "</script>")
    void batchInsert(@Param("roleId") Long roleId, @Param("permIds") List<Long> permIds);
}
