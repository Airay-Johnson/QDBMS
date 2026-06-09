-- ============================================================
-- 问卷调查系统 - 数据库初始化脚本（统一小写列名版）
-- 数据库: OpenGauss
-- ============================================================

-- 1. 创建数据库（使用 OpenGauss 语法）
-- 请在 OpenGauss 管理工具中执行: CREATE DATABASE survey_db;

-- 2. 设置搜索路径
CREATE SCHEMA IF NOT EXISTS public;
SET search_path TO public;

-- ============================================================
-- 用户表 (User)
-- ============================================================
CREATE TABLE IF NOT EXISTS "User" (
    uid BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- ============================================================
-- 问卷表 (Questionnaire)
-- ============================================================
CREATE TABLE IF NOT EXISTS Questionnaire (
    id BIGSERIAL PRIMARY KEY,
    uid BIGINT NOT NULL REFERENCES "User"(uid),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED')),
    is_deleted BOOLEAN DEFAULT false
);

-- ============================================================
-- 问题表 (Question)
-- ============================================================
CREATE TABLE IF NOT EXISTS Question (
    qid BIGSERIAL PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL REFERENCES Questionnaire(id) ON DELETE CASCADE,
    qtext TEXT NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TEXT')),
    options JSON,
    is_required BOOLEAN DEFAULT false,
    sequence_number INT NOT NULL
);

-- ============================================================
-- 受访者表 (Respondent)
-- ============================================================
CREATE TABLE IF NOT EXISTS Respondent (
    rid BIGSERIAL PRIMARY KEY,
    age INT CHECK(age >= 18 AND age < 100),
    sex VARCHAR(10) CHECK(sex IN ('MALE', 'FEMALE', 'OTHER')),
    address VARCHAR(250),
    email VARCHAR(100) NOT NULL UNIQUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 响应表 (Response) - 关联受访者
-- ============================================================
CREATE TABLE IF NOT EXISTS Response (
    reid BIGSERIAL PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL REFERENCES Questionnaire(id),
    respondent_id BIGINT NOT NULL REFERENCES Respondent(rid),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    is_completed BOOLEAN DEFAULT false
);

-- ============================================================
-- 答案表 (Answer)
-- ============================================================
CREATE TABLE IF NOT EXISTS Answer (
    answer_id BIGSERIAL PRIMARY KEY,
    response_id BIGINT NOT NULL REFERENCES Response(reid) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES Question(qid),
    answer_text TEXT NOT NULL
);

-- ============================================================
-- 权限表 (Permission)
-- ============================================================
CREATE TABLE IF NOT EXISTS Permission (
    pid BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200) NOT NULL
);

-- ============================================================
-- 角色表 (Role)
-- ============================================================
CREATE TABLE IF NOT EXISTS Role (
    roid BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200) NOT NULL
);

-- ============================================================
-- 用户角色关联表 (User_Role)
-- ============================================================
CREATE TABLE IF NOT EXISTS User_Role (
    user_id BIGINT NOT NULL REFERENCES "User"(uid),
    role_id BIGINT NOT NULL REFERENCES Role(roid),
    PRIMARY KEY(user_id, role_id)
);

-- ============================================================
-- 角色权限关联表 (Role_Permission)
-- ============================================================
CREATE TABLE IF NOT EXISTS Role_Permission (
    role_id BIGINT NOT NULL REFERENCES Role(roid),
    permission_id BIGINT NOT NULL REFERENCES Permission(pid),
    PRIMARY KEY(role_id, permission_id)
);

-- ============================================================
-- 用户权限关联表 (User_Permission) - 直接赋予用户的权限
-- ============================================================
CREATE TABLE IF NOT EXISTS User_Permission (
    user_id BIGINT NOT NULL REFERENCES "User"(uid),
    permission_id BIGINT NOT NULL REFERENCES Permission(pid),
    PRIMARY KEY(user_id, permission_id)
);

-- ============================================================
-- 日志表 (Log)
-- ============================================================
CREATE TABLE IF NOT EXISTS "Log" (
    lid BIGSERIAL PRIMARY KEY,
    uid BIGINT NOT NULL REFERENCES "User"(uid),
    operation VARCHAR(100) NOT NULL,
    target VARCHAR(250) NOT NULL,
    details TEXT,
    op_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);

-- ============================================================
-- 索引创建
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_questionnaire_uid ON Questionnaire(uid);
CREATE INDEX IF NOT EXISTS idx_questionnaire_status ON Questionnaire(status);
CREATE INDEX IF NOT EXISTS idx_question_questionnaire_id ON Question(questionnaire_id);
CREATE INDEX IF NOT EXISTS idx_question_type ON Question(type);
CREATE UNIQUE INDEX IF NOT EXISTS idx_respondent_email ON Respondent(email);
CREATE INDEX IF NOT EXISTS idx_respondent_demographics ON Respondent(age, sex);
CREATE INDEX IF NOT EXISTS idx_response_questionnaire ON Response(questionnaire_id);
CREATE INDEX IF NOT EXISTS idx_response_respondent ON Response(respondent_id);
CREATE INDEX IF NOT EXISTS idx_answer_question_id ON Answer(question_id);
CREATE INDEX IF NOT EXISTS idx_answer_response_id ON Answer(response_id);
CREATE INDEX IF NOT EXISTS idx_user_username ON "User"(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON "User"(email);
CREATE INDEX IF NOT EXISTS idx_log_operation_time ON "Log"(op_time);

-- ============================================================
-- 插入初始角色
-- ============================================================
INSERT INTO Role (name, description) VALUES
    ('ADMIN', '系统管理员，拥有所有权限'),
    ('USER', '普通用户，可以创建和管理自己的问卷'),
    ('VIEWER', '查看者，仅能查看数据')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- 插入初始权限（PermissionType 枚举值）
-- ============================================================
INSERT INTO Permission (name, description) VALUES
    ('SURVEY_CREATE', '创建问卷'),
    ('SURVEY_EDIT', '编辑问卷'),
    ('SURVEY_PUBLISH', '发布问卷'),
    ('QUESTION_MANAGE', '管理问题'),
    ('RESPONDENT_MANAGE', '管理受访者'),
    ('RESPONSE_VIEW', '查看回答'),
    ('DATA_ANALYZE', '数据分析'),
    ('USER_MANAGE', '用户管理'),
    ('PERMISSION_GRANT', '权限授予'),
    ('LOG_VIEW', '查看日志'),
    ('DATA_EXPORT', '导出数据')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- 为 ADMIN 角色分配所有权限
-- ============================================================
INSERT INTO Role_Permission (role_id, permission_id)
SELECT r.roid, p.pid
FROM Role r, Permission p
WHERE r.name = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================================
-- 为 USER 角色分配基础权限
-- ============================================================
INSERT INTO Role_Permission (role_id, permission_id)
SELECT r.roid, p.pid
FROM Role r, Permission p
WHERE r.name = 'USER'
  AND p.name IN ('SURVEY_CREATE', 'SURVEY_EDIT', 'SURVEY_PUBLISH',
                 'QUESTION_MANAGE', 'RESPONSE_VIEW', 'DATA_ANALYZE', 'DATA_EXPORT')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================================
-- 为 VIEWER 角色分配查看权限
-- ============================================================
INSERT INTO Role_Permission (role_id, permission_id)
SELECT r.roid, p.pid
FROM Role r, Permission p
WHERE r.name = 'VIEWER'
  AND p.name IN ('RESPONSE_VIEW', 'DATA_ANALYZE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================================
-- 插入默认管理员账户 (密码: admin123, bcrypt 加密)
-- ============================================================
INSERT INTO "User" (username, password, email) VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDHfFsFbEyPJDQz7R2w/8y8cT9w5e1O', 'admin@example.com')
ON CONFLICT (username) DO NOTHING;

-- 为管理员分配 ADMIN 角色
INSERT INTO User_Role (user_id, role_id)
SELECT u.uid, r.roid
FROM "User" u, Role r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ============================================================
-- 提示信息
-- ============================================================
SELECT '数据库初始化完成！' AS message;
SELECT '默认管理员账号: admin / admin123' AS notice;
