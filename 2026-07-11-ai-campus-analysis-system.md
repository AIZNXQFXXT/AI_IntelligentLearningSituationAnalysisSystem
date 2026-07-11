# AI智能校园学情分析系统 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个C/S架构的AI智能校园学情分析系统，包含SpringBoot后端、JavaFX桌面客户端、PostgreSQL数据库、Redis缓存，集成DeepSeek大模型实现学情诊断/评语生成/风险预警。

**Architecture:** C/S双层架构——SpringBoot后端提供RESTful API，JavaFX桌面客户端通过HTTP调用后端。后端部署Windows，数据库(PostgreSQL)和缓存(Redis)运行在WSL子系统。多模块Maven项目：common-module(公共DTO/枚举/工具)、backend-module(SpringBoot后端)、client-module(JavaFX客户端)。

**Tech Stack:** JDK17 + SpringBoot3.2 + MyBatis-Plus3.5 + PostgreSQL14 + Redis7 + JavaFX21 + Maven3.8 + OkHttp4 + EasyExcel3 + DeepSeek API

## Global Constraints

- JDK 17+, Maven 3.8+, SpringBoot 3.2+
- PostgreSQL 14+ (WSL), Redis 7+ (WSL)
- 后端部署Windows，通过localhost连接WSL的PostgreSQL/Redis
- 所有表逻辑删除(`is_deleted`)，统一`created_at/updated_at`自动填充
- 统一返回格式：`ApiResponse{code, msg, data}`，分页返回`PageResult{records, total, page, size, pages}`
- 密码BCrypt加密，JWT Token(Access 2h + Refresh 7d)
- AI调用策略模式：DeepSeek + LocalMock降级，超时自动重试2次

---

## 阶段一：项目骨架 + 登录认证 + 公共基础设施

### Task 1.1: 创建Maven多模块项目骨架

**Files:**
- Create: `ai-campus-system/pom.xml` (父POM)
- Create: `ai-campus-system/common-module/pom.xml`
- Create: `ai-campus-system/backend-module/pom.xml`
- Create: `ai-campus-system/client-module/pom.xml`

**Interfaces:**
- Consumes: 无
- Produces: 三模块Maven结构，统一依赖版本管理

- [ ] **Step 1: 创建父POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.campus</groupId>
    <artifactId>ai-campus-system</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    <name>AI Campus Learning Analysis System</name>

    <modules>
        <module>common-module</module>
        <module>backend-module</module>
        <module>client-module</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring-boot.version>3.2.5</spring-boot.version>
        <mybatis-plus.version>3.5.6</mybatis-plus.version>
        <postgresql.version>42.7.3</postgresql.version>
        <jjwt.version>0.12.5</jjwt.version>
        <bcprov.version>1.78</bcprov.version>
        <easyexcel.version>3.3.4</easyexcel.version>
        <okhttp.version>4.12.0</okhttp.version>
        <gson.version>2.10.1</gson.version>
        <caffeine.version>3.1.8</caffeine.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <dependency>
                <groupId>org.postgresql</groupId>
                <artifactId>postgresql</artifactId>
                <version>${postgresql.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-impl</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-jackson</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>org.bouncycastle</groupId>
                <artifactId>bcprov-jdk18on</artifactId>
                <version>${bcprov.version}</version>
            </dependency>
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>easyexcel</artifactId>
                <version>${easyexcel.version}</version>
            </dependency>
            <dependency>
                <groupId>com.squareup.okhttp3</groupId>
                <artifactId>okhttp</artifactId>
                <version>${okhttp.version}</version>
            </dependency>
            <dependency>
                <groupId>com.google.code.gson</groupId>
                <artifactId>gson</artifactId>
                <version>${gson.version}</version>
            </dependency>
            <dependency>
                <groupId>com.github.ben-manes.caffeine</groupId>
                <artifactId>caffeine</artifactId>
                <version>${caffeine.version}</version>
            </dependency>
            <dependency>
                <groupId>com.campus</groupId>
                <artifactId>common-module</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

- [ ] **Step 2: 创建common-module POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.campus</groupId>
        <artifactId>ai-campus-system</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>common-module</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
        </dependency>
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 3: 创建backend-module POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.campus</groupId>
        <artifactId>ai-campus-system</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>backend-module</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.campus</groupId>
            <artifactId>common-module</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>easyexcel</artifactId>
        </dependency>
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <executions>
                    <execution>
                        <goals><goal>repackage</goal></goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 4: 创建client-module POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.campus</groupId>
        <artifactId>ai-campus-system</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>client-module</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.campus</groupId>
            <artifactId>common-module</artifactId>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>21</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>21</version>
        </dependency>
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>com.campus.client.CampusApp</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 5: 验证项目结构**

Run: `mvn clean install -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git init && git add -A && git commit -m "feat: initialize multi-module Maven project skeleton"
```

---

### Task 1.2: common-module 公共基础类

**Files:**
- Create: `common-module/src/main/java/com/campus/common/vo/ApiResponse.java`
- Create: `common-module/src/main/java/com/campus/common/vo/PageResult.java`
- Create: `common-module/src/main/java/com/campus/common/enums/RoleEnum.java`
- Create: `common-module/src/main/java/com/campus/common/enums/ErrorCode.java`
- Create: `common-module/src/main/java/com/campus/common/enums/RiskLevelEnum.java`
- Create: `common-module/src/main/java/com/campus/common/enums/CourseTypeEnum.java`
- Create: `common-module/src/main/java/com/campus/common/enums/ExamTypeEnum.java`
- Create: `common-module/src/main/java/com/campus/common/exception/BusinessException.java`
- Create: `common-module/src/main/java/com/campus/common/dto/LoginDTO.java`
- Create: `common-module/src/main/java/com/campus/common/validator/Create.java`
- Create: `common-module/src/main/java/com/campus/common/validator/Update.java`

**Interfaces:**
- Consumes: 无
- Produces: ApiResponse, PageResult, RoleEnum, ErrorCode, BusinessException等公共类

- [ ] **Step 1: 创建 ApiResponse.java**

```java
package com.campus.common.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(200, "success", null);
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<>(500, msg, null);
    }
}
```

- [ ] **Step 2: 创建 PageResult.java**

```java
package com.campus.common.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long page;
    private long size;
    private long pages;
}
```

- [ ] **Step 3: 创建枚举类**

```java
// RoleEnum.java
package com.campus.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    ADMIN("ADMIN", "管理员"),
    TEACHER("TEACHER", "教师"),
    STUDENT("STUDENT", "学生");

    private final String code;
    private final String desc;
}
```

```java
// ErrorCode.java
package com.campus.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    DUPLICATE(409, "数据重复"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    LOGIN_FAILED(1001, "账号或密码错误"),
    ACCOUNT_DISABLED(1002, "账号已禁用"),
    ACCOUNT_LOCKED(1003, "账号已锁定"),
    TOKEN_EXPIRED(1004, "Token已过期"),
    AI_CALL_FAILED(2001, "AI服务调用失败"),
    AI_RATE_LIMIT(2002, "AI调用超限"),
    SCORE_DUPLICATE(3001, "成绩重复录入"),
    SCORE_ARCHIVED(3002, "成绩已归档不可修改");

    private final int code;
    private final String msg;
}
```

```java
// RiskLevelEnum.java
package com.campus.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum RiskLevelEnum {
    HIGH("HIGH", "高风险"),
    MEDIUM("MEDIUM", "中风险"),
    LOW("LOW", "低风险");

    private final String code;
    private final String desc;
}
```

```java
// CourseTypeEnum.java
package com.campus.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum CourseTypeEnum {
    ELECTIVE("ELECTIVE", "选修"),
    REQUIRED("REQUIRED", "必修"),
    MAJOR("MAJOR", "专业");

    private final String code;
    private final String desc;
}
```

```java
// ExamTypeEnum.java
package com.campus.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum ExamTypeEnum {
    MOCK("MOCK", "月考"),
    MIDTERM("MIDTERM", "期中"),
    FINAL("FINAL", "期末"),
    RETEST("RETEST", "补考");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 4: 创建异常和校验类**

```java
// BusinessException.java
package com.campus.common.exception;

import com.campus.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

```java
// Create.java
package com.campus.common.validator;

public interface Create {}
```

```java
// Update.java
package com.campus.common.validator;

public interface Update {}
```

- [ ] **Step 5: Commit**

```bash
git add common-module/
git commit -m "feat(common): add common DTOs, enums, exceptions, validators"
```

---

### Task 1.3: 后端配置 + 数据库建表 + Entity/Mapper

**Files:**
- Create: `backend-module/src/main/resources/application.yml`
- Create: `backend-module/src/main/java/com/campus/backend/CampusApplication.java`
- Create: `backend-module/src/main/java/com/campus/backend/config/MyBatisPlusConfig.java`
- Create: `backend-module/src/main/java/com/campus/backend/entity/BaseEntity.java`
- Create: `backend-module/src/main/java/com/campus/backend/entity/` (19个实体类)
- Create: `backend-module/src/main/java/com/campus/backend/mapper/` (19个Mapper接口)
- Create: `backend-module/src/main/resources/db/migration/V1__init_schema.sql`

**Interfaces:**
- Consumes: common-module中的枚举、异常类
- Produces: 所有Entity和Mapper接口

- [ ] **Step 1: 创建 application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/campus_ai
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 2000ms

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0

campus:
  jwt:
    secret: your-256-bit-secret-key-for-jwt-token-signing-here
    access-token-expiration: 7200000    # 2小时
    refresh-token-expiration: 604800000 # 7天
  ai:
    deepseek-api-key: ${DEEPSEEK_API_KEY:}
    deepseek-base-url: https://api.deepseek.com
    daily-limit: 50
    timeout-connect: 5000
    timeout-read: 30000
```

- [ ] **Step 2: 创建 CampusApplication.java**

```java
package com.campus.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.campus.backend.mapper")
@EnableAsync
public class CampusApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 MyBatisPlusConfig.java**

```java
package com.campus.backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }
}
```

- [ ] **Step 4: 创建SQL建表脚本 (V1__init_schema.sql)**

```sql
-- 1. sys_user
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    avatar VARCHAR(255),
    phone VARCHAR(20),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 2. teacher
CREATE TABLE teacher (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    teacher_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    title VARCHAR(50),
    subject VARCHAR(50),
    department VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 3. student
CREATE TABLE student (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    student_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    class_id BIGINT NOT NULL,
    enroll_year VARCHAR(10),
    status SMALLINT NOT NULL DEFAULT 1,
    phone VARCHAR(20),
    guardian_phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_student_class_id ON student(class_id);
CREATE INDEX idx_student_name ON student(name);

-- 4. class_info
CREATE TABLE class_info (
    id BIGSERIAL PRIMARY KEY,
    grade VARCHAR(20) NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    head_teacher_id BIGINT,
    student_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 5. course
CREATE TABLE course (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    credit DECIMAL(3,1),
    description TEXT,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 6. exam
CREATE TABLE exam (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    class_id BIGINT NOT NULL,
    exam_date DATE,
    is_archived SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 7. score
CREATE TABLE score (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    exam_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    regular_score DECIMAL(5,1),
    exam_score DECIMAL(5,1),
    final_score DECIMAL(5,1) NOT NULL,
    rank_class INT,
    rank_grade INT,
    is_absent SMALLINT NOT NULL DEFAULT 0,
    is_cheat SMALLINT NOT NULL DEFAULT 0,
    audit_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    entered_by BIGINT,
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_score_student_exam ON score(student_id, exam_id);
CREATE INDEX idx_score_exam ON score(exam_id);

-- 8. score_correction
CREATE TABLE score_correction (
    id BIGSERIAL PRIMARY KEY,
    score_id BIGINT NOT NULL,
    old_final_score DECIMAL(5,1),
    new_final_score DECIMAL(5,1) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    operator_id BIGINT NOT NULL,
    operated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 9. teaching_task
CREATE TABLE teaching_task (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    UNIQUE(teacher_id, class_id, course_id, semester)
);

-- 10. ai_diagnosis_record
CREATE TABLE ai_diagnosis_record (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    diagnosis_text TEXT,
    strengths TEXT,
    weaknesses TEXT,
    trend_analysis TEXT,
    risk_level VARCHAR(20),
    tokens_used INT NOT NULL DEFAULT 0,
    cost DECIMAL(10,6),
    duration_ms INT NOT NULL DEFAULT 0,
    ai_model VARCHAR(50) NOT NULL,
    prompt_template VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 11. ai_comment
CREATE TABLE ai_comment (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    is_teacher_edited SMALLINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    generated_by VARCHAR(50) NOT NULL,
    tokens_used INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 12. ai_comment_version
CREATE TABLE ai_comment_version (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    content TEXT NOT NULL,
    source VARCHAR(20) NOT NULL,
    tokens_used INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 13. knowledge_point
CREATE TABLE knowledge_point (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    subject_type VARCHAR(50),
    level SMALLINT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 14. risk_warning
CREATE TABLE risk_warning (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    risk_reason TEXT,
    ai_analysis TEXT,
    handle_status VARCHAR(20) NOT NULL DEFAULT 'UNHANDLED',
    handler_id BIGINT,
    handle_remark TEXT,
    handle_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_risk_student_semester ON risk_warning(student_id, semester);

-- 15. sys_config
CREATE TABLE sys_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 16. sys_dict
CREATE TABLE sys_dict (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(50) NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    item_value VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    UNIQUE(type_code, item_code)
);

-- 17. operation_log
CREATE TABLE operation_log (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    operator_id BIGINT NOT NULL,
    operation VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    detail TEXT,
    old_data TEXT,
    new_data TEXT,
    ip VARCHAR(50),
    user_agent VARCHAR(500),
    duration_ms INT NOT NULL DEFAULT 0,
    result_status VARCHAR(20) NOT NULL,
    fail_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_oplog_operator ON operation_log(operator_id);
CREATE INDEX idx_oplog_created ON operation_log(created_at);

-- 18. ai_call_log
CREATE TABLE ai_call_log (
    id BIGSERIAL PRIMARY KEY,
    caller_id BIGINT NOT NULL,
    caller_role VARCHAR(20),
    function_name VARCHAR(50),
    ai_model VARCHAR(50),
    request_body TEXT,
    response_body TEXT,
    http_status INT,
    tokens_input INT,
    tokens_output INT,
    tokens_total INT,
    estimated_cost DECIMAL(10,6),
    duration_ms INT NOT NULL DEFAULT 0,
    success SMALLINT NOT NULL DEFAULT 1,
    error_message TEXT,
    prompt_template VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

-- 19. task_record
CREATE TABLE task_record (
    id BIGSERIAL PRIMARY KEY,
    task_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress INT NOT NULL DEFAULT 0,
    current_count INT NOT NULL DEFAULT 0,
    total_count INT NOT NULL DEFAULT 0,
    file_url VARCHAR(255),
    result_json TEXT,
    created_by BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted SMALLINT NOT NULL DEFAULT 0
);
```

- [ ] **Step 5: 创建BaseEntity**

```java
package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public abstract class BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 6: 创建User实体 (其他实体类似)**

```java
package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String role;
    private String avatar;
    private String phone;
    private Integer status;
}
```

- [ ] **Step 7: 创建Mapper接口**

```java
package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

- [ ] **Step 8: Commit**

```bash
git add backend-module/
git commit -m "feat(backend): add config, SQL schema, entities and mappers"
```

---

### Task 1.4: JWT认证 + 登录接口

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/security/JwtUtil.java`
- Create: `backend-module/src/main/java/com/campus/backend/security/JwtAuthInterceptor.java`
- Create: `backend-module/src/main/java/com/campus/backend/security/PasswordEncoder.java`
- Create: `backend-module/src/main/java/com/campus/backend/security/RoleAspect.java`
- Create: `backend-module/src/main/java/com/campus/backend/config/WebMvcConfig.java`
- Create: `backend-module/src/main/java/com/campus/backend/config/GlobalExceptionHandler.java`
- Create: `backend-module/src/main/java/com/campus/backend/service/AuthService.java`
- Create: `backend-module/src/main/java/com/campus/backend/service/impl/AuthServiceImpl.java`
- Create: `backend-module/src/main/java/com/campus/backend/controller/AuthController.java`

**Interfaces:**
- Consumes: UserMapper, User实体, JWT配置
- Produces: POST /api/auth/login, Token生成/校验

- [ ] **Step 1: 创建 JwtUtil.java**

```java
package com.campus.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${campus.jwt.secret}")
    private String secret;

    @Value("${campus.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${campus.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }
}
```

- [ ] **Step 2: 创建 JwtAuthInterceptor.java**

```java
package com.campus.backend.security;

import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        Claims claims = jwtUtil.parseToken(token);
        request.setAttribute("userId", claims.get("userId", Long.class));
        request.setAttribute("username", claims.getSubject());
        request.setAttribute("role", claims.get("role", String.class));

        return true;
    }
}
```

- [ ] **Step 3: 创建 RoleAspect.java**

```java
package com.campus.backend.security;

import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value();
}

@Aspect
@Component
public class RoleAspect {

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String role = (String) attrs.getRequest().getAttribute("role");

        for (String allowed : requireRole.value()) {
            if (allowed.equals(role)) {
                return joinPoint.proceed();
            }
        }
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }
}
```

- [ ] **Step 4: 创建 WebMvcConfig.java**

```java
package com.campus.backend.config;

import com.campus.backend.security.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowCredentials(true);
    }
}
```

- [ ] **Step 5: 创建 GlobalExceptionHandler.java**

```java
package com.campus.backend.config;

import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import com.campus.common.vo.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return ApiResponse.error(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), e.getMessage());
    }
}
```

- [ ] **Step 6: 创建 AuthService 和 AuthServiceImpl**

```java
// AuthService.java
package com.campus.backend.service;

import com.campus.common.dto.LoginDTO;
import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginDTO dto);
    Map<String, Object> refreshToken(String refreshToken);
}
```

```java
// AuthServiceImpl.java
package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.security.JwtUtil;
import com.campus.backend.service.AuthService;
import com.campus.common.dto.LoginDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (user.getStatus() == 2) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (!PasswordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());

        redisTemplate.opsForValue().set(
            "token:" + accessToken,
            user.getId().toString(),
            2, TimeUnit.HOURS
        );

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("role", user.getRole());
        result.put("username", user.getUsername());
        result.put("userId", user.getId());
        return result;
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        var claims = jwtUtil.parseToken(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(
            claims.get("userId", Long.class),
            claims.getSubject(),
            claims.get("role", String.class)
        );
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        return result;
    }
}
```

- [ ] **Step 7: 创建 AuthController.java**

```java
package com.campus.backend.controller;

import com.campus.backend.security.RequireRole;
import com.campus.backend.service.AuthService;
import com.campus.common.dto.LoginDTO;
import com.campus.common.vo.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return ApiResponse.ok(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(authService.refreshToken(body.get("refreshToken")));
    }

    @PatchMapping("/profile")
    @RequireRole({"ADMIN", "TEACHER", "STUDENT"})
    public ApiResponse<Void> updateProfile(@RequestBody Map<String, Object> body) {
        // TODO: 实现个人信息修改
        return ApiResponse.ok();
    }
}
```

- [ ] **Step 8: Commit**

```bash
git add backend-module/
git commit -m "feat(backend): implement JWT auth, login API, RBAC aspect"
```

---

## 阶段二：管理员CRUD模块

### Task 2.1: 班级管理 CRUD

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/controller/ClassController.java`
- Create: `backend-module/src/main/java/com/campus/backend/service/ClassService.java`
- Create: `backend-module/src/main/java/com/campus/backend/service/impl/ClassServiceImpl.java`
- Create: `common-module/src/main/java/com/campus/common/dto/ClassDTO.java`
- Modify: `backend-module/src/main/java/com/campus/backend/mapper/ClassMapper.java`

**Interfaces:**
- Consumes: ClassMapper, BaseEntity
- Produces: GET/POST/PUT/DELETE /api/classes

- [ ] **Step 1: 创建 ClassDTO.java**

```java
package com.campus.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClassDTO {
    private Long id;

    @NotBlank(message = "年级不能为空")
    private String grade;

    @NotBlank(message = "班级名不能为空")
    private String className;

    private Long headTeacherId;
}
```

- [ ] **Step 2: 创建 ClassMapper.java**

```java
package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.ClassInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ClassMapper extends BaseMapper<ClassInfo> {
    @Select("SELECT COUNT(*) FROM student WHERE class_id = #{classId} AND is_deleted = 0")
    int countStudents(Long classId);
}
```

- [ ] **Step 3: 创建 ClassServiceImpl.java**

```java
package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.service.ClassService;
import com.campus.common.dto.ClassDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import com.campus.common.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassMapper classMapper;

    @Override
    public PageResult<ClassInfo> pageList(int page, int size, String keyword) {
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ClassInfo::getClassName, keyword)
                   .or().like(ClassInfo::getGrade, keyword);
        }
        wrapper.orderByDesc(ClassInfo::getCreatedAt);
        Page<ClassInfo> result = classMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size, result.getPages());
    }

    @Override
    public void create(ClassDTO dto) {
        // 校验同年级班级名唯一
        long count = classMapper.selectCount(
            new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getGrade, dto.getGrade())
                .eq(ClassInfo::getClassName, dto.getClassName())
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        ClassInfo info = new ClassInfo();
        info.setGrade(dto.getGrade());
        info.setClassName(dto.getClassName());
        info.setHeadTeacherId(dto.getHeadTeacherId());
        info.setStudentCount(0);
        classMapper.insert(info);
    }

    @Override
    public void update(Long id, ClassDTO dto) {
        ClassInfo info = classMapper.selectById(id);
        if (info == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        info.setGrade(dto.getGrade());
        info.setClassName(dto.getClassName());
        info.setHeadTeacherId(dto.getHeadTeacherId());
        classMapper.updateById(info);
    }

    @Override
    public void delete(Long id) {
        int studentCount = classMapper.countStudents(id);
        if (studentCount > 0) {
            throw new BusinessException(500, "班级下有学生，无法删除");
        }
        classMapper.deleteById(id);
    }
}
```

- [ ] **Step 4: 创建 ClassController.java**

```java
package com.campus.backend.controller;

import com.campus.backend.security.RequireRole;
import com.campus.backend.service.ClassService;
import com.campus.backend.entity.ClassInfo;
import com.campus.common.dto.ClassDTO;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class ClassController {

    private final ClassService classService;

    @GetMapping
    public ApiResponse<PageResult<ClassInfo>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(classService.pageList(page, size, keyword));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody ClassDTO dto) {
        classService.create(dto);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody ClassDTO dto) {
        classService.update(id, dto);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        classService.delete(id);
        return ApiResponse.ok();
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add backend-module/ common-module/
git commit -m "feat(backend): add class CRUD API"
```

---

### Task 2.2-2.5: 教师/学生/课程/考试/教学任务 CRUD

*(按相同模式创建TeacherController, StudentController, CourseController, ExamController, TeachingTaskController，每个包含分页查询、新增、编辑、删除接口)*

**关键差异点:**
- **教师/学生**: 支持Excel批量导入(`/batch`异步)、账号启停(`/status`)
- **课程**: 含status启停、type分类
- **考试**: 仅管理员CRUD，含归档`is_archived`字段
- **教学任务**: 唯一索引`(teacher_id, class_id, course_id, semester)`

---

## 阶段三：成绩管理模块

### Task 3.1: 成绩单条录入 + 批量导入

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/controller/ScoreController.java`
- Create: `backend-module/src/main/java/com/campus/backend/service/ScoreService.java`
- Create: `backend-module/src/main/java/com/campus/backend/service/impl/ScoreServiceImpl.java`
- Create: `backend-module/src/main/java/com/campus/backend/entity/Score.java`
- Create: `backend-module/src/main/java/com/campus/backend/mapper/ScoreMapper.java`

**Interfaces:**
- Consumes: ScoreMapper, TeachingTaskMapper, ExamMapper
- Produces: POST /api/scores, POST /api/scores/batch

- [ ] **Step 1: 创建 ScoreServiceImpl 核心逻辑**

```java
@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private final ScoreMapper scoreMapper;
    private final TeachingTaskMapper teachingTaskMapper;

    @Override
    @Transactional
    public void saveSingle(ScoreDTO dto, Long operatorId) {
        // 1. 校验教师拥有该教学任务权限
        // 2. 校验分数0~100
        if (dto.getRegularScore() != null && (dto.getRegularScore() < 0 || dto.getRegularScore() > 100)) {
            throw new BusinessException("平时分超出范围");
        }
        if (dto.getExamScore() != null && (dto.getExamScore() < 0 || dto.getExamScore() > 100)) {
            throw new BusinessException("卷面分超出范围");
        }

        // 3. 查重：同一学生同一考试同一课程不可重复
        long count = scoreMapper.selectCount(
            new LambdaQueryWrapper<Score>()
                .eq(Score::getStudentId, dto.getStudentId())
                .eq(Score::getExamId, dto.getExamId())
                .eq(Score::getCourseId, dto.getCourseId())
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.SCORE_DUPLICATE);
        }

        // 4. 计算总分 (平时分40% + 卷面分60%)
        BigDecimal regular = dto.getRegularScore() != null ? dto.getRegularScore() : BigDecimal.ZERO;
        BigDecimal exam = dto.getExamScore() != null ? dto.getExamScore() : BigDecimal.ZERO;
        BigDecimal finalScore = regular.multiply(new BigDecimal("0.4"))
            .add(exam.multiply(new BigDecimal("0.6")));

        Score score = new Score();
        score.setStudentId(dto.getStudentId());
        score.setExamId(dto.getExamId());
        score.setCourseId(dto.getCourseId());
        score.setRegularScore(dto.getRegularScore());
        score.setExamScore(dto.getExamScore());
        score.setFinalScore(finalScore);
        score.setEnteredBy(operatorId);
        scoreMapper.insert(score);
    }
}
```

- [ ] **Step 2: 创建异步批量导入接口**

```java
// ScoreController.java
@PostMapping("/batch")
@RequireRole("TEACHER")
public ApiResponse<Map<String, String>> batchImport(
        @RequestParam("file") MultipartFile file,
        @RequestParam Long examId,
        @RequestParam Long classId) {
    String taskId = taskService.submitTask(TaskType.SCORE_IMPORT, examId, classId, file);
    return ApiResponse.ok(Map.of("taskId", taskId));
}
```

- [ ] **Step 3: Commit**

```bash
git add backend-module/
git commit -m "feat(backend): add score entry and batch import"
```

---

### Task 3.2: 成绩修改审计

**Files:**
- Modify: `backend-module/src/main/java/com/campus/backend/service/impl/ScoreServiceImpl.java`
- Create: `backend-module/src/main/java/com/campus/backend/entity/ScoreCorrection.java`

**关键逻辑:**
- 检查考试是否归档 → 归档则拦截
- 记录修改前分数 → 写入score_correction表
- 更新score表分数和reason字段

---

## 阶段四：AI智能分析模块

### Task 4.1: AI策略模式 + DeepSeek集成

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/ai/AiService.java`
- Create: `backend-module/src/main/java/com/campus/backend/ai/AiServiceFactory.java`
- Create: `backend-module/src/main/java/com/campus/backend/ai/DeepSeekProvider.java`
- Create: `backend-module/src/main/java/com/campus/backend/ai/LocalMockProvider.java`
- Create: `backend-module/src/main/java/com/campus/backend/ai/AiRateLimiter.java`
- Create: `common-module/src/main/java/com/campus/common/constant/PromptTemplate.java`

**Interfaces:**
- Consumes: OkHttp, Redis, ai_call_log表
- Produces: POST /api/diagnoses, POST /api/comments/batch

- [ ] **Step 1: 创建 AiService 接口**

```java
package com.campus.backend.ai;

public interface AiService {
    AiResult call(AiRequest request);
}
```

- [ ] **Step 2: 创建 DeepSeekProvider**

```java
@Service
@ConditionalOnProperty(name = "campus.ai.provider", havingValue = "deepseek", matchIfMissing = true)
public class DeepSeekProvider implements AiService {

    @Value("${campus.ai.deepseek-api-key}")
    private String apiKey;

    @Value("${campus.ai.deepseek-base-url}")
    private String baseUrl;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();

    @Override
    public AiResult call(AiRequest request) {
        JsonObject body = new JsonObject();
        body.addProperty("model", "deepseek-chat");
        body.addProperty("messages", request.getPrompt());

        Request httpRequest = new Request.Builder()
            .url(baseUrl + "/v1/chat/completions")
            .addHeader("Authorization", "Bearer " + apiKey)
            .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
            .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            // 解析响应，提取usage信息
            return AiResult.success(responseBody, tokensUsed, cost);
        }
    }
}
```

- [ ] **Step 3: 创建 AiServiceFactory (重试+降级)**

```java
@Service
public class AiServiceFactory {

    private final Map<String, AiService> providers;
    private final AiRateLimiter rateLimiter;
    private static final int MAX_RETRY = 2;

    public AiResult execute(AiRequest request, Long userId) {
        if (!rateLimiter.allowRequest(userId)) {
            throw new BusinessException(ErrorCode.AI_RATE_LIMIT);
        }

        for (int i = 0; i <= MAX_RETRY; i++) {
            try {
                return providers.get("deepseek").call(request);
            } catch (Exception e) {
                log.warn("AI调用失败，第{}次重试", i + 1);
            }
        }
        // 降级到Mock
        return providers.get("mock").call(request);
    }
}
```

- [ ] **Step 4: 创建 AiRateLimiter**

```java
@Service
public class AiRateLimiter {

    @Autowired private StringRedisTemplate redis;
    private static final int DAILY_LIMIT = 50;

    public boolean allowRequest(Long userId) {
        String key = "ai:rate:" + userId;
        long now = System.currentTimeMillis();
        long windowStart = now - 24 * 60 * 60 * 1000;

        Long count = redis.opsForZSet().count(key, windowStart, now);
        if (count != null && count >= DAILY_LIMIT) {
            return false;
        }
        redis.opsForZSet().add(key, String.valueOf(now), now);
        redis.expire(key, Duration.ofDays(1));
        return true;
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add backend-module/ common-module/
git commit -m "feat(backend): implement AI strategy pattern with DeepSeek + Mock fallback"
```

---

### Task 4.2: 学情诊断/评语/风险预警

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/service/DiagnosisService.java`
- Create: `backend-module/src/main/java/com/campus/backend/controller/DiagnosisController.java`
- Create: `backend-module/src/main/java/com/campus/backend/controller/CommentController.java`
- Create: `backend-module/src/main/java/com/campus/backend/controller/RiskWarningController.java`

**关键逻辑:**
- 诊断: 查询学生成绩 → 组装Prompt → 调用AI → 解析JSON → 存入ai_diagnosis_record
- 批量评语: 异步任务 → 循环学生 → 独立Prompt → 存入ai_comment + ai_comment_version
- 风险预警: SQL聚合筛选(多科不及格/连续下滑) → AI判定风险等级 → 存入risk_warning

---

## 阶段五：统计看板 + 报表导出 + 学生端

### Task 5.1: 成绩统计可视化

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/controller/StatsController.java`
- Create: `backend-module/src/main/java/com/campus/backend/service/StatsService.java`

**关键逻辑:**
- 班级单科统计: AVG/MIN/MAX/及格率/优秀率
- 分数分布: 按10分段统计人数
- 排名: 班级排名/年级排名
- 多学期趋势: 按学期聚合平均分

### Task 5.2: Excel报表导出

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/controller/ReportController.java`

**关键逻辑:**
- EasyExcel生成: 班级成绩表、评语汇总、风险清单
- 大数据量走异步任务

### Task 5.3: JavaFX客户端核心框架

**Files:**
- Create: `client-module/src/main/java/com/campus/client/CampusApp.java`
- Create: `client-module/src/main/java/com/campus/client/navigation/SceneManager.java`
- Create: `client-module/src/main/java/com/campus/client/state/SessionStore.java`
- Create: `client-module/src/main/java/com/campus/client/service/ApiClient.java`
- Create: `client-module/src/main/resources/fxml/login.fxml`
- Create: `client-module/src/main/java/com/campus/client/controller/LoginController.java`

**关键逻辑:**
- SceneManager: 维护FXML路由表，动态加载页面
- SessionStore: 单例保存当前用户/Token/角色
- ApiClient: OkHttp封装，自动添加JWT Header

---

## 阶段六：日志 + 异步任务 + 联调

### Task 6.1: AOP操作日志

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/aop/OperationLogAspect.java`

**关键逻辑:**
- @Around拦截所有Controller方法
- 记录操作人、操作类型、修改前后JSON、耗时、结果

### Task 6.2: 异步任务中心

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/service/TaskService.java`
- Create: `backend-module/src/main/java/com/campus/backend/controller/TaskController.java`
- Create: `backend-module/src/main/java/com/campus/backend/entity/TaskRecord.java`
- Create: `client-module/src/main/java/com/campus/client/state/TaskPollManager.java`

**关键逻辑:**
- 后端: ThreadPoolTaskExecutor异步执行，实时更新task_record进度
- 客户端: TaskPollManager定时轮询taskId，弹窗展示进度0~100%

### Task 6.3: 完整联调 + 测试

**验收标准:**
1. 管理员: 登录 → 创建班级/教师/学生 → 分配教学任务 → 查看全校学情
2. 教师: 登录 → 录入成绩 → AI诊断 → 批量评语 → 导出报表
3. 学生: 登录 → 查看成绩/诊断/评语/预警

---

## 开发优先级总结

| 优先级 | 任务 | 工作量 |
|--------|------|--------|
| P0 | Task 1.1-1.4 骨架+登录 | 3天 |
| P0 | Task 2.1-2.5 管理员CRUD | 5天 |
| P1 | Task 3.1-3.2 成绩管理 | 4天 |
| P1 | Task 4.1-4.2 AI分析 | 5天 |
| P2 | Task 5.1-5.3 统计+客户端 | 4天 |
| P2 | Task 6.1-6.3 日志+异步+联调 | 3天 |
| **总计** | | **~24天** |
