package com.qdbms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qdbms.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);
}
