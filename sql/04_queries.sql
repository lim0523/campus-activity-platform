USE campus_activity_db;

-- 1. 查询当前可报名活动
SELECT
  a.activity_id,
  a.title,
  c.category_name,
  u.real_name AS organizer_name,
  a.location,
  a.start_time,
  a.capacity,
  COUNT(r.registration_id) AS approved_count
FROM activity a
JOIN activity_category c ON a.category_id = c.category_id
JOIN sys_user u ON a.organizer_id = u.user_id
LEFT JOIN activity_registration r
  ON a.activity_id = r.activity_id
 AND r.status IN ('approved', 'attended')
WHERE a.status = 'published'
GROUP BY a.activity_id, a.title, c.category_name, u.real_name, a.location, a.start_time, a.capacity
HAVING approved_count < a.capacity
ORDER BY a.start_time;

-- 2. 查询某活动报名名单
SELECT
  a.title,
  su.real_name,
  su.student_no,
  ar.status,
  ar.registered_at
FROM activity_registration ar
JOIN activity a ON ar.activity_id = a.activity_id
JOIN sys_user su ON ar.user_id = su.user_id
WHERE ar.activity_id = 1
ORDER BY ar.registered_at;

-- 3. 查询某活动签到名单
SELECT
  a.title,
  su.real_name,
  su.student_no,
  ac.checkin_time,
  ac.checkin_result
FROM activity_registration ar
JOIN activity a ON ar.activity_id = a.activity_id
JOIN sys_user su ON ar.user_id = su.user_id
JOIN activity_checkin ac ON ar.registration_id = ac.registration_id
WHERE ar.activity_id = 1
ORDER BY ac.checkin_time;

-- 4. 查询某活动已报名但未签到的用户
SELECT
  a.title,
  su.real_name,
  su.student_no,
  ar.status
FROM activity_registration ar
JOIN activity a ON ar.activity_id = a.activity_id
JOIN sys_user su ON ar.user_id = su.user_id
LEFT JOIN activity_checkin ac ON ar.registration_id = ac.registration_id
WHERE ar.activity_id = 1
  AND ar.status IN ('approved', 'attended')
  AND ac.checkin_id IS NULL;

-- 5. 查询学生个人参与记录
SELECT
  su.real_name,
  a.title,
  c.category_name,
  ar.status AS registration_status,
  ac.checkin_result,
  a.start_time
FROM sys_user su
JOIN activity_registration ar ON su.user_id = ar.user_id
JOIN activity a ON ar.activity_id = a.activity_id
JOIN activity_category c ON a.category_id = c.category_id
LEFT JOIN activity_checkin ac ON ar.registration_id = ac.registration_id
WHERE su.user_id = 1
ORDER BY a.start_time DESC;

-- 6. 统计每个活动的报名人数、签到人数和签到率
SELECT
  a.activity_id,
  a.title,
  COUNT(DISTINCT CASE WHEN ar.status IN ('approved', 'attended') THEN ar.registration_id END) AS registered_count,
  COUNT(DISTINCT ac.checkin_id) AS checkin_count,
  ROUND(
    COUNT(DISTINCT ac.checkin_id) /
    NULLIF(COUNT(DISTINCT CASE WHEN ar.status IN ('approved', 'attended') THEN ar.registration_id END), 0) * 100,
    2
  ) AS checkin_rate_percent
FROM activity a
LEFT JOIN activity_registration ar ON a.activity_id = ar.activity_id
LEFT JOIN activity_checkin ac ON ar.registration_id = ac.registration_id
GROUP BY a.activity_id, a.title
ORDER BY checkin_rate_percent DESC;

-- 7. 统计各分类活动数量和报名总人数
SELECT
  c.category_name,
  COUNT(DISTINCT a.activity_id) AS activity_count,
  COUNT(DISTINCT CASE WHEN ar.status IN ('approved', 'attended') THEN ar.registration_id END) AS registration_count
FROM activity_category c
LEFT JOIN activity a ON c.category_id = a.category_id
LEFT JOIN activity_registration ar ON a.activity_id = ar.activity_id
GROUP BY c.category_id, c.category_name
ORDER BY registration_count DESC;

-- 8. 查询热门活动排行
SELECT
  a.title,
  c.category_name,
  a.capacity,
  COUNT(ar.registration_id) AS registration_count
FROM activity a
JOIN activity_category c ON a.category_id = c.category_id
LEFT JOIN activity_registration ar
  ON a.activity_id = ar.activity_id
 AND ar.status IN ('approved', 'attended')
GROUP BY a.activity_id, a.title, c.category_name, a.capacity
ORDER BY registration_count DESC, a.start_time ASC;

-- 9. 查询活动平均评分
SELECT
  a.title,
  COUNT(f.feedback_id) AS feedback_count,
  ROUND(AVG(f.rating), 2) AS avg_rating
FROM activity a
LEFT JOIN activity_feedback f ON a.activity_id = f.activity_id
GROUP BY a.activity_id, a.title
ORDER BY avg_rating DESC;

-- 10. 查询组织者发布活动的统计数据
SELECT
  organizer.real_name AS organizer_name,
  COUNT(DISTINCT a.activity_id) AS activity_count,
  COUNT(DISTINCT ar.registration_id) AS registration_count,
  COUNT(DISTINCT ac.checkin_id) AS checkin_count
FROM sys_user organizer
LEFT JOIN activity a ON organizer.user_id = a.organizer_id
LEFT JOIN activity_registration ar
  ON a.activity_id = ar.activity_id
 AND ar.status IN ('approved', 'attended')
LEFT JOIN activity_checkin ac ON ar.registration_id = ac.registration_id
WHERE organizer.role_id = 2
GROUP BY organizer.user_id, organizer.real_name;
