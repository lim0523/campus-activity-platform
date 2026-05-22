USE campus_activity_db;

CREATE OR REPLACE VIEW v_activity_statistics AS
SELECT
  a.activity_id,
  a.title,
  c.category_name,
  organizer.real_name AS organizer_name,
  a.capacity,
  COUNT(DISTINCT CASE WHEN ar.status IN ('approved', 'attended') THEN ar.registration_id END) AS registered_count,
  COUNT(DISTINCT ac.checkin_id) AS checkin_count,
  ROUND(
    COUNT(DISTINCT ac.checkin_id) /
    NULLIF(COUNT(DISTINCT CASE WHEN ar.status IN ('approved', 'attended') THEN ar.registration_id END), 0) * 100,
    2
  ) AS checkin_rate_percent
FROM activity a
JOIN activity_category c ON a.category_id = c.category_id
JOIN sys_user organizer ON a.organizer_id = organizer.user_id
LEFT JOIN activity_registration ar ON a.activity_id = ar.activity_id
LEFT JOIN activity_checkin ac ON ar.registration_id = ac.registration_id
GROUP BY a.activity_id, a.title, c.category_name, organizer.real_name, a.capacity;

CREATE OR REPLACE VIEW v_user_activity_history AS
SELECT
  su.user_id,
  su.real_name,
  su.student_no,
  a.activity_id,
  a.title,
  c.category_name,
  ar.status AS registration_status,
  ar.registered_at,
  ac.checkin_time,
  ac.checkin_result
FROM sys_user su
JOIN activity_registration ar ON su.user_id = ar.user_id
JOIN activity a ON ar.activity_id = a.activity_id
JOIN activity_category c ON a.category_id = c.category_id
LEFT JOIN activity_checkin ac ON ar.registration_id = ac.registration_id;

CREATE INDEX idx_activity_time ON activity(start_time, end_time);
CREATE INDEX idx_activity_status ON activity(status);
CREATE INDEX idx_registration_status ON activity_registration(status);
CREATE INDEX idx_registration_user ON activity_registration(user_id);
CREATE INDEX idx_checkin_time ON activity_checkin(checkin_time);
CREATE INDEX idx_feedback_rating ON activity_feedback(rating);

-- 查看活动统计视图
SELECT * FROM v_activity_statistics ORDER BY activity_id;

-- 查看用户参与历史视图
SELECT * FROM v_user_activity_history ORDER BY user_id, registered_at;
