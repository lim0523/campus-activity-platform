# DataGrip ER 可视化指南

## 前提

本机需要已经安装并启动 MySQL。DataGrip 需要能连接到该 MySQL 实例。

## 操作步骤

1. 打开 DataGrip。
2. 在左侧 Database 面板点击 `+`。
3. 选择 `Data Source` -> `MySQL`。
4. 填写连接信息：
   - Host：`localhost`
   - Port：`3306`
   - User：你的 MySQL 用户名
   - Password：你的 MySQL 密码
5. 点击 `Test Connection`，确认连接成功。
6. 打开 SQL Console，依次执行：
   - `sql/01_create_database.sql`
   - `sql/02_create_tables.sql`
   - `sql/03_insert_sample_data.sql`
7. 在 Database 面板刷新数据源。
8. 展开 `campus_activity_db`。
9. 选中所有核心表。
10. 右键选择 `Diagrams` -> `Show Visualization` 或 `New Diagram`。
11. DataGrip 会根据外键关系自动生成 ER 图。

## 建议截图内容

课程报告中建议截图：

- 完整 ER 图。
- `activity`、`activity_registration`、`activity_checkin` 三张表的关系局部图。
- 典型查询结果截图。

## 注意点

- DataGrip 的 ER 图依赖真实外键。如果执行建表 SQL 时去掉外键，图中关系线会缺失。
- 如果看不到关系线，检查 `02_create_tables.sql` 是否完整执行成功。
- 如果表太密集，可以在图中手动拖拽布局，再截图放入报告。
