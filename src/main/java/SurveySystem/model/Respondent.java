package SurveySystem.model;

import java.time.LocalDateTime;

public class Respondent {
    private Long rid; // 受访者ID
    private Integer age; // 年龄 (18-99)
    private String sex; // 性别 (MALE, FEMALE, OTHER)
    private String address; // 地址
    private String email; // 邮箱 (非空)

    // 无参构造函数
    public Respondent() {
    }

    // 带参构造函数
    public Respondent(Long rid, Integer age, String sex, String address, String email) {
        this.rid = rid;
        this.age = age;
        this.sex = sex;
        this.address = address;
        this.email = email;
    }

    // Getter 和 Setter 方法
    public Long getRid() {
        return rid;
    }

    public void setRid(Long rid) {
        this.rid = rid;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        // 验证年龄范围 (18-99)
        if (age != null && (age < 18 || age >= 100)) {
            throw new IllegalArgumentException("年龄必须在18到99岁之间");
        }
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        // 验证性别选项
        if (sex != null && !sex.equals("MALE") && !sex.equals("FEMALE") && !sex.equals("OTHER")) {
            throw new IllegalArgumentException("性别必须是'MALE', 'FEMALE'或'OTHER'");
        }
        this.sex = sex;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        // 验证邮箱非空
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        this.email = email;
    }


    @Override
    public String toString() {
        return "Respondent{" +
                "rid=" + rid +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    // 实用方法：验证对象是否有效
    public boolean isValid() {
        return email != null && !email.trim().isEmpty() &&
                age != null && age >= 18 && age < 100 &&
                sex != null && (sex.equals("MALE") || sex.equals("FEMALE") || sex.equals("OTHER"));
    }
}