# QDBMS — 问卷数据库管理系统

基于 Spring Boot + Vue 3 的全栈问卷管理系统，支持问卷创建、分发、数据收集、统计分析和数据导出。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Element Plus + ECharts + Vite | Vue 3.4 / Vite 5 |
| 后端 | Spring Boot + MyBatis Plus + Spring Security + JWT | Spring Boot 3.2.5 |
| 数据库 | MySQL（生产）/ H2（本地开发） | MySQL 8.0 / H2 |
| API 文档 | Knife4j (Swagger) | 4.4 |
| 部署 | Docker Compose / Nginx | — |

## 功能一览

- **用户管理**：注册、登录、JWT 认证、角色权限（RBAC）
- **问卷管理**：创建、编辑、发布、关闭问卷，支持单选/多选/文本题
- **数据采集**：受访者管理，答卷收集
- **数据分析**：可视化统计图表（ECharts）
- **数据导出**：Excel 导出、文本分析
- **API 文档**：Knife4j 在线接口文档

## 环境要求

### 本地开发
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0（可选，默认使用 H2 内嵌数据库）

### Docker 部署
- Docker 20+
- Docker Compose 2+

## 快速开始

### 方式一：本地开发（H2 内嵌数据库，无需安装 MySQL）

```bash
# 1. 克隆项目
git clone <repo-url>
cd QDBMS

# 2. 构建前端
cd qdbms-web
npm install
npm run build
cd ..

# 3. 启动后端（默认 H2 模式，自动建表）
cd qdbms-server
mvn spring-boot:run
```

浏览器打开 http://localhost:8080

### 方式二：Docker Compose 一键部署（MySQL + 后端 + Nginx）

```bash
# 1. 复制环境变量配置（可选）
cp .env.example .env

# 2. 启动所有服务
docker-compose up -d

# 3. 等待 MySQL 健康检查通过（约 30 秒），然后访问
# http://localhost
```

### 方式三：本地 EXE 打包（Windows）

```powershell
# 需要 JDK 17+ (含 jpackage) 和 Node.js
.\build-local.ps1     # 构建前端 + JAR
.\package-exe.ps1     # 打包成 Windows EXE
# 双击 package/QDBMS.exe 运行
```

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

## 项目结构

```
QDBMS/
├── qdbms-server/             # Spring Boot 后端
│   ├── src/main/java/com/qdbms/
│   │   ├── config/           # Spring Security、CORS、MyBatis Plus 配置
│   │   ├── controller/       # REST API 控制器
│   │   ├── dto/              # 数据传输对象
│   │   ├── entity/           # 数据库实体
│   │   ├── mapper/           # MyBatis Plus Mapper
│   │   ├── security/         # JWT 过滤器、Token 提供器
│   │   └── service/          # 业务逻辑层
│   ├── src/main/resources/
│   │   ├── db/               # 数据库初始化 SQL
│   │   ├── application.yml          # 主配置
│   │   └── application-local.yml    # 本地 H2 配置
│   ├── Dockerfile
│   └── pom.xml
├── qdbms-web/                # Vue 3 前端
│   ├── src/
│   │   ├── views/            # 页面组件
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # Pinia 状态管理
│   │   └── api/              # Axios API 封装
│   ├── nginx.conf            # Nginx 配置
│   ├── Dockerfile
│   └── package.json
├── src/                      # [遗留] 原 JavaFX 桌面版代码，不再维护
├── docker-compose.yml        # Docker 编排文件
├── build-local.ps1           # 本地构建脚本
├── package-exe.ps1           # EXE 打包脚本
├── .env.example              # 环境变量模板
└── README.md
```

## API 文档

启动后访问：
- Knife4j 接口文档：http://localhost:8080/swagger-ui.html (本地模式)
- Docker 部署时通过 Nginx 反向代理访问

## 配置说明

### 切换数据库

默认使用 **H2 内嵌数据库**（无需安装 MySQL），适合本地开发和 EXE 打包。

切换为 MySQL：

```bash
# 方式 1：启动时指定 profile
mvn spring-boot:run -Dspring-boot.run.profiles=mysql

# 方式 2：修改 application.yml 中 spring.profiles.active 为 mysql
```

### Docker 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MYSQL_ROOT_PASSWORD` | 123456 | MySQL root 密码 |
| `JWT_SECRET` | (内置默认值) | JWT 签名密钥，生产环境务必修改 |

## 数据库表结构

10 张核心表：`user`、`role`、`permission`、`user_role`、`role_permission`、`questionnaire`、`question`、`respondent`、`response`、`answer`

## 注意事项

1. **生产环境**请务必修改默认密码和 JWT 密钥
2. `.env` 文件包含敏感信息，已加入 `.gitignore`，请勿提交
3. `src/` 目录为原 JavaFX 桌面版遗留代码，仅供参考，不再维护
