# API 设计

## 设计目标

当前后端已经扩展到“活动主系统 + 轻社区模块”，重点覆盖 JWT 登录、角色权限、活动主链路、论坛讨论、热度榜和往期活动归档。

## 登录与权限

### 0.1 用户登录

- 方法：`POST`
- 路径：`/api/auth/login`
- 说明：返回 JWT 和当前用户信息

### 0.2 获取当前用户

- 方法：`GET`
- 路径：`/api/auth/me`
- 说明：恢复登录态并控制前端导航权限

## 接口列表

### 1. 查询活动列表

- 方法：`GET`
- 路径：`/api/activities`
- 说明：返回活动、分类、组织者、名额、当前报名人数和活动状态，供首页、管理页、签到页共享

### 2. 查询某活动报名名单

- 方法：`GET`
- 路径：`/api/activities/{activityId}/registrations`
- 说明：返回报名用户、学号、报名状态和报名时间

### 2.1 查询某活动详情

- 方法：`GET`
- 路径：`/api/activities/{activityId}`
- 说明：返回活动完整信息，供详情页和管理页使用

### 2.2 查询某活动签到名单

- 方法：`GET`
- 路径：`/api/activities/{activityId}/checkins`
- 说明：返回签到记录、签到结果、签到时间

### 3. 查询活动统计

- 方法：`GET`
- 路径：`/api/activities/statistics`
- 说明：返回每个活动的报名人数、签到人数、签到率

### 3.1 查询首页汇总指标

- 方法：`GET`
- 路径：`/api/activities/dashboard-summary`
- 说明：返回活动总数、报名总人次、签到总人次、已结束活动数和平均反馈分

### 4. 查询某用户活动参与记录

- 方法：`GET`
- 路径：`/api/users/{userId}/activities`
- 说明：返回用户参与活动记录、报名状态、签到结果

### 5. 查询分类下拉选项

- 方法：`GET`
- 路径：`/api/categories`
- 说明：返回启用中的活动分类

### 6. 查询组织者下拉选项

- 方法：`GET`
- 路径：`/api/users/organizers`
- 说明：返回可以创建活动的组织者和管理员

### 7. 查询启用用户列表

- 方法：`GET`
- 路径：`/api/users`
- 说明：返回可报名、可签到的用户列表

## 已补充的写接口

- `POST /api/activities`：创建活动
- `PUT /api/activities/{activityId}`：编辑活动
- `POST /api/activities/{activityId}/registrations`：报名活动
- `POST /api/activities/{activityId}/checkins`：活动签到
- `POST /api/activities/{activityId}/feedbacks`：提交反馈
- `POST /api/forum/posts`：发布讨论帖
- `POST /api/forum/posts/{postId}/comments`：发表评论
- `POST /api/forum/posts/{postId}/likes`：点赞或取消点赞

## 已补充的读取接口

- `GET /api/activities/{activityId}/feedbacks`：反馈列表
- `GET /api/activities/dashboard-summary`：首页汇总卡片
- `GET /api/forum/tags`：论坛标签
- `GET /api/forum/posts`：论坛帖子列表
- `GET /api/forum/hot`：论坛热度榜
- `GET /api/forum/posts/{postId}/comments`：帖子评论
- `GET /api/archive/activities`：往期活动归档

## 后续可扩展接口

- `DELETE /api/activities/{activityId}`：逻辑取消活动
- `GET /api/dashboard/summary`：首页汇总指标

## 开发顺序建议

1. 先跑通登录、活动列表和当前用户接口。
2. 再补活动管理、报名和签到。
3. 最后补论坛、热度榜和往期活动页面。

## 当前演示重点

- 数据库设计重点扩展为两条主线：
  - `activity`、`activity_registration`、`activity_checkin`、`activity_feedback`
  - `forum_post`、`forum_comment`、`forum_post_tag`、`forum_post_like`
- 权限重点是：
  - JWT 登录
  - `student / organizer / admin` 角色区分
  - 活动发起者与管理员的资源级操作权限
