package com.qdbms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qdbms.entity.Questionnaire;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface QuestionnaireMapper extends BaseMapper<Questionnaire> {

    @Select("SELECT * FROM questionnaire WHERE creator_id = #{creatorId} ORDER BY created_at DESC")
    List<Questionnaire> findByCreatorId(@Param("creatorId") Long creatorId);
}
