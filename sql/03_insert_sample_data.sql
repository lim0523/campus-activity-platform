USE campus_activity_db;

INSERT INTO sys_role (role_code, role_name, description) VALUES
('student', '学生', '普通学生用户'),
('organizer', '组织者', '活动发布与签到管理人员'),
('admin', '管理员', '系统管理员');

INSERT INTO sys_user (role_id, username, password_hash, real_name, student_no, phone, email) VALUES
(1, 'stu001', '123456', '张雨晴', '20240001', '13800000001', 'stu001@example.com'),
(1, 'stu002', '123456', '李明轩', '20240002', '13800000002', 'stu002@example.com'),
(1, 'stu003', '123456', '王思远', '20240003', '13800000003', 'stu003@example.com'),
(2, 'org001', '123456', '陈老师', NULL, '13800001001', 'org001@example.com'),
(2, 'org002', '123456', '刘社长', NULL, '13800001002', 'org002@example.com'),
(3, 'admin', '123456', '系统管理员', NULL, '13800002001', 'admin@example.com');

INSERT INTO activity_category (category_name, description) VALUES
('学术讲座', '院系讲座、公开课、专业分享'),
('社团活动', '社团招新、兴趣活动、社团展示'),
('志愿服务', '校内外志愿服务活动'),
('竞赛培训', '比赛宣讲、训练营、赛前辅导');

INSERT INTO activity (
  category_id, organizer_id, title, description, location,
  start_time, end_time, registration_deadline, capacity, status
) VALUES
(1, 4, '人工智能学习路线分享', '面向低年级学生的 AI 学习路线讲座', '教学楼 A101', '2026-06-10 19:00:00', '2026-06-10 21:00:00', '2026-06-09 22:00:00', 80, 'published'),
(2, 5, '摄影社校园采风活动', '摄影社组织的校园主题采风', '图书馆门口', '2026-06-12 15:00:00', '2026-06-12 17:30:00', '2026-06-11 20:00:00', 30, 'published'),
(3, 4, '图书馆志愿整理活动', '协助图书馆整理书架和引导读者', '图书馆一楼', '2026-06-15 09:00:00', '2026-06-15 12:00:00', '2026-06-14 18:00:00', 20, 'published'),
(4, 5, '数据库竞赛赛前训练营', '数据库设计与 SQL 优化训练', '实验楼 B203', '2026-06-18 18:30:00', '2026-06-18 21:00:00', '2026-06-17 22:00:00', 40, 'draft');

INSERT INTO activity_registration (activity_id, user_id, status, registered_at) VALUES
(1, 1, 'approved', '2026-06-01 10:00:00'),
(1, 2, 'approved', '2026-06-01 11:20:00'),
(1, 3, 'cancelled', '2026-06-02 09:30:00'),
(2, 1, 'approved', '2026-06-03 14:00:00'),
(2, 3, 'approved', '2026-06-03 15:15:00'),
(3, 2, 'approved', '2026-06-04 08:45:00');

INSERT INTO activity_checkin (registration_id, checkin_time, checkin_result, operator_id, note) VALUES
(1, '2026-06-10 18:55:00', 'normal', 4, '现场签到'),
(2, '2026-06-10 19:08:00', 'late', 4, '迟到签到'),
(4, '2026-06-12 14:58:00', 'normal', 5, '现场签到');

UPDATE activity_registration SET status = 'attended' WHERE registration_id IN (1, 2, 4);

INSERT INTO activity_feedback (activity_id, user_id, rating, content) VALUES
(1, 1, 5, '内容清晰，对后续学习有帮助'),
(1, 2, 4, '案例比较实用'),
(2, 1, 5, '活动安排很好');

INSERT INTO notification (user_id, title, content, read_status) VALUES
(1, '报名成功', '你已成功报名人工智能学习路线分享', 'read'),
(2, '报名成功', '你已成功报名人工智能学习路线分享', 'unread'),
(3, '活动提醒', '摄影社校园采风活动即将开始', 'unread');

INSERT INTO forum_tag (tag_name, color_hex) VALUES
('报名攻略', '#C55A36'),
('现场体验', '#486B5B'),
('复盘总结', '#8D6B2E'),
('资源分享', '#5D63B5');

INSERT INTO forum_post (activity_id, author_id, title, content, scope, view_count, status, created_at, updated_at) VALUES
(2, 1, '摄影社采风活动穿什么更合适？', '第一次参加摄影社外拍，大家建议穿深色还是浅色？', 'ongoing', 12, 'visible', '2026-06-09 18:20:00', '2026-06-09 18:20:00'),
(1, 2, 'AI 学习路线讲座的笔记整理', '把老师讲到的课程、工具和学习路径做了一份整理，欢迎补充。', 'archive', 36, 'visible', '2026-06-10 22:10:00', '2026-06-10 22:10:00'),
(3, 3, '图书馆志愿活动值不值得报名？', '我想知道这个活动会不会比较累，适合第一次参加志愿服务的人吗？', 'ongoing', 9, 'visible', '2026-06-11 09:15:00', '2026-06-11 09:15:00');

INSERT INTO forum_post_tag (post_id, tag_id) VALUES
(1, 1),
(1, 2),
(3, 1);

INSERT INTO forum_comment (post_id, author_id, content, created_at) VALUES
(1, 5, '建议穿方便活动的衣服，现场会有一些走动和取景。', '2026-06-09 18:45:00'),
(1, 2, '我上次去类似活动穿浅色还挺出片的。', '2026-06-09 19:02:00'),
(2, 1, '我觉得老师提到的项目驱动学习法特别实用。', '2026-06-10 22:30:00'),
(3, 4, '志愿活动主要是整理和引导，不会特别累，适合新手。', '2026-06-11 09:40:00');

INSERT INTO forum_post_like (post_id, user_id, created_at) VALUES
(1, 2, '2026-06-09 18:50:00'),
(1, 3, '2026-06-09 19:05:00'),
(2, 1, '2026-06-10 22:35:00'),
(2, 3, '2026-06-10 22:40:00');
