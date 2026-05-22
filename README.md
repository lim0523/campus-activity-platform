# 校园活动平台

本项目是数据库课程作业，技术栈为 `Spring Boot + Vue + MySQL`。当前版本重点展示活动展示、在线报名、签到管理、活动指南、往期评价，以及基于角色的后台辅助管理能力。

## 目录结构

- `backend/`：Spring Boot 后端
- `frontend/`：Vue + Vite 前端
- `sql/`：数据库创建、初始化、统计查询和约束测试脚本
- `design/`：ER 模型和表关系说明
- `docs/`：需求说明、DataGrip 可视化说明、接口文档

## 数据库信息

- 数据库名：`campus_activity_db`
- 主机：`127.0.0.1`
- 端口：`3306`
- 用户名：通过环境变量 `DB_USERNAME` 配置，默认 `root`
- 密码：通过环境变量 `DB_PASSWORD` 配置

## 组员拉取后如何运行

### 1. 准备 MySQL 数据库

先创建数据库并导入脚本：

```bash
mysql -u root -p < sql/01_create_database.sql
mysql -u root -p campus_activity_db < sql/02_create_tables.sql
mysql -u root -p campus_activity_db < sql/03_insert_sample_data.sql
mysql -u root -p campus_activity_db < sql/05_views_indexes.sql
mysql -u root -p campus_activity_db < sql/07_upgrade_v2_auth_forum.sql
```

如果只想快速体验，以上脚本已经足够。

### 2. 启动后端

进入 `backend/` 目录，按你的本机数据库账号设置环境变量后启动：

```bash
export DB_USERNAME=root
export DB_PASSWORD=你的数据库密码
mvn spring-boot:run
```

默认后端地址：

```text
http://127.0.0.1:8080
```

### 3. 启动前端

进入 `frontend/` 目录执行：

```bash
npm install
npm run dev
```

默认前端地址：

```text
http://127.0.0.1:5173
```

前端通过 Vite 代理访问后端 `/api` 接口，因此需要保证后端已经启动。

## 后端启动

在 `/backend` 目录执行：

```bash
mvn spring-boot:run
```

默认端口：

```text
http://127.0.0.1:8080
```

可用接口示例：

- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/activities`
- `GET /api/activities/dashboard-summary`
- `GET /api/activities/statistics`
- `GET /api/forum/posts`
- `GET /api/archive/activities`

## 前端启动

在 `/frontend` 目录执行：

```bash
npm install
npm run dev
```

默认地址：

```text
http://127.0.0.1:5173
```

前端通过 Vite 代理访问后端 `/api` 接口，启动时需要保证后端已运行。

## 演示账号

- 学生：`stu001` / `123456`
- 组织者：`org002` / `123456`
- 管理员：`admin` / `123456`

## DataGrip 查看 ER 图

1. 连接本地 MySQL。
2. 打开数据库 `campus_activity_db`。
3. 选中核心表：
   - `sys_role`
   - `sys_user`
   - `activity_category`
   - `activity`
   - `activity_registration`
   - `activity_checkin`
   - `activity_feedback`
   - `notification`
   - `forum_post`
   - `forum_comment`
   - `forum_tag`
   - `forum_post_tag`
   - `forum_post_like`
4. 右键 `Diagrams` -> `Show Visualization`。

## 作业答辩建议

- 先讲需求边界，再讲 JWT 登录和角色权限
- 再讲 ER 关系、主外键和论坛扩展表
- 然后演示活动创建、报名、签到、论坛讨论、热度榜、往期活动
- 最后展示 DataGrip ER 图和 `sql/04_queries.sql` 中的典型 SQL
