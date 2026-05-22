USE campus_activity_db;

UPDATE sys_user
SET password_hash = '123456'
WHERE username IN ('stu001', 'stu002', 'stu003', 'org001', 'org002', 'admin');

CREATE TABLE IF NOT EXISTS forum_tag (
  tag_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tag_name VARCHAR(50) NOT NULL,
  color_hex VARCHAR(20) NOT NULL DEFAULT '#C55A36',
  status VARCHAR(20) NOT NULL DEFAULT 'enabled',
  CONSTRAINT uk_forum_tag_name UNIQUE (tag_name),
  CONSTRAINT chk_forum_tag_status CHECK (status IN ('enabled', 'disabled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS forum_post (
  post_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  scope VARCHAR(20) NOT NULL DEFAULT 'ongoing',
  view_count INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'visible',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_forum_post_scope CHECK (scope IN ('ongoing', 'archive')),
  CONSTRAINT chk_forum_post_status CHECK (status IN ('visible', 'hidden')),
  CONSTRAINT fk_forum_post_activity FOREIGN KEY (activity_id) REFERENCES activity(activity_id),
  CONSTRAINT fk_forum_post_author FOREIGN KEY (author_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS forum_comment (
  comment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  content VARCHAR(1000) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_forum_comment_post FOREIGN KEY (post_id) REFERENCES forum_post(post_id),
  CONSTRAINT fk_forum_comment_author FOREIGN KEY (author_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS forum_post_tag (
  post_tag_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  CONSTRAINT uk_forum_post_tag UNIQUE (post_id, tag_id),
  CONSTRAINT fk_forum_post_tag_post FOREIGN KEY (post_id) REFERENCES forum_post(post_id),
  CONSTRAINT fk_forum_post_tag_tag FOREIGN KEY (tag_id) REFERENCES forum_tag(tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS forum_post_like (
  like_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_forum_post_like UNIQUE (post_id, user_id),
  CONSTRAINT fk_forum_post_like_post FOREIGN KEY (post_id) REFERENCES forum_post(post_id),
  CONSTRAINT fk_forum_post_like_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO forum_tag (tag_name, color_hex, status) VALUES
('报名攻略', '#C55A36', 'enabled'),
('现场体验', '#486B5B', 'enabled'),
('复盘总结', '#8D6B2E', 'enabled'),
('资源分享', '#5D63B5', 'enabled')
ON DUPLICATE KEY UPDATE color_hex = VALUES(color_hex), status = VALUES(status);

INSERT INTO forum_post (post_id, activity_id, author_id, title, content, scope, view_count, status, created_at, updated_at) VALUES
(1, 2, 1, '摄影社采风活动穿什么更合适？', '第一次参加摄影社外拍，大家建议穿深色还是浅色？', 'ongoing', 12, 'visible', '2026-06-09 18:20:00', '2026-06-09 18:20:00'),
(2, 1, 2, 'AI 学习路线讲座的笔记整理', '把老师讲到的课程、工具和学习路径做了一份整理，欢迎补充。', 'archive', 36, 'visible', '2026-06-10 22:10:00', '2026-06-10 22:10:00'),
(3, 3, 3, '图书馆志愿活动值不值得报名？', '我想知道这个活动会不会比较累，适合第一次参加志愿服务的人吗？', 'ongoing', 9, 'visible', '2026-06-11 09:15:00', '2026-06-11 09:15:00')
ON DUPLICATE KEY UPDATE
title = VALUES(title),
content = VALUES(content),
scope = VALUES(scope),
view_count = VALUES(view_count),
status = VALUES(status),
updated_at = VALUES(updated_at);

INSERT INTO forum_post_tag (post_id, tag_id) VALUES
(1, 1),
(1, 2),
(3, 1)
ON DUPLICATE KEY UPDATE tag_id = VALUES(tag_id);

INSERT INTO forum_comment (comment_id, post_id, author_id, content, created_at) VALUES
(1, 1, 5, '建议穿方便活动的衣服，现场会有一些走动和取景。', '2026-06-09 18:45:00'),
(2, 1, 2, '我上次去类似活动穿浅色还挺出片的。', '2026-06-09 19:02:00'),
(3, 2, 1, '我觉得老师提到的项目驱动学习法特别实用。', '2026-06-10 22:30:00'),
(4, 3, 4, '志愿活动主要是整理和引导，不会特别累，适合新手。', '2026-06-11 09:40:00')
ON DUPLICATE KEY UPDATE
content = VALUES(content),
created_at = VALUES(created_at);

INSERT INTO forum_post_like (post_id, user_id, created_at) VALUES
(1, 2, '2026-06-09 18:50:00'),
(1, 3, '2026-06-09 19:05:00'),
(2, 1, '2026-06-10 22:35:00'),
(2, 3, '2026-06-10 22:40:00')
ON DUPLICATE KEY UPDATE created_at = VALUES(created_at);
