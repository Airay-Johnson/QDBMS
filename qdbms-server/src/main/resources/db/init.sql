-- ===================================================
-- QDBMS 数据库迁移: openGauss → MySQL 8.0
-- 精简为10张核心表
-- ===================================================

CREATE DATABASE IF NOT EXISTS qdbms DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE qdbms;

-- 1. 用户表
DROP TABLE IF EXISTS answer;
DROP TABLE IF EXISTS response;
DROP TABLE IF EXISTS respondent;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS questionnaire;
DROP TABLE IF EXISTS role_permission;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt加密',
    email VARCHAR(100),
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色表
CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 权限表
CREATE TABLE permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 用户角色关联
CREATE TABLE user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 5. 角色权限关联
CREATE TABLE role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_perm (role_id, perm_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (perm_id) REFERENCES permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 6. 问卷表
CREATE TABLE questionnaire (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status TINYINT DEFAULT 0 COMMENT '0=草稿 1=已发布 2=已关闭',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME,
    closed_at DATETIME,
    FOREIGN KEY (creator_id) REFERENCES user(id),
    INDEX idx_creator (creator_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷表';

-- 7. 问题表
CREATE TABLE question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'single=单选 multiple=多选 text=文本',
    options JSON COMMENT '[{"label":"A","value":"选项A"}]',
    sequence_number INT DEFAULT 0,
    is_required TINYINT(1) DEFAULT 1,
    FOREIGN KEY (questionnaire_id) REFERENCES questionnaire(id) ON DELETE CASCADE,
    INDEX idx_questionnaire (questionnaire_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题表';

-- 8. 受访者表
CREATE TABLE respondent (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL,
    name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    group_name VARCHAR(50),
    FOREIGN KEY (questionnaire_id) REFERENCES questionnaire(id) ON DELETE CASCADE,
    INDEX idx_questionnaire_resp (questionnaire_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='受访者表';

-- 9. 答卷表
CREATE TABLE response (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL,
    respondent_id BIGINT,
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    FOREIGN KEY (questionnaire_id) REFERENCES questionnaire(id),
    FOREIGN KEY (respondent_id) REFERENCES respondent(id) ON DELETE SET NULL,
    INDEX idx_questionnaire_resp2 (questionnaire_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答卷表';

-- 10. 答案明细表
CREATE TABLE answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    response_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT COMMENT '文本题答案',
    answer_options JSON COMMENT '选择题答案(支持多选)',
    FOREIGN KEY (response_id) REFERENCES response(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(id),
    INDEX idx_response (response_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答案明细表';

-- ===================================================
-- 初始化数据
-- ===================================================

-- 角色
INSERT INTO role (role_name, description) VALUES
('ADMIN', '系统管理员，拥有所有权限'),
('USER', '普通用户，可创建和管理自己的问卷'),
('ANALYST', '数据分析师，可查看统计数据');

-- 权限
INSERT INTO permission (perm_name, description) VALUES
('survey:create', '创建问卷'),
('survey:edit', '编辑问卷'),
('survey:delete', '删除问卷'),
('survey:publish', '发布问卷'),
('survey:view', '查看问卷'),
('user:manage', '管理用户'),
('role:manage', '管理角色'),
('data:export', '导出数据'),
('data:view', '查看统计数据');

-- 管理员角色拥有所有权限
INSERT INTO role_permission (role_id, perm_id)
SELECT 1, id FROM permission;

-- 普通用户权限
INSERT INTO role_permission (role_id, perm_id)
SELECT 2, id FROM permission WHERE perm_name IN ('survey:create','survey:edit','survey:delete','survey:publish','survey:view','data:view','data:export');

-- 分析师权限
INSERT INTO role_permission (role_id, perm_id)
SELECT 3, id FROM permission WHERE perm_name IN ('survey:view','data:view','data:export');

-- 默认管理员 (密码: admin123)
INSERT INTO user (username, password, email, is_active) VALUES
('admin', '$2a$10$v0eCOSNUKWWdce80OOdsY.QKy38EDvkxO0W7ZGjsmzDTWPkoFNfEi', 'admin@qdbms.com', 1);

-- 分配管理员角色
INSERT INTO user_role (user_id, role_id) VALUES (1, 1);
