-- =====================================================
-- AI Campus - 学习分析系统 数据库初始化脚本
-- Database: ai_campus (PostgreSQL 15+)
-- =====================================================

-- 数据库创建
CREATE DATABASE ai_campus;

\c ai_campus;

-- =====================================================
-- 1. 系统用户 & 权限
-- =====================================================

CREATE TABLE sys_user (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL,
    avatar       VARCHAR(255),
    phone        VARCHAR(20),
    status       SMALLINT DEFAULT 1,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted   SMALLINT DEFAULT 0
);
COMMENT ON TABLE sys_user IS '系统用户';
COMMENT ON COLUMN sys_user.role IS 'ADMIN / TEACHER / STUDENT';
COMMENT ON COLUMN sys_user.status IS '1=启用 0=禁用';

-- 插入默认管理员（密码: 123456）
INSERT INTO sys_user (username, password, role, status)
VALUES ('admin', '$2a$10$FRwew0l4DX3kuZVHhsOOg.pEoWMdaO7KAYZEcZ2iPWn10HnRndWKa', 'ADMIN', 1);

-- =====================================================
-- 2. 班级信息
-- =====================================================

CREATE TABLE class_info (
    id              BIGSERIAL PRIMARY KEY,
    grade           VARCHAR(20)  NOT NULL,
    class_name      VARCHAR(50)  NOT NULL,
    head_teacher_id BIGINT,
    student_count   INTEGER DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT DEFAULT 0
);
COMMENT ON TABLE class_info IS '班级信息';

CREATE INDEX idx_class_info_grade ON class_info(grade);

-- =====================================================
-- 3. 教师信息
-- =====================================================

CREATE TABLE teacher (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE,
    teacher_no  VARCHAR(30) NOT NULL UNIQUE,
    name        VARCHAR(50) NOT NULL,
    title       VARCHAR(50),
    subject     VARCHAR(50),
    education   VARCHAR(20),
    department  VARCHAR(100),
    status      SMALLINT DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted  SMALLINT DEFAULT 0
);
COMMENT ON TABLE teacher IS '教师信息';
COMMENT ON COLUMN teacher.title IS '职称';
COMMENT ON COLUMN teacher.status IS '1=在职 0=离职';

CREATE INDEX idx_teacher_user_id ON teacher(user_id);
CREATE INDEX idx_teacher_department ON teacher(department);

-- =====================================================
-- 4. 学生信息
-- =====================================================

CREATE TABLE student (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE,
    student_no      VARCHAR(30) NOT NULL UNIQUE,
    name            VARCHAR(50) NOT NULL,
    gender          VARCHAR(6),
    class_id        BIGINT,
    enroll_year     VARCHAR(10),
    status          SMALLINT DEFAULT 1,
    phone           VARCHAR(20),
    guardian_phone  VARCHAR(20),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT DEFAULT 0
);
COMMENT ON TABLE student IS '学生信息';
COMMENT ON COLUMN student.status IS '1=在读 0=休学 2=退学';

CREATE INDEX idx_student_user_id ON student(user_id);
CREATE INDEX idx_student_class_id ON student(class_id);
CREATE INDEX idx_student_student_no ON student(student_no);

-- =====================================================
-- 5. 课程信息
-- =====================================================

CREATE TABLE course (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(20),
    credit      NUMERIC(4,1),
    description TEXT,
    status      SMALLINT DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted  SMALLINT DEFAULT 0
);
COMMENT ON TABLE course IS '课程信息';
COMMENT ON COLUMN course.type IS 'ELECTIVE / REQUIRED / MAJOR';
COMMENT ON COLUMN course.status IS '1=启用 0=停用';

CREATE INDEX idx_course_name ON course(name);

-- =====================================================
-- 6. 考试信息
-- =====================================================

CREATE TABLE exam (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(20),
    semester    VARCHAR(20),
    class_id    BIGINT,
    exam_date   DATE,
    is_archived SMALLINT DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted  SMALLINT DEFAULT 0
);
COMMENT ON TABLE exam IS '考试批次';
COMMENT ON COLUMN exam.type IS 'MOCK / MIDTERM / FINAL / RETEST';

CREATE INDEX idx_exam_class_id ON exam(class_id);
CREATE INDEX idx_exam_semester ON exam(semester);

-- =====================================================
-- 7. 教学任务（教师-班级-课程分配）
-- =====================================================

CREATE TABLE teaching_task (
    id          BIGSERIAL PRIMARY KEY,
    teacher_id  BIGINT NOT NULL,
    class_id    BIGINT NOT NULL,
    course_id   BIGINT NOT NULL,
    semester    VARCHAR(20),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted  SMALLINT DEFAULT 0
);
COMMENT ON TABLE teaching_task IS '教学任务（排课）';

CREATE INDEX idx_teaching_task_teacher ON teaching_task(teacher_id);
CREATE INDEX idx_teaching_task_class ON teaching_task(class_id);
CREATE INDEX idx_teaching_task_semester ON teaching_task(semester);
CREATE UNIQUE INDEX idx_teaching_task_unique ON teaching_task(teacher_id, class_id, course_id, semester);

-- =====================================================
-- 8. 成绩记录
-- =====================================================

CREATE TABLE score (
    id            BIGSERIAL PRIMARY KEY,
    student_id    BIGINT NOT NULL,
    exam_id       BIGINT NOT NULL,
    course_id     BIGINT NOT NULL,
    regular_score NUMERIC(5,2),
    exam_score    NUMERIC(5,2),
    final_score   NUMERIC(5,2),
    rank_class    INTEGER,
    rank_grade    INTEGER,
    is_absent     SMALLINT DEFAULT 0,
    is_cheat      SMALLINT DEFAULT 0,
    audit_status  VARCHAR(20) DEFAULT 'DRAFT',
    entered_by    BIGINT,
    reason        TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted    SMALLINT DEFAULT 0
);
COMMENT ON TABLE score IS '成绩记录';
COMMENT ON COLUMN score.audit_status IS 'DRAFT / SUBMITTED / ARCHIVED';

CREATE INDEX idx_score_student ON score(student_id);
CREATE INDEX idx_score_exam ON score(exam_id);
CREATE INDEX idx_score_course ON score(course_id);
CREATE UNIQUE INDEX idx_score_unique ON score(student_id, exam_id, course_id);

-- =====================================================
-- 9. 成绩更正审核
-- =====================================================

CREATE TABLE score_correction (
    id              BIGSERIAL PRIMARY KEY,
    score_id        BIGINT NOT NULL,
    old_final_score NUMERIC(5,2),
    new_final_score NUMERIC(5,2),
    reason          TEXT,
    operator_id     BIGINT,
    operated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT DEFAULT 0
);
COMMENT ON TABLE score_correction IS '成绩更正记录';

CREATE INDEX idx_score_correction_score ON score_correction(score_id);

-- =====================================================
-- 10. 知识点树
-- =====================================================

CREATE TABLE knowledge_point (
    id           BIGSERIAL PRIMARY KEY,
    parent_id    BIGINT,
    name         VARCHAR(100) NOT NULL,
    subject_type VARCHAR(50),
    level        INTEGER DEFAULT 1,
    sort_order   INTEGER DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted   SMALLINT DEFAULT 0
);
COMMENT ON TABLE knowledge_point IS '知识点树结构';

CREATE INDEX idx_kp_parent ON knowledge_point(parent_id);

-- =====================================================
-- 11. 学业风险预警
-- =====================================================

CREATE TABLE risk_warning (
    id            BIGSERIAL PRIMARY KEY,
    student_id    BIGINT NOT NULL,
    semester      VARCHAR(20),
    risk_level    VARCHAR(20) NOT NULL,
    risk_reason   TEXT,
    ai_analysis   TEXT,
    handle_status VARCHAR(20) DEFAULT 'PENDING',
    handler_id    BIGINT,
    handle_remark TEXT,
    handle_at     TIMESTAMP,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted    SMALLINT DEFAULT 0
);
COMMENT ON TABLE risk_warning IS '学业风险预警';
COMMENT ON COLUMN risk_warning.risk_level IS 'LOW / MEDIUM / HIGH / CRITICAL';
COMMENT ON COLUMN risk_warning.handle_status IS 'PENDING / PROCESSING / HANDLED / IGNORED';

CREATE INDEX idx_rw_student ON risk_warning(student_id);
CREATE INDEX idx_rw_semester ON risk_warning(semester);
CREATE INDEX idx_rw_risk_level ON risk_warning(risk_level);
CREATE INDEX idx_rw_status ON risk_warning(handle_status);

-- =====================================================
-- 12. AI 评语
-- =====================================================

CREATE TABLE ai_comment (
    id                BIGSERIAL PRIMARY KEY,
    student_id        BIGINT NOT NULL,
    teacher_id        BIGINT,
    semester          VARCHAR(20),
    content           TEXT NOT NULL,
    is_teacher_edited SMALLINT DEFAULT 0,
    status            VARCHAR(20) DEFAULT 'DRAFT',
    generated_by      VARCHAR(50),
    tokens_used       INTEGER DEFAULT 0,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted        SMALLINT DEFAULT 0
);
COMMENT ON TABLE ai_comment IS 'AI 生成评语';

CREATE INDEX idx_ai_comment_student ON ai_comment(student_id);
CREATE INDEX idx_ai_comment_semester ON ai_comment(semester);

-- =====================================================
-- 13. AI 评语版本历史
-- =====================================================

CREATE TABLE ai_comment_version (
    id          BIGSERIAL PRIMARY KEY,
    comment_id  BIGINT NOT NULL,
    version_no  INTEGER NOT NULL,
    content     TEXT NOT NULL,
    source      VARCHAR(50),
    tokens_used INTEGER DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted  SMALLINT DEFAULT 0
);
COMMENT ON TABLE ai_comment_version IS '评语版本历史';

CREATE INDEX idx_acv_comment ON ai_comment_version(comment_id);

-- =====================================================
-- 14. AI 诊断报告
-- =====================================================

CREATE TABLE ai_diagnosis_record (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT NOT NULL,
    semester        VARCHAR(20),
    diagnosis_text  TEXT NOT NULL,
    strengths       TEXT,
    weaknesses      TEXT,
    trend_analysis  TEXT,
    risk_level      VARCHAR(20),
    tokens_used     INTEGER DEFAULT 0,
    cost            NUMERIC(10,6),
    duration_ms     INTEGER,
    ai_model        VARCHAR(50),
    prompt_template VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT DEFAULT 0
);
COMMENT ON TABLE ai_diagnosis_record IS 'AI 诊断报告';

CREATE INDEX idx_adr_student ON ai_diagnosis_record(student_id);
CREATE INDEX idx_adr_semester ON ai_diagnosis_record(semester);

-- =====================================================
-- 15. AI 调用日志
-- =====================================================

CREATE TABLE ai_call_log (
    id              BIGSERIAL PRIMARY KEY,
    caller_id       BIGINT,
    caller_role     VARCHAR(20),
    function_name   VARCHAR(100) NOT NULL,
    ai_model        VARCHAR(50),
    request_body    TEXT,
    response_body   TEXT,
    http_status     INTEGER,
    tokens_input    INTEGER DEFAULT 0,
    tokens_output   INTEGER DEFAULT 0,
    tokens_total    INTEGER DEFAULT 0,
    estimated_cost  NUMERIC(10,6),
    duration_ms     INTEGER,
    success         SMALLINT DEFAULT 1,
    error_message   TEXT,
    prompt_template VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted      SMALLINT DEFAULT 0
);
COMMENT ON TABLE ai_call_log IS 'AI API 调用监控日志';

CREATE INDEX idx_aicall_function ON ai_call_log(function_name);
CREATE INDEX idx_aicall_model ON ai_call_log(ai_model);
CREATE INDEX idx_aicall_success ON ai_call_log(success);
CREATE INDEX idx_aicall_created ON ai_call_log(created_at);

-- =====================================================
-- 16. 异步任务记录
-- =====================================================

CREATE TABLE task_record (
    id             BIGSERIAL PRIMARY KEY,
    task_type      VARCHAR(50) NOT NULL,
    status         VARCHAR(20) DEFAULT 'PENDING',
    progress       INTEGER DEFAULT 0,
    current_count  INTEGER DEFAULT 0,
    total_count    INTEGER DEFAULT 0,
    file_url       VARCHAR(500),
    result_json    TEXT,
    error_message  TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted     SMALLINT DEFAULT 0
);
COMMENT ON TABLE task_record IS '异步任务（导入/导出/批量AI）';
COMMENT ON COLUMN task_record.task_type IS 'SCORE_IMPORT / AI_COMMENT_BATCH / EXPORT';
COMMENT ON COLUMN task_record.status IS 'PENDING -> PROCESSING -> COMPLETED -> FAILED';

CREATE INDEX idx_task_type ON task_record(task_type);
CREATE INDEX idx_task_status ON task_record(status);

-- =====================================================
-- 17. 系统配置
-- =====================================================

CREATE TABLE sys_config (
    id           BIGSERIAL PRIMARY KEY,
    config_key   VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description  VARCHAR(255),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted   SMALLINT DEFAULT 0
);
COMMENT ON TABLE sys_config IS '系统配置键值对';

-- =====================================================
-- 18. 数据字典
-- =====================================================

CREATE TABLE sys_dict (
    id         BIGSERIAL PRIMARY KEY,
    type_code  VARCHAR(50) NOT NULL,
    item_code  VARCHAR(50) NOT NULL,
    item_value VARCHAR(100) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    status     SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT DEFAULT 0
);
COMMENT ON TABLE sys_dict IS '数据字典';

CREATE UNIQUE INDEX idx_sys_dict_unique ON sys_dict(type_code, item_code);
CREATE INDEX idx_sys_dict_type ON sys_dict(type_code);

-- =====================================================
-- 19. 操作日志
-- =====================================================

CREATE TABLE operation_log (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(50),
    operator_id  BIGINT,
    operation    VARCHAR(50) NOT NULL,
    target_type  VARCHAR(50),
    target_id    BIGINT,
    detail       TEXT,
    old_data     TEXT,
    new_data     TEXT,
    ip           VARCHAR(50),
    user_agent   VARCHAR(500),
    duration_ms  BIGINT,
    result_status VARCHAR(20),
    fail_reason  TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted   SMALLINT DEFAULT 0
);
COMMENT ON TABLE operation_log IS '操作审计日志';

CREATE INDEX idx_oplog_operator ON operation_log(operator_id);
CREATE INDEX idx_oplog_created ON operation_log(created_at);

-- =====================================================
-- 外键约束
-- =====================================================

-- 学生 & 教师 -> 系统用户
ALTER TABLE student ADD CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES sys_user(id);
ALTER TABLE teacher ADD CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES sys_user(id);

-- 学生 -> 班级
ALTER TABLE student ADD CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class_info(id);

-- 班级 -> 班主任（教师）
ALTER TABLE class_info ADD CONSTRAINT fk_class_head_teacher FOREIGN KEY (head_teacher_id) REFERENCES teacher(id);

-- 考试 -> 班级
ALTER TABLE exam ADD CONSTRAINT fk_exam_class FOREIGN KEY (class_id) REFERENCES class_info(id);

-- 教学任务
ALTER TABLE teaching_task ADD CONSTRAINT fk_tt_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id);
ALTER TABLE teaching_task ADD CONSTRAINT fk_tt_class FOREIGN KEY (class_id) REFERENCES class_info(id);
ALTER TABLE teaching_task ADD CONSTRAINT fk_tt_course FOREIGN KEY (course_id) REFERENCES course(id);

-- 成绩
ALTER TABLE score ADD CONSTRAINT fk_score_student FOREIGN KEY (student_id) REFERENCES student(id);
ALTER TABLE score ADD CONSTRAINT fk_score_exam FOREIGN KEY (exam_id) REFERENCES exam(id);
ALTER TABLE score ADD CONSTRAINT fk_score_course FOREIGN KEY (course_id) REFERENCES course(id);
ALTER TABLE score ADD CONSTRAINT fk_score_entered_by FOREIGN KEY (entered_by) REFERENCES sys_user(id);

-- 成绩更正
ALTER TABLE score_correction ADD CONSTRAINT fk_sc_score FOREIGN KEY (score_id) REFERENCES score(id);
ALTER TABLE score_correction ADD CONSTRAINT fk_sc_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id);

-- 知识点树自引用
ALTER TABLE knowledge_point ADD CONSTRAINT fk_kp_parent FOREIGN KEY (parent_id) REFERENCES knowledge_point(id);

-- 风险预警
ALTER TABLE risk_warning ADD CONSTRAINT fk_rw_student FOREIGN KEY (student_id) REFERENCES student(id);
ALTER TABLE risk_warning ADD CONSTRAINT fk_rw_handler FOREIGN KEY (handler_id) REFERENCES sys_user(id);

-- AI 评语
ALTER TABLE ai_comment ADD CONSTRAINT fk_ac_student FOREIGN KEY (student_id) REFERENCES student(id);
ALTER TABLE ai_comment ADD CONSTRAINT fk_ac_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id);

-- AI 评语版本
ALTER TABLE ai_comment_version ADD CONSTRAINT fk_acv_comment FOREIGN KEY (comment_id) REFERENCES ai_comment(id);

-- AI 诊断报告
ALTER TABLE ai_diagnosis_record ADD CONSTRAINT fk_adr_student FOREIGN KEY (student_id) REFERENCES student(id);

-- =====================================================
-- 种子数据：数据字典初始值
-- =====================================================

INSERT INTO sys_dict (type_code, item_code, item_value, sort_order) VALUES
    ('course_type', 'REQUIRED',  '必修',  1),
    ('course_type', 'ELECTIVE',  '选修',  2),
    ('course_type', 'MAJOR',     '专业',  3),
    ('exam_type',   'MOCK',      '模拟',  1),
    ('exam_type',   'MIDTERM',   '期中',  2),
    ('exam_type',   'FINAL',     '期末',  3),
    ('exam_type',   'RETEST',    '补考',  4),
    ('risk_level',  'LOW',       '低风险', 1),
    ('risk_level',  'MEDIUM',    '中风险', 2),
    ('risk_level',  'HIGH',      '高风险', 3),
    ('risk_level',  'CRITICAL',  '严重',   4),
    ('audit_status','DRAFT',     '草稿',   1),
    ('audit_status','SUBMITTED', '已提交', 2),
    ('audit_status','ARCHIVED',  '已归档', 3);
