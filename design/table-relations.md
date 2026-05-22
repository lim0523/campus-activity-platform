# 表关系说明

## 主关系

| 父表 | 子表 | 关系 | 外键 |
| --- | --- | --- | --- |
| `sys_role` | `sys_user` | 1:N | `sys_user.role_id` |
| `sys_user` | `activity` | 1:N | `activity.organizer_id` |
| `activity_category` | `activity` | 1:N | `activity.category_id` |
| `sys_user` | `activity_registration` | 1:N | `activity_registration.user_id` |
| `activity` | `activity_registration` | 1:N | `activity_registration.activity_id` |
| `activity_registration` | `activity_checkin` | 1:0..1 | `activity_checkin.registration_id` |
| `sys_user` | `activity_feedback` | 1:N | `activity_feedback.user_id` |
| `activity` | `activity_feedback` | 1:N | `activity_feedback.activity_id` |
| `sys_user` | `notification` | 1:N | `notification.user_id` |
| `activity` | `forum_post` | 1:N | `forum_post.activity_id` |
| `sys_user` | `forum_post` | 1:N | `forum_post.author_id` |
| `forum_post` | `forum_comment` | 1:N | `forum_comment.post_id` |
| `sys_user` | `forum_comment` | 1:N | `forum_comment.author_id` |
| `forum_post` | `forum_post_tag` | 1:N | `forum_post_tag.post_id` |
| `forum_tag` | `forum_post_tag` | 1:N | `forum_post_tag.tag_id` |
| `forum_post` | `forum_post_like` | 1:N | `forum_post_like.post_id` |
| `sys_user` | `forum_post_like` | 1:N | `forum_post_like.user_id` |

## 唯一约束

- `sys_role.role_code` 唯一。
- `sys_user.username` 唯一。
- `sys_user.student_no` 唯一。
- `activity_category.category_name` 唯一。
- `activity_registration(activity_id, user_id)` 唯一，防止重复报名。
- `activity_checkin.registration_id` 唯一，防止重复签到。
- `activity_feedback(activity_id, user_id)` 唯一，防止同一用户重复评价同一活动。
- `forum_tag.tag_name` 唯一。
- `forum_post_tag(post_id, tag_id)` 唯一，防止帖子重复绑定同一标签。
- `forum_post_like(post_id, user_id)` 唯一，防止用户重复点赞同一帖子。

## 关键业务约束

- 活动结束时间必须晚于开始时间。
- 活动容量必须大于 0。
- 反馈评分范围为 1 到 5。
- 状态字段用 `CHECK` 约束限制枚举值。
- 只有 `organizer/admin` 可以发起活动。
- 只有活动发起者本人或 `admin` 可以修改活动状态与执行人工补签。
- 论坛帖子必须关联活动；正在进行中的活动允许携带 Tag，已结束活动进入“往期讨论”范围。

## 常用连接路径

查询活动报名名单：

```text
activity -> activity_registration -> sys_user
```

查询活动签到名单：

```text
activity -> activity_registration -> activity_checkin -> sys_user
```

查询学生参与记录：

```text
sys_user -> activity_registration -> activity -> activity_category
```

查询活动评价：

```text
activity -> activity_feedback -> sys_user
```

查询论坛热度榜：

```text
activity -> forum_post -> forum_comment / forum_post_like / forum_post_tag -> forum_tag
```

查询往期活动归档：

```text
activity(status=finished) -> activity_feedback -> sys_user
```
