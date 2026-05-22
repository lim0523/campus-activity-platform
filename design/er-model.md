# ER 模型设计

## 实体列表

| 实体 | 表名 | 说明 |
| --- | --- | --- |
| 角色 | `sys_role` | 存储学生、组织者、管理员等角色 |
| 用户 | `sys_user` | 存储登录用户和基础资料 |
| 活动分类 | `activity_category` | 存储讲座、比赛、社团、志愿服务等分类 |
| 活动 | `activity` | 存储活动基础信息、时间、地点、名额、状态 |
| 活动报名 | `activity_registration` | 记录用户对活动的报名状态 |
| 活动签到 | `activity_checkin` | 记录报名用户的签到时间和签到结果 |
| 活动反馈 | `activity_feedback` | 记录活动结束后的评分和评价 |
| 通知 | `notification` | 记录面向用户的系统通知 |
| 论坛帖子 | `forum_post` | 记录围绕活动展开的讨论帖 |
| 论坛评论 | `forum_comment` | 记录帖子下的评论互动 |
| 论坛标签 | `forum_tag` | 记录讨论标签 |
| 帖子标签关系 | `forum_post_tag` | 记录帖子与标签的多对多关系 |
| 帖子点赞 | `forum_post_like` | 记录用户对帖子点赞 |

## ER 关系

```mermaid
erDiagram
    sys_role ||--o{ sys_user : has
    sys_user ||--o{ activity : creates
    activity_category ||--o{ activity : classifies
    sys_user ||--o{ activity_registration : registers
    activity ||--o{ activity_registration : receives
    activity_registration ||--o| activity_checkin : generates
    sys_user ||--o{ activity_feedback : writes
    activity ||--o{ activity_feedback : receives
    sys_user ||--o{ notification : receives
    activity ||--o{ forum_post : hosts
    sys_user ||--o{ forum_post : writes
    forum_post ||--o{ forum_comment : has
    sys_user ||--o{ forum_comment : writes
    forum_post ||--o{ forum_post_tag : uses
    forum_tag ||--o{ forum_post_tag : classifies
    forum_post ||--o{ forum_post_like : receives
    sys_user ||--o{ forum_post_like : gives

    sys_role {
        bigint role_id PK
        varchar role_code UK
        varchar role_name
    }

    sys_user {
        bigint user_id PK
        bigint role_id FK
        varchar username UK
        varchar password_hash
        varchar real_name
        varchar student_no UK
        varchar phone
        varchar email
        varchar status
    }

    activity_category {
        bigint category_id PK
        varchar category_name UK
        varchar description
        varchar status
    }

    activity {
        bigint activity_id PK
        bigint category_id FK
        bigint organizer_id FK
        varchar title
        varchar location
        datetime start_time
        datetime end_time
        int capacity
        varchar status
    }

    activity_registration {
        bigint registration_id PK
        bigint activity_id FK
        bigint user_id FK
        varchar status
        datetime registered_at
    }

    activity_checkin {
        bigint checkin_id PK
        bigint registration_id FK
        datetime checkin_time
        varchar checkin_result
    }

    activity_feedback {
        bigint feedback_id PK
        bigint activity_id FK
        bigint user_id FK
        tinyint rating
        varchar content
    }

    notification {
        bigint notification_id PK
        bigint user_id FK
        varchar title
        varchar content
        varchar read_status
    }

    forum_post {
        bigint post_id PK
        bigint activity_id FK
        bigint author_id FK
        varchar title
        varchar scope
        varchar status
    }

    forum_comment {
        bigint comment_id PK
        bigint post_id FK
        bigint author_id FK
        varchar content
    }

    forum_tag {
        bigint tag_id PK
        varchar tag_name UK
        varchar color_hex
        varchar status
    }

    forum_post_tag {
        bigint post_tag_id PK
        bigint post_id FK
        bigint tag_id FK
    }

    forum_post_like {
        bigint like_id PK
        bigint post_id FK
        bigint user_id FK
    }
```

## 关系说明

- 一个角色可以对应多个用户。
- 一个用户可以创建多个活动。
- 一个活动分类可以包含多个活动。
- 一个用户可以报名多个活动。
- 一个活动可以拥有多条报名记录。
- 一条报名记录最多对应一条签到记录。
- 一个用户可以对多个活动提交反馈。
- 一个活动可以收到多条反馈。
- 一个用户可以收到多条通知。
- 一个活动可以拥有多条论坛帖子。
- 一个帖子可以收到多条评论和点赞。
- 一个帖子可以关联多个标签，一个标签也可以被多个帖子复用。
- 已结束活动的帖子和反馈共同构成“往期活动”展示区。
