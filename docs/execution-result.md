# 执行结果记录

## 执行时间

2026-05-20

## MySQL 连接

- Host：`127.0.0.1`
- Port：`3306`
- User：`root`
- Database：`campus_activity_db`

## 已执行 SQL

已按顺序执行：

```text
sql/01_create_database.sql
sql/02_create_tables.sql
sql/03_insert_sample_data.sql
sql/05_views_indexes.sql
sql/04_queries.sql
```

## 创建结果

基础表：

```text
activity
activity_category
activity_checkin
activity_feedback
activity_registration
notification
sys_role
sys_user
```

视图：

```text
v_activity_statistics
v_user_activity_history
```

## 示例数据量

```text
activity: 4
activity_category: 4
activity_checkin: 3
activity_feedback: 3
activity_registration: 6
notification: 3
sys_role: 3
sys_user: 6
```

## 查询验证

`sql/04_queries.sql` 已执行通过，包含：

- 可报名活动查询
- 活动报名名单查询
- 活动签到名单查询
- 学生参与记录查询
- 活动报名数、签到数、签到率统计
- 分类活动数量和报名数量统计
- 热门活动排行
- 活动平均评分
- 组织者活动统计

## DataGrip 可视化状态

数据库已经导入本地 MySQL。DataGrip 中连接该 MySQL 后，刷新 `campus_activity_db`，选中 8 张基础表，使用 `Diagrams` -> `Show Visualization` 或 `New Diagram` 即可生成 ER 图。

本次未能直接代操作 DataGrip UI，因为系统屏幕捕捉返回错误，无法稳定读取 DataGrip 窗口状态。
