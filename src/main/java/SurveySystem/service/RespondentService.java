package SurveySystem.service;

import SurveySystem.dao.RespondentDao;
import SurveySystem.model.Respondent;

import java.util.List;

/**
 * 受访者业务逻辑层（RespondentService）
 * 封装受访者相关业务操作，包括注册、验证、查询、统计分析等
 */
public class RespondentService {

    private final RespondentDao respondentDao = new RespondentDao();

    /**
     * 注册新受访者
     * 如果邮箱已存在则复用该受访者记录
     * @param email 邮箱（必填）
     * @param age 年龄（可选，范围18-99）
     * @param sex 性别（可选，MALE/FEMALE/OTHER）
     * @param address 地址（可选）
     * @return 受访者对象，注册失败返回null
     */
    public Respondent register(String email, Integer age, String sex, String address) {
        // 参数校验
        if (email == null || email.trim().isEmpty()) {
            System.err.println("邮箱不能为空");
            return null;
        }

        email = email.trim();

        // 检查邮箱是否已存在
        Respondent existing = respondentDao.findByEmail(email);
        if (existing != null) {
            System.out.println("受访者已存在，直接返回: " + email);
            return existing;
        }

        // 创建新受访者
        Respondent respondent = new Respondent();
        respondent.setEmail(email);

        if (age != null) {
            try {
                respondent.setAge(age);
            } catch (IllegalArgumentException e) {
                System.err.println("年龄无效: " + age + ", 将设为null");
            }
        }

        if (sex != null && !sex.trim().isEmpty()) {
            try {
                respondent.setSex(sex.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("性别无效: " + sex + ", 将设为null");
            }
        }

        respondent.setAddress(address);

        Long rid = respondentDao.addRespondent(respondent);
        if (rid != null) {
            respondent.setRid(rid);
            System.out.println("受访者注册成功: RID=" + rid + ", EMAIL=" + email);
            return respondent;
        }

        return null;
    }

    /**
     * 根据邮箱查找受访者
     */
    public Respondent findByEmail(String email) {
        return respondentDao.findByEmail(email);
    }

    /**
     * 根据ID查找受访者
     */
    public Respondent findById(Long rid) {
        return respondentDao.findById(rid);
    }

    /**
     * 获取所有受访者列表
     */
    public List<Respondent> findAll() {
        return respondentDao.findAll();
    }

    /**
     * 更新受访者信息
     */
    public boolean update(Respondent respondent) {
        if (respondent == null || respondent.getRid() == null) {
            return false;
        }
        return respondentDao.update(respondent);
    }

    /**
     * 删除受访者
     */
    public boolean delete(Long rid) {
        return respondentDao.delete(rid);
    }

    /**
     * 获取受访者总数
     */
    public int count() {
        return respondentDao.count();
    }

    /**
     * 获取年龄分布统计
     * @return 列表每项为 [年龄段名称, 数量]
     */
    public List<Object[]> getAgeDistribution() {
        return respondentDao.countByAgeGroup();
    }

    /**
     * 获取性别分布统计
     * @return 列表每项为 [性别名称, 数量]
     */
    public List<Object[]> getSexDistribution() {
        return respondentDao.countBySex();
    }
}
