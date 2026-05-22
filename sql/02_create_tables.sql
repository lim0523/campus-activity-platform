USE campus_activity_db;

CREATE TABLE sys_role (
  role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL,
  role_name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_sys_role_code UNIQUE (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_user (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  student_no VARCHAR(30),
  phone VARCHAR(30),
  email VARCHAR(100),
  status VARCHAR(20) NOT NULL DEFAULT 'enabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_sys_user_username UNIQUE (username),
  CONSTRAINT uk_sys_user_student_no UNIQUE (student_no),
  CONSTRAINT chk_sys_user_status CHECK (status IN ('enabled', 'disabled')),
  CONSTRAINT fk_sys_user_role FOREIGN KEY (role_id) REFERENCES sys_role(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activity_category (
  category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_name VARCHAR(80) NOT NULL,
  description VARCHAR(255),
  status VARCHAR(20) NOT NULL DEFAULT 'enabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_activity_category_name UNIQUE (category_name),
  CONSTRAINT chk_activity_category_status CHECK (status IN ('enabled', 'disabled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activity (
  activity_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  organizer_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  description TEXT,
  location VARCHAR(120) NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  registration_deadline DATETIME NOT NULL,
  capacity INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'draft',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_activity_capacity CHECK (capacity > 0),
  CONSTRAINT chk_activity_time CHECK (end_time > start_time),
  CONSTRAINT chk_activity_deadline CHECK (registration_deadline <= start_time),
  CONSTRAINT chk_activity_status CHECK (status IN ('draft', 'published', 'cancelled', 'finished')),
  CONSTRAINT fk_activity_category FOREIGN KEY (category_id) REFERENCES activity_category(category_id),
  CONSTRAINT fk_activity_organizer FOREIGN KEY (organizer_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activity_registration (
  registration_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'approved',
  registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cancelled_at DATETIME,
  remark VARCHAR(255),
  CONSTRAINT uk_registration_activity_user UNIQUE (activity_id, user_id),
  CONSTRAINT chk_registration_status CHECK (status IN ('pending', 'approved', 'rejected', 'cancelled', 'attended')),
  CONSTRAINT fk_registration_activity FOREIGN KEY (activity_id) REFERENCES activity(activity_id),
  CONSTRAINT fk_registration_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activity_checkin (
  checkin_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  registration_id BIGINT NOT NULL,
  checkin_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  checkin_result VARCHAR(20) NOT NULL DEFAULT 'normal',
  operator_id BIGINT,
  note VARCHAR(255),
  CONSTRAINT uk_checkin_registration UNIQUE (registration_id),
  CONSTRAINT chk_checkin_result CHECK (checkin_result IN ('normal', 'late', 'manual')),
  CONSTRAINT fk_checkin_registration FOREIGN KEY (registration_id) REFERENCES activity_registration(registration_id),
  CONSTRAINT fk_checkin_operator FOREIGN KEY (operator_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activity_feedback (
  feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  rating TINYINT NOT NULL,
  content VARCHAR(500),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_feedback_activity_user UNIQUE (activity_id, user_id),
  CONSTRAINT chk_feedback_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT fk_feedback_activity FOREIGN KEY (activity_id) REFERENCES activity(activity_id),
  CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification (
  notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  content VARCHAR(500) NOT NULL,
  read_status VARCHAR(20) NOT NULL DEFAULT 'unread',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at DATETIME,
  CONSTRAINT chk_notification_read_status CHECK (read_status IN ('unread', 'read')),
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE forum_tag (
  tag_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tag_name VARCHAR(50) NOT NULL,
  color_hex VARCHAR(20) NOT NULL DEFAULT '#C55A36',
  status VARCHAR(20) NOT NULL DEFAULT 'enabled',
  CONSTRAINT uk_forum_tag_name UNIQUE (tag_name),
  CONSTRAINT chk_forum_tag_status CHECK (status IN ('enabled', 'disabled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE forum_post (
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

CREATE TABLE forum_comment (
  comment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  content VARCHAR(1000) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_forum_comment_post FOREIGN KEY (post_id) REFERENCES forum_post(post_id),
  CONSTRAINT fk_forum_comment_author FOREIGN KEY (author_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE forum_post_tag (
  post_tag_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  CONSTRAINT uk_forum_post_tag UNIQUE (post_id, tag_id),
  CONSTRAINT fk_forum_post_tag_post FOREIGN KEY (post_id) REFERENCES forum_post(post_id),
  CONSTRAINT fk_forum_post_tag_tag FOREIGN KEY (tag_id) REFERENCES forum_tag(tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE forum_post_like (
  like_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_forum_post_like UNIQUE (post_id, user_id),
  CONSTRAINT fk_forum_post_like_post FOREIGN KEY (post_id) REFERENCES forum_post(post_id),
  CONSTRAINT fk_forum_post_like_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
