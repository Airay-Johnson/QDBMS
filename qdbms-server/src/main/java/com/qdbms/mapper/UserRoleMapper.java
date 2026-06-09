package com.qdbms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<com.qdbms.entity.UserRole> {

    @Select("SELECT r.role_name FROM user_role ur JOIN role r ON ur.role_id = r.id WHERE ur.user_id = #{userId}")
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);
}
