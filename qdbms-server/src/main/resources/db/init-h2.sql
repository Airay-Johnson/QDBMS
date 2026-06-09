-- ===================================================
-- QDBMS H2 数据库初始化（本地模式）
-- H2 MySQL 兼容模式
-- ===================================================

SET MODE MySQL;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 4. 用户角色关联
CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- 5. 角色权限关联
CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL,
    UNIQUE (role_id, perm_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (perm_id) REFERENCES permission(id) ON DELETE CASCADE
);

-- 6. 问卷表
CREATE TABLE IF NOT EXISTS questionnaire (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    closed_at TIMESTAMP NULL,
    FOREIGN KEY (creator_id) REFERENCES user(id)
);

-- 7. 问题表
CREATE TABLE IF NOT EXISTS question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    options TEXT,
    sequence_number INT DEFAULT 0,
    is_required BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (questionnaire_id) REFERENCES questionnaire(id) ON DELETE CASCADE
);

-- 8. 受访者表
CREATE TABLE IF NOT EXISTS respondent (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL,
    name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    group_name VARCHAR(50),
    FOREIGN KEY (questionnaire_id) REFERENCES questionnaire(id) ON DELETE CASCADE
);

-- 9. 答卷表
CREATE TABLE IF NOT EXISTS response (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL,
    respondent_id BIGINT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    FOREIGN KEY (questionnaire_id) REFERENCES questionnaire(id),
    FOREIGN KEY (respondent_id) REFERENCES respondent(id) ON DELETE SET NULL
);

-- 10. 答案明细表
CREATE TABLE IF NOT EXISTS answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    response_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    answer_options TEXT,
    FOREIGN KEY (response_id) REFERENCES response(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(id)
);

-- ===================================================
-- 初始化数据（使用 MERGE 避免重复插入）
-- ===================================================

MERGE INTO role (id, role_name, description) KEY(role_name) VALUES
(1, 'ADMIN', '系统管理员'),
(2, 'USER', '普通用户'),
(3, 'ANALYST', '数据分析师');

MERGE INTO permission (id, perm_name, description) KEY(perm_name) VALUES
(1, 'survey:create', '创建问卷'),
(2, 'survey:edit', '编辑问卷'),
(3, 'survey:delete', '删除问卷'),
(4, 'survey:publish', '发布问卷'),
(5, 'survey:view', '查看问卷'),
(6, 'user:manage', '管理用户'),
(7, 'role:manage', '管理角色'),
(8, 'data:export', '导出数据'),
(9, 'data:view', '查看统计数据');

-- 管理员角色拥有所有权限
MERGE INTO role_permission (role_id, perm_id) KEY(role_id, perm_id)
SELECT 1, id FROM permission WHERE NOT EXISTS (SELECT 1 FROM role_permission WHERE role_id = 1);

-- 普通用户权限
MERGE INTO role_permission (role_id, perm_id) KEY(role_id, perm_id)
SELECT 2, id FROM permission WHERE perm_name IN ('survey:create','survey:edit','survey:delete','survey:publish','survey:view','data:view','data:export')
AND NOT EXISTS (SELECT 1 FROM role_permission WHERE role_id = 2 AND perm_id = permission.id);

-- 分析师权限
MERGE INTO role_permission (role_id, perm_id) KEY(role_id, perm_id)
SELECT 3, id FROM permission WHERE perm_name IN ('survey:view','data:view','data:export')
AND NOT EXISTS (SELECT 1 FROM role_permission WHERE role_id = 3 AND perm_id = permission.id);

-- 默认管理员 (admin / admin123)
-- BCrypt hash: $2a$10$v0eCOSNUKWWdce80OOdsY.QKy38EDvkxO0W7ZGjsmzDTWPkoFNfEi
MERGE INTO user (id, username, password, email, is_active) KEY(username) VALUES
(1, 'admin', '$2a$10$v0eCOSNUKWWdce80OOdsY.QKy38EDvkxO0W7ZGjsmzDTWPkoFNfEi', 'admin@qdbms.com', TRUE);

-- 分配管理员角色
MERGE INTO user_role (user_id, role_id) KEY(user_id, role_id) VALUES (1, 1);
