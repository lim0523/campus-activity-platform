# 数据库范围说明

## 数据库名称

`campus_activity_db`

## 核心实体

- `sys_role`：角色表
- `sys_user`：用户表
- `activity_category`：活动分类表
- `activity`：活动表
- `activity_registration`：活动报名表
- `activity_checkin`：活动签到表
- `activity_feedback`：活动反馈表
- `notification`：通知表

## 状态设计

### 活动状态 `activity.status`

- `draft`：草稿
- `published`：已发布
- `cancelled`：已取消
- `finished`：已结束

### 报名状态 `activity_registration.status`

- `pending`：待审核
- `approved`：报名成功
- `rejected`：已拒绝
- `cancelled`：已取消
- `attended`：已参加

### 签到结果 `activity_checkin.checkin_result`

- `normal`：正常签到
- `late`：迟到签到
- `manual`：人工补签

### 通知状态 `notification.read_status`

- `unread`：未读
- `read`：已读

## 设计取舍

- 保留 `sys_role`，便于展示角色关系，但不展开复杂权限模型。
- 活动报名和签到拆成两张表，避免签到数据直接污染报名记录。
- 活动反馈独立成表，便于统计评分和收集文字反馈。
- 通知表作为辅助实体，体现系统完整性，但不作为核心复杂功能。
- 不设计支付、聊天、图片文件表，避免范围过大。

## 推荐表数量

核心表 8 张，适合数据库课程作业：关系足够丰富，但不会超出 Spring Boot + Vue 的实现范围。
