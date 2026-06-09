#!/usr/bin/env python3
"""
QDBMS 数据库迁移脚本: openGauss → MySQL 8.0
使用方式:
  1. pip install psycopg2-binary pymysql
  2. 修改下方配置
  3. python migrate_opengauss_to_mysql.py
"""

import json
import sys
from datetime import datetime

try:
    import psycopg2
    import pymysql
except ImportError:
    print("请先安装依赖: pip install psycopg2-binary pymysql")
    sys.exit(1)

# ============== 配置 ==============
OPENGAUSS_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "database": "survey_db",
    "user": "gaussdb",
    "password": "your_password"
}

MYSQL_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "database": "qdbms",
    "user": "root",
    "password": "123456",
    "charset": "utf8mb4"
}
# ==================================


def get_og_conn():
    return psycopg2.connect(**OPENGAUSS_CONFIG)


def get_my_conn():
    return pymysql.connect(**MYSQL_CONFIG)


def migrate_users(og_cursor, my_cursor):
    """迁移用户表: User → user"""
    og_cursor.execute('SELECT uid, username, password, email, is_active FROM "User"')
    rows = og_cursor.fetchall()
    count = 0
    for row in rows:
        uid, username, password, email, is_active = row
        my_cursor.execute(
            "INSERT INTO user (id, username, password, email, is_active) "
            "VALUES (%s, %s, %s, %s, %s) "
            "ON DUPLICATE KEY UPDATE username=VALUES(username)",
            (uid, username, password, email, 1 if is_active else 0)
        )
        count += 1
    print(f"  [user] 迁移 {count} 条记录")
    return count


def migrate_questionnaires(og_cursor, my_cursor):
    """迁移问卷表: Questionnaire → questionnaire"""
    og_cursor.execute(
        'SELECT id, uid, title, description, create_time, status FROM Questionnaire WHERE is_deleted IS NOT TRUE'
    )
    rows = og_cursor.fetchall()
    count = 0
    status_map = {"DRAFT": 0, "PUBLISHED": 1, "CLOSED": 2}
    for row in rows:
        qid, uid, title, desc, create_time, status = row
        s = status_map.get(status, 0)
        pub_time = create_time if s == 1 else None
        close_time = create_time if s == 2 else None
        my_cursor.execute(
            "INSERT INTO questionnaire (id, creator_id, title, description, status, created_at, published_at, closed_at) "
            "VALUES (%s, %s, %s, %s, %s, %s, %s, %s) "
            "ON DUPLICATE KEY UPDATE title=VALUES(title)",
            (qid, uid, title, desc, s, create_time, pub_time, close_time)
        )
        count += 1
    print(f"  [questionnaire] 迁移 {count} 条记录")
    return count


def migrate_questions(og_cursor, my_cursor):
    """迁移问题表: Question → question"""
    og_cursor.execute(
        'SELECT qid, questionnaire_id, qtext, type, options, is_required, sequence_number FROM Question'
    )
    rows = og_cursor.fetchall()
    count = 0
    type_map = {"SINGLE_CHOICE": "single", "MULTIPLE_CHOICE": "multiple", "TEXT": "text"}
    for row in rows:
        qid, qnaire_id, qtext, qtype, options, required, seq = row
        my_cursor.execute(
            "INSERT INTO question (id, questionnaire_id, question_text, type, options, is_required, sequence_number) "
            "VALUES (%s, %s, %s, %s, %s, %s, %s) "
            "ON DUPLICATE KEY UPDATE question_text=VALUES(question_text)",
            (qid, qnaire_id, qtext, type_map.get(qtype, "text"),
             json.dumps(options) if isinstance(options, (list, dict)) else options,
             1 if required else 0, seq)
        )
        count += 1
    print(f"  [question] 迁移 {count} 条记录")
    return count


def migrate_respondents(og_cursor, my_cursor):
    """迁移受访者表: Respondent → respondent"""
    og_cursor.execute('SELECT rid, age, sex, address, email, create_time FROM Respondent')
    rows = og_cursor.fetchall()
    count = 0
    for row in rows:
        rid, age, sex, address, email, create_time = row
        my_cursor.execute(
            "INSERT INTO respondent (id, name, email, group_name) "
            "VALUES (%s, %s, %s, %s) "
            "ON DUPLICATE KEY UPDATE email=VALUES(email)",
            (rid, f"{sex}_{age}" if sex or age else None, email, address)
        )
        count += 1
    print(f"  [respondent] 迁移 {count} 条记录")
    return count


def migrate_responses(og_cursor, my_cursor):
    """迁移答卷表: Response → response"""
    og_cursor.execute(
        'SELECT reid, questionnaire_id, respondent_id, start_time, end_time, is_completed FROM Response'
    )
    rows = og_cursor.fetchall()
    count = 0
    for row in rows:
        reid, qid, rid, start_time, end_time, completed = row
        my_cursor.execute(
            "INSERT INTO response (id, questionnaire_id, respondent_id, submitted_at) "
            "VALUES (%s, %s, %s, %s) "
            "ON DUPLICATE KEY UPDATE submitted_at=VALUES(submitted_at)",
            (reid, qid, rid, end_time or start_time)
        )
        count += 1
    print(f"  [response] 迁移 {count} 条记录")
    return count


def migrate_answers(og_cursor, my_cursor):
    """迁移答案表: Answer → answer"""
    og_cursor.execute(
        'SELECT answer_id, response_id, question_id, answer_text FROM Answer'
    )
    rows = og_cursor.fetchall()
    count = 0
    for row in rows:
        aid, resp_id, qid, text = row
        my_cursor.execute(
            "INSERT INTO answer (id, response_id, question_id, answer_text) "
            "VALUES (%s, %s, %s, %s) "
            "ON DUPLICATE KEY UPDATE answer_text=VALUES(answer_text)",
            (aid, resp_id, qid, text)
        )
        count += 1
    print(f"  [answer] 迁移 {count} 条记录")
    return count


def migrate_roles_and_permissions(my_cursor):
    """角色和权限已在 init.sql 中初始化，这里跳过"""
    print("  [role/permission] 已在 init.sql 中初始化，跳过")


def main():
    print("=" * 60)
    print("QDBMS 数据库迁移: openGauss → MySQL 8.0")
    print(f"开始时间: {datetime.now()}")
    print("=" * 60)

    print("\n连接 openGauss...")
    og_conn = get_og_conn()
    og_cursor = og_conn.cursor()

    print("连接 MySQL...")
    my_conn = get_my_conn()
    my_cursor = my_conn.cursor()

    # 禁用外键检查加速迁移
    my_cursor.execute("SET FOREIGN_KEY_CHECKS = 0")

    total = 0
    print("\n开始迁移...\n")
    total += migrate_users(og_cursor, my_cursor)
    total += migrate_questionnaires(og_cursor, my_cursor)
    total += migrate_questions(og_cursor, my_cursor)
    total += migrate_respondents(og_cursor, my_cursor)
    total += migrate_responses(og_cursor, my_cursor)
    total += migrate_answers(og_cursor, my_cursor)

    my_cursor.execute("SET FOREIGN_KEY_CHECKS = 1")
    my_conn.commit()

    print(f"\n迁移完成！共迁移 {total} 条记录")

    og_cursor.close()
    og_conn.close()
    my_cursor.close()
    my_conn.close()


if __name__ == "__main__":
    main()
