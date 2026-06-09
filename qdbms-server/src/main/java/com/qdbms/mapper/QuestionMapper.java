package com.qdbms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qdbms.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("SELECT * FROM question WHERE questionnaire_id = #{qid} ORDER BY sequence_number ASC")
    List<Question> findByQuestionnaireId(@Param("qid") Long questionnaireId);

    @Select("SELECT COALESCE(MAX(sequence_number), 0) FROM question WHERE questionnaire_id = #{qid}")
    int maxSequenceNumber(@Param("qid") Long questionnaireId);
}
