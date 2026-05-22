USE campus_activity_db;

-- 测试 1：重复报名应失败，触发 uk_registration_activity_user
-- INSERT INTO activity_registration (activity_id, user_id, status)
-- VALUES (1, 1, 'approved');

-- 测试 2：未存在的用户报名应失败，触发 fk_registration_user
-- INSERT INTO activity_registration (activity_id, user_id, status)
-- VALUES (1, 9999, 'approved');

-- 测试 3：活动容量不能小于等于 0，触发 chk_activity_capacity
-- INSERT INTO activity (
--   category_id, organizer_id, title, location, start_time, end_time,
--   registration_deadline, capacity, status
-- ) VALUES (
--   1, 4, '错误容量活动', 'A101', '2026-07-01 10:00:00',
--   '2026-07-01 12:00:00', '2026-06-30 18:00:00', 0, 'published'
-- );

-- 测试 4：活动结束时间必须晚于开始时间，触发 chk_activity_time
-- INSERT INTO activity (
--   category_id, organizer_id, title, location, start_time, end_time,
--   registration_deadline, capacity, status
-- ) VALUES (
--   1, 4, '错误时间活动', 'A101', '2026-07-01 12:00:00',
--   '2026-07-01 10:00:00', '2026-06-30 18:00:00', 30, 'published'
-- );

-- 测试 5：重复签到应失败，触发 uk_checkin_registration
-- INSERT INTO activity_checkin (registration_id, checkin_result, operator_id)
-- VALUES (1, 'manual', 4);

-- 测试 6：反馈评分只能在 1 到 5，触发 chk_feedback_rating
-- INSERT INTO activity_feedback (activity_id, user_id, rating, content)
-- VALUES (2, 2, 6, '错误评分');

-- 可执行的检查查询：查看当前约束相关数据
SELECT activity_id, title, capacity, status FROM activity;
SELECT registration_id, activity_id, user_id, status FROM activity_registration;
SELECT checkin_id, registration_id, checkin_result FROM activity_checkin;
