package com.qdbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qdbms.entity.Respondent;
import com.qdbms.mapper.RespondentMapper;
import com.qdbms.service.RespondentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentServiceImpl implements RespondentService {

    private final RespondentMapper respondentMapper;

    @Override
    public IPage<Respondent> listByQuestionnaire(Long questionnaireId, int page, int size) {
        LambdaQueryWrapper<Respondent> wrapper = new LambdaQueryWrapper<Respondent>()
                .eq(Respondent::getQuestionnaireId, questionnaireId)
                .orderByDesc(Respondent::getId);
        return respondentMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Respondent getById(Long id) {
        Respondent r = respondentMapper.selectById(id);
        if (r == null) {
            throw new IllegalArgumentException("受访者不存在");
        }
        return r;
    }

    @Override
    @Transactional
    public Respondent create(Respondent respondent) {
        respondentMapper.insert(respondent);
        return respondent;
    }

    @Override
    @Transactional
    public Respondent update(Long id, Respondent respondent) {
        Respondent existing = getById(id);
        existing.setName(respondent.getName());
        existing.setEmail(respondent.getEmail());
        existing.setPhone(respondent.getPhone());
        existing.setGroupName(respondent.getGroupName());
        respondentMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getById(id);
        respondentMapper.deleteById(id);
    }

    @Override
    @Transactional
    public int importFromExcel(Long questionnaireId, InputStream inputStream) {
        int count = 0;
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Respondent r = new Respondent();
                r.setQuestionnaireId(questionnaireId);
                r.setName(getCellString(row, 0));
                r.setEmail(getCellString(row, 1));
                r.setPhone(getCellString(row, 2));
                r.setGroupName(getCellString(row, 3));

                if (r.getName() != null || r.getEmail() != null) {
                    respondentMapper.insert(r);
                    count++;
                }
            }
        } catch (Exception e) {
            log.error("Excel导入失败", e);
            throw new RuntimeException("Excel导入失败: " + e.getMessage());
        }
        return count;
    }

    @Override
    public List<Respondent> listByGroup(Long questionnaireId, String groupName) {
        LambdaQueryWrapper<Respondent> wrapper = new LambdaQueryWrapper<Respondent>()
                .eq(Respondent::getQuestionnaireId, questionnaireId)
                .eq(groupName != null, Respondent::getGroupName, groupName);
        return respondentMapper.selectList(wrapper);
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> cell.toString();
        };
    }
}
