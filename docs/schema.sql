-- =====================================================================
-- 宠爱到家（PetCare AtHome）数据库设计 V1.0
-- 对应 petcare-athome-openapi.yaml V1.0 MVP（P0 范围）
--
-- 约定：
--   1. MySQL 8.0+，InnoDB，utf8mb4 / utf8mb4_0900_ai_ci
--   2. 金额一律为「分」，INT 存储（6800 = 68.00 元）
--   3. 布尔用 TINYINT(1)；枚举用 VARCHAR + COMMENT，取值即 API 枚举值
--   4. 软删除用 deleted_at（NULL 未删）；订单与服务记录永不物理删（追溯 ≥3 年）
--   5. 敏感字段（入户凭证 / 身份证号 / 银行卡号）存 AES-GCM 密文（*_cipher），
--      密钥由 KMS 托管，展示一律走脱敏字段
--   6. 坐标 DECIMAL(10,6)；服务时段 order_day 存 TIME 起止，订单冗余展示用字符串
--
-- 枚举统一说明（接口文档中不一致处，以本文件为准）：
--   服务类目   FEEDING / GROOMING / MEDICAL（弃用 FEED/GROOM）
--   认证类型   REAL_NAME / FACE / HEALTH_CERT / NO_CRIMINAL / QUALIFICATION / PLATFORM_TRAINING
--   告警类型   TIMEOUT_NO_ACCEPT / CHECKIN_DEVIATION / PROVIDER_EXCEPTION / USER_COMPLAINT / PAYMENT_ABNORMAL
--   提现状态   APPLIED / APPROVED / PAID / REJECTED（弃用 PENDING）
--   申诉类型   PET_ABNORMAL / ENV_ABNORMAL / SERVICE_QUALITY / REFUND_DISPUTE / OTHER
--   奖惩类型   WARNING / FINE / SUSPEND / BAN / REWARD
--   申请状态   SUBMITTED / REVIEWING / APPROVED / REJECTED
-- =====================================================================

-- =====================================================================
-- 1. 通用
-- =====================================================================

CREATE TABLE `dict` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `type`       VARCHAR(64)  NOT NULL COMMENT '字典类型：pet_personality/cancel_reason/exception_type/refund_reason',
  `item_key`   VARCHAR(64)  NOT NULL COMMENT '取值编码，与 API 枚举一致',
  `item_value` VARCHAR(128) NOT NULL COMMENT '展示文案',
  `sort`       INT          NOT NULL DEFAULT 0,
  `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '1 启用 0 停用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_key` (`type`, `item_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通用字典';

-- =====================================================================
-- 2. 用户端：账户 / 地址 / 宠物
-- =====================================================================

CREATE TABLE `user` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `openid`          VARCHAR(64)  NOT NULL COMMENT '微信 openid（用户端小程序）',
  `unionid`         VARCHAR(64)  DEFAULT NULL,
  `nickname`        VARCHAR(64)  NOT NULL DEFAULT '',
  `avatar`          VARCHAR(512) DEFAULT NULL COMMENT 'OSS URL',
  `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '手机号明文仅此处，对外接口脱敏',
  `complaint_marked` TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '客诉标记',
  `blacklisted`     TINYINT(1)  NOT NULL DEFAULT 0,
  `blacklist_reason` VARCHAR(255) DEFAULT NULL,
  `status`          VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/BANNED/CANCELLED',
  `last_login_at`   DATETIME     DEFAULT NULL,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at`      DATETIME     DEFAULT NULL COMMENT '注销时间；注销后昵称/头像脱敏覆写',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='C 端用户';

CREATE TABLE `user_subscription` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT UNSIGNED NOT NULL,
  `template`   VARCHAR(32) NOT NULL COMMENT 'DISPATCHED/DEPARTED/ARRIVED/REPORT_READY/REFUND_PROGRESS',
  `accepted`   TINYINT(1)  NOT NULL COMMENT '一次性订阅授权结果',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_template` (`user_id`, `template`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订阅消息授权上报（取每模板最新一条）';

CREATE TABLE `address` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`            BIGINT UNSIGNED NOT NULL,
  `contact_name`       VARCHAR(32)  NOT NULL,
  `contact_phone`      VARCHAR(20)  NOT NULL,
  `city`               VARCHAR(32)  NOT NULL DEFAULT '',
  `district`           VARCHAR(32)  NOT NULL DEFAULT '',
  `community`          VARCHAR(64)  NOT NULL COMMENT '小区',
  `detail`             VARCHAR(128) NOT NULL COMMENT '楼栋门牌',
  `longitude`          DECIMAL(10,6) NOT NULL,
  `latitude`           DECIMAL(10,6) NOT NULL,
  `entry_mode`         VARCHAR(16)  NOT NULL COMMENT 'SMART_LOCK/PROPERTY/KEY_DEPOSIT/AT_HOME',
  `entry_secret_cipher` VARCHAR(1024) DEFAULT NULL COMMENT '入户凭证密文（门锁临时密码/钥匙寄存位置），仅服务时段解密给当单服务师',
  `is_default`         TINYINT(1)   NOT NULL DEFAULT 0,
  `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at`         DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户地址与入户方式';

CREATE TABLE `entry_credential_log` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `address_id`  BIGINT UNSIGNED NOT NULL,
  `order_id`    BIGINT UNSIGNED NOT NULL,
  `provider_id` BIGINT UNSIGNED NOT NULL,
  `action`      VARCHAR(8)  NOT NULL COMMENT 'VIEW/UNLOCK',
  `ip`          VARCHAR(64) DEFAULT NULL,
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_address` (`address_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入户凭证查看/使用留痕（对用户透明展示）';

CREATE TABLE `pet` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`            BIGINT UNSIGNED NOT NULL,
  `name`               VARCHAR(32) NOT NULL,
  `species`            VARCHAR(16) NOT NULL COMMENT 'CAT/DOG/OTHER，字典维护',
  `breed`              VARCHAR(64) DEFAULT NULL,
  `gender`             VARCHAR(8)  NOT NULL DEFAULT 'UNKNOWN' COMMENT 'MALE/FEMALE/UNKNOWN',
  `birthday`           DATE        DEFAULT NULL,
  `weight_kg`          DECIMAL(5,2) DEFAULT NULL,
  `sterilized`         TINYINT(1)  NOT NULL DEFAULT 0,
  `avatar`             VARCHAR(512) DEFAULT NULL,
  `personality_tags`   JSON DEFAULT NULL COMMENT '性格标签 key 数组，字典 pet_personality',
  `personality_note`   VARCHAR(512) DEFAULT NULL,
  `allergies`          VARCHAR(512) DEFAULT NULL COMMENT '过敏与禁忌',
  `chronic_disease`    VARCHAR(512) DEFAULT NULL COMMENT '慢病与用药',
  `feeding_instruction` JSON DEFAULT NULL COMMENT '喂养指令 {dailyFoodGram,feedTimes,waterNote,litterOrWalkNote}，服务时带入 SOP',
  `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at`         DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宠物档案';

CREATE TABLE `pet_vaccination` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `pet_id`          BIGINT UNSIGNED NOT NULL,
  `vaccine_name`    VARCHAR(64) NOT NULL,
  `vaccinated_at`   DATE NOT NULL,
  `next_at`         DATE DEFAULT NULL COMMENT '下次接种时间（提醒用）',
  `certificate_url` VARCHAR(512) DEFAULT NULL COMMENT '疫苗本照片',
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet` (`pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='疫苗记录';

-- =====================================================================
-- 3. 服务类目与定价
-- =====================================================================

CREATE TABLE `service` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`            VARCHAR(64) NOT NULL COMMENT '如：上门喂养',
  `category`        VARCHAR(16) NOT NULL COMMENT 'FEEDING/GROOMING/MEDICAL',
  `cover_url`       VARCHAR(512) DEFAULT NULL,
  `price_from`      INT NOT NULL DEFAULT 0 COMMENT '起步价（分）',
  `duration_minutes` INT NOT NULL DEFAULT 60,
  `tags`            JSON DEFAULT NULL,
  `content_items`   JSON DEFAULT NULL COMMENT '服务内容清单（字符串数组）',
  `notice`          TEXT COMMENT '服务须知',
  `refund_policy`   TEXT COMMENT '退款规则文案',
  `status`          VARCHAR(8) NOT NULL DEFAULT 'ON' COMMENT 'ON/OFF',
  `sort`            INT NOT NULL DEFAULT 0,
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at`      DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_category_status` (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务类目 SKU';

CREATE TABLE `service_price_rule` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `service_id`  BIGINT UNSIGNED NOT NULL,
  `rule_type`   VARCHAR(16) NOT NULL COMMENT 'BASE/EXTRA_PET/WEIGHT/DISTANCE',
  `label`       VARCHAR(64) NOT NULL COMMENT '展示名，如「每加一只猫」',
  `amount`      INT NOT NULL DEFAULT 0 COMMENT '金额（分）；阶梯规则存 rule_config',
  `rule_config` JSON DEFAULT NULL COMMENT '扩展参数，如 {"perPet":3000} {"perKg":500,"startKg":10} {"perKm":100,"freeKm":5}',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_service` (`service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务计价规则（价格试算依据）';

CREATE TABLE `sop_item` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `service_type` VARCHAR(16) NOT NULL COMMENT 'FEEDING/GROOMING/MEDICAL',
  `code`         VARCHAR(32) NOT NULL COMMENT '步骤标识，如 FEED_FOOD/CLEAN_LITTER',
  `title`        VARCHAR(64) NOT NULL COMMENT '如：喂食并拍照',
  `required`     TINYINT(1) NOT NULL DEFAULT 1,
  `need_photo`   TINYINT(1) NOT NULL DEFAULT 0 COMMENT '强制拍照（带水印）',
  `sort`         INT NOT NULL DEFAULT 0,
  `status`       TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`service_type`, `code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SOP 执行清单模板（按服务类型）';

-- =====================================================================
-- 4. 服务师
-- =====================================================================

CREATE TABLE `provider` (
  `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `openid`              VARCHAR(64) NOT NULL COMMENT '微信 openid（服务师端小程序）',
  `phone`               VARCHAR(20) NOT NULL,
  `real_name`           VARCHAR(32) NOT NULL,
  `id_card_no_cipher`   VARCHAR(512) NOT NULL COMMENT '身份证号密文',
  `avatar`              VARCHAR(512) DEFAULT NULL,
  `city`                VARCHAR(32) NOT NULL DEFAULT '',
  `service_areas`       JSON DEFAULT NULL COMMENT '可服务区域（区/商圈名数组）',
  `service_types`       JSON DEFAULT NULL COMMENT '可接服务类型 [FEEDING,GROOMING,MEDICAL]；MEDICAL 需持有对应认证',
  `experiences`         TEXT COMMENT '从业经历',
  `specialties`         JSON DEFAULT NULL COMMENT '专长标签，如 猫行为学/老年宠物护理',
  `intro`               TEXT COMMENT '个人介绍',
  `years`               TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '从业年限',
  `status`              VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ACTIVE/SUSPENDED/BANNED/QUIT',
  `level`               VARCHAR(16) DEFAULT NULL COMMENT '等级，V1.1 启用',
  `rating_avg`          DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '评分冗余，评价后异步重算',
  `rating_professional` DECIMAL(3,2) NOT NULL DEFAULT 0.00,
  `rating_punctual`     DECIMAL(3,2) NOT NULL DEFAULT 0.00,
  `rating_communication` DECIMAL(3,2) NOT NULL DEFAULT 0.00,
  `rating_care`         DECIMAL(3,2) NOT NULL DEFAULT 0.00,
  `service_count`       INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计完单数',
  `reject_count_7d`     INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '近 7 日连续拒单数，超阈值派单降权',
  `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最近登录时间',
  `deleted_at`          DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status_city` (`status`, `city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务师（真实姓名对外脱敏展示，由服务端截取）';

CREATE TABLE `provider_application` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `provider_id`        BIGINT UNSIGNED NOT NULL,
  `real_name`          VARCHAR(32) NOT NULL,
  `id_card_no_cipher`  VARCHAR(512) NOT NULL,
  `id_card_front_url`  VARCHAR(512) NOT NULL,
  `id_card_back_url`   VARCHAR(512) NOT NULL,
  `phone`              VARCHAR(20) NOT NULL,
  `city`               VARCHAR(32) NOT NULL DEFAULT '',
  `service_areas`      JSON DEFAULT NULL,
  `service_types`      JSON DEFAULT NULL,
  `experiences`        TEXT,
  `specialties`        JSON DEFAULT NULL,
  `status`             VARCHAR(16) NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/REVIEWING/APPROVED/REJECTED',
  `reject_reason`      VARCHAR(255) DEFAULT NULL,
  `submitted_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `reviewed_at`        DATETIME DEFAULT NULL,
  `reviewer_id`        BIGINT UNSIGNED DEFAULT NULL COMMENT 'admin_user.id',
  `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`, `submitted_at`),
  KEY `idx_provider` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务师入驻申请（审核时限 3 个工作日）';

CREATE TABLE `provider_certification` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `provider_id`  BIGINT UNSIGNED NOT NULL,
  `cert_type`    VARCHAR(20) NOT NULL COMMENT 'REAL_NAME/FACE/HEALTH_CERT/NO_CRIMINAL/QUALIFICATION/PLATFORM_TRAINING（五重认证+职业资格）',
  `status`       VARCHAR(16) NOT NULL DEFAULT 'NOT_SUBMITTED' COMMENT 'NOT_SUBMITTED/PENDING/PASSED/REJECTED/EXPIRED',
  `cert_no`      VARCHAR(64) DEFAULT NULL COMMENT '证书编号',
  `image_urls`   JSON DEFAULT NULL COMMENT '证照照片',
  `issued_at`    DATE DEFAULT NULL,
  `expire_at`    DATE DEFAULT NULL COMMENT '到期自动置 EXPIRED（如健康证）',
  `remark`       VARCHAR(255) DEFAULT NULL,
  `submitted_at` DATETIME DEFAULT NULL,
  `audited_at`   DATETIME DEFAULT NULL,
  `auditor_id`   BIGINT UNSIGNED DEFAULT NULL COMMENT 'admin_user.id',
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_type` (`provider_id`, `cert_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务师认证（MEDICAL 类订单派单校验 QUALIFICATION）';

CREATE TABLE `training_course` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title`           VARCHAR(64) NOT NULL,
  `category`        VARCHAR(16) NOT NULL COMMENT 'FEEDING/GROOMING/MEDICAL/EMERGENCY',
  `cover_url`       VARCHAR(512) DEFAULT NULL,
  `duration_minutes` INT NOT NULL DEFAULT 0,
  `content`         JSON DEFAULT NULL COMMENT '课程内容（图文/题目）',
  `sort`            INT NOT NULL DEFAULT 0,
  `status`          TINYINT NOT NULL DEFAULT 1,
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台培训课程';

CREATE TABLE `provider_exam_record` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `provider_id` BIGINT UNSIGNED NOT NULL,
  `course_id`   BIGINT UNSIGNED NOT NULL,
  `score`       TINYINT UNSIGNED NOT NULL COMMENT '百分制，≥80 通过',
  `passed`      TINYINT(1) NOT NULL DEFAULT 0,
  `attempt_no`  INT NOT NULL DEFAULT 1,
  `answers`     JSON DEFAULT NULL COMMENT '答卷 {questionId, optionKey}[]',
  `passed_at`   DATETIME DEFAULT NULL,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_provider_course` (`provider_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='培训考试记录（多次补考）';

CREATE TABLE `provider_agreement` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `provider_id`      BIGINT UNSIGNED NOT NULL,
  `agreement_type`   VARCHAR(20) NOT NULL COMMENT 'COOPERATION/SERVICE_STANDARD/LIABILITY',
  `signature_url`    VARCHAR(512) NOT NULL COMMENT '电子签名图片',
  `contract_file_url` VARCHAR(512) DEFAULT NULL COMMENT '生成的协议文件',
  `signed_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_type` (`provider_id`, `agreement_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电子协议签署';

CREATE TABLE `provider_schedule` (
  `provider_id`   BIGINT UNSIGNED NOT NULL COMMENT '一服务师一配置',
  `weekdays`      JSON DEFAULT NULL COMMENT '可接单星期 [1..7]',
  `time_ranges`   JSON DEFAULT NULL COMMENT '时段 [{"start":"09:00","end":"18:00"}]',
  `service_areas` JSON DEFAULT NULL,
  `service_types` JSON DEFAULT NULL,
  `accept_mode`   VARCHAR(8) NOT NULL DEFAULT 'MANUAL' COMMENT 'AUTO/MANUAL',
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务师排班（可用时间窗 = 排班 ∩ 已占单，capacity 由运营配置）';

CREATE TABLE `provider_bank_card` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `provider_id`    BIGINT UNSIGNED NOT NULL,
  `holder_name`    VARCHAR(32) NOT NULL,
  `bank_name`      VARCHAR(64) NOT NULL,
  `card_no_cipher` VARCHAR(512) NOT NULL COMMENT '卡号密文',
  `card_no_masked` VARCHAR(32) NOT NULL COMMENT '尾号展示',
  `phone`          VARCHAR(20) NOT NULL DEFAULT '' COMMENT '预留手机号',
  `is_default`     TINYINT(1) NOT NULL DEFAULT 0,
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at`     DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_provider` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务师银行卡（接口文档缺失管理端点，表先建好）';

CREATE TABLE `provider_favorite` (
  `user_id`     BIGINT UNSIGNED NOT NULL,
  `provider_id` BIGINT UNSIGNED NOT NULL,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏/候选专属服务师';

CREATE TABLE `provider_punishment` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `provider_id`   BIGINT UNSIGNED NOT NULL,
  `type`          VARCHAR(16) NOT NULL COMMENT 'WARNING/FINE/SUSPEND/BAN/REWARD',
  `reason`        VARCHAR(255) NOT NULL,
  `amount`        INT DEFAULT NULL COMMENT '罚款/奖励金额（分）',
  `duration_days` INT DEFAULT NULL COMMENT '封禁天数（SUSPEND/BAN）',
  `operator_id`   BIGINT UNSIGNED NOT NULL COMMENT 'admin_user.id',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_provider` (`provider_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务师奖惩记录';

-- =====================================================================
-- 5. 订单域
-- =====================================================================

CREATE TABLE `orders` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no`           VARCHAR(32) NOT NULL COMMENT '展示单号',
  `user_id`            BIGINT UNSIGNED NOT NULL,
  `service_id`         BIGINT UNSIGNED NOT NULL,
  `service_name`       VARCHAR(64) NOT NULL COMMENT '下单时快照',
  `provider_id`        BIGINT UNSIGNED DEFAULT NULL COMMENT '接单后回填',
  `status`             VARCHAR(16) NOT NULL DEFAULT 'PENDING_PAY'
                      COMMENT 'PENDING_PAY/PENDING_ACCEPT/ACCEPTED/DEPARTED/ARRIVED/SERVING/PENDING_CONFIRM/SETTLING/REVIEWED/FINISHED/CANCELLED/REFUNDING/REFUNDED/DISPUTED',
  `start_date`         DATE NOT NULL,
  `days`               TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '连续预约天数',
  `time_slot`          VARCHAR(16) NOT NULL DEFAULT '' COMMENT '首日时段展示快照，如 14:00-15:00',
  `remark`             VARCHAR(512) DEFAULT NULL,
  -- 地址快照（防用户改地址影响历史单；打卡定位偏差用快照坐标比对）
  `contact_name`       VARCHAR(32) NOT NULL,
  `contact_phone`      VARCHAR(20) NOT NULL,
  `city`               VARCHAR(32) NOT NULL DEFAULT '',
  `district`           VARCHAR(32) NOT NULL DEFAULT '',
  `community`          VARCHAR(64) NOT NULL,
  `detail`             VARCHAR(128) NOT NULL,
  `longitude`          DECIMAL(10,6) NOT NULL,
  `latitude`           DECIMAL(10,6) NOT NULL,
  `entry_mode`         VARCHAR(16) NOT NULL COMMENT 'SMART_LOCK/PROPERTY/KEY_DEPOSIT/AT_HOME',
  -- 金额（分）
  `base_amount`        INT NOT NULL DEFAULT 0,
  `extra_amount`       INT NOT NULL DEFAULT 0 COMMENT '加价（多宠/体重/距离）',
  `discount_amount`    INT NOT NULL DEFAULT 0,
  `total_amount`       INT NOT NULL DEFAULT 0 COMMENT '应付',
  `pay_amount`         INT NOT NULL DEFAULT 0 COMMENT '实付',
  -- 关键时间点
  `pay_expire_at`      DATETIME DEFAULT NULL COMMENT '15 分钟支付倒计时',
  `pay_time`           DATETIME DEFAULT NULL,
  `dispatch_deadline`  DATETIME DEFAULT NULL COMMENT '派单超时（支付后 30 分钟，超时自动退款）',
  `confirm_deadline`   DATETIME DEFAULT NULL COMMENT '报告提交后 72h 自动确认',
  `confirmed_at`       DATETIME DEFAULT NULL,
  `review_deadline`    DATETIME DEFAULT NULL COMMENT '评价窗口截止（confirmed_at + 72h，超时未评由定时任务归档 FINISHED）',
  `cancel_time`        DATETIME DEFAULT NULL,
  `cancel_reason_code` VARCHAR(32) DEFAULT NULL COMMENT '字典 cancel_reason',
  `cancel_remark`      VARCHAR(255) DEFAULT NULL,
  `penalty_amount`     INT NOT NULL DEFAULT 0 COMMENT '用户 4h 内取消的违约金（给服务师）',
  `refund_amount`      INT NOT NULL DEFAULT 0 COMMENT '累计退款',
  `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at`         DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  KEY `idx_provider_created` (`provider_id`, `created_at`),
  KEY `idx_status_start` (`status`, `start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表';

CREATE TABLE `order_pet` (
  `id`       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT UNSIGNED NOT NULL,
  `pet_id`   BIGINT UNSIGNED NOT NULL,
  `pet_name` VARCHAR(32) NOT NULL COMMENT '快照',
  `species`  VARCHAR(16) NOT NULL COMMENT '快照',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_pet` (`order_id`, `pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单-宠物关联';

CREATE TABLE `order_day` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`   BIGINT UNSIGNED NOT NULL,
  `date`       DATE NOT NULL,
  `slot_begin` TIME NOT NULL COMMENT '当天时段起（改约按天独立调整）',
  `slot_end`   TIME NOT NULL,
  `status`     VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SERVING/DONE/PAUSED/CANCELLED',
  `paused`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '暂停一天；与 status=PAUSED 等价，冗余便于查询',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_date` (`order_id`, `date`),
  KEY `idx_date_status` (`date`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='多日订单子项（单日订单也生成 1 行，报告/记录统一挂 day）';

CREATE TABLE `order_dispatch` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`         BIGINT UNSIGNED NOT NULL,
  `provider_id`      BIGINT UNSIGNED NOT NULL,
  `action`           VARCHAR(16) NOT NULL COMMENT 'DISPATCH/ACCEPT/REJECT/REASSIGN/TIMEOUT',
  `remark`           VARCHAR(255) DEFAULT NULL,
  `operator_id`      BIGINT UNSIGNED DEFAULT NULL COMMENT '后台派单的管理员，服务师端动作为空',
  `response_deadline` DATETIME DEFAULT NULL COMMENT '指派后 15 分钟响应截止',
  `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_provider_action` (`provider_id`, `action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='派单/接单/改派历史（改派无服务师可接则退款+补偿）';

CREATE TABLE `order_status_log` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT UNSIGNED NOT NULL,
  `from_status`   VARCHAR(16) DEFAULT NULL,
  `to_status`     VARCHAR(16) NOT NULL,
  `operator_type` VARCHAR(8) NOT NULL COMMENT 'USER/PROVIDER/ADMIN/SYSTEM',
  `operator_id`   BIGINT UNSIGNED DEFAULT NULL,
  `remark`        VARCHAR(255) DEFAULT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单状态流转日志（进度流 ProgressNodeVO 数据源）';

CREATE TABLE `service_record` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`        BIGINT UNSIGNED NOT NULL,
  `day_id`          BIGINT UNSIGNED NOT NULL COMMENT 'order_day.id；打卡/照片/报告按天归组，照片流按天查询',
  `provider_id`     BIGINT UNSIGNED NOT NULL,
  `type`            VARCHAR(8) NOT NULL COMMENT 'CHECKIN/PHOTO/VIDEO/NOTE',
  `sop_item_code`   VARCHAR(32) DEFAULT NULL COMMENT '对应 sop_item.code',
  `media_url`       VARCHAR(512) DEFAULT NULL,
  `watermarked`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '客户端合成时间+定位水印',
  `longitude`       DECIMAL(10,6) DEFAULT NULL,
  `latitude`        DECIMAL(10,6) DEFAULT NULL,
  `distance_meters` INT DEFAULT NULL COMMENT '打卡与地址坐标偏差（米），仅 CHECKIN',
  `deviation_alert` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '偏差 >500m 触发告警',
  `remark`          VARCHAR(255) DEFAULT NULL,
  `captured_at`     DATETIME NOT NULL COMMENT '拍摄/打卡时间',
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_day` (`order_id`, `day_id`, `type`),
  KEY `idx_provider` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务过程记录（打卡+照片/视频，保留 ≥1 年）';

CREATE TABLE `service_report` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT UNSIGNED NOT NULL,
  `day_id`        BIGINT UNSIGNED NOT NULL COMMENT '接口 dayId 可空，后端归到当天子项后落库恒非空',
  `provider_id`   BIGINT UNSIGNED NOT NULL,
  `eating`        VARCHAR(16) NOT NULL COMMENT 'NORMAL/LESS/MORE/REFUSED',
  `drinking`      VARCHAR(16) NOT NULL COMMENT 'NORMAL/LESS/MORE',
  `excretion`     VARCHAR(16) NOT NULL COMMENT 'NORMAL/ABNORMAL',
  `spirit`        VARCHAR(16) NOT NULL COMMENT 'GOOD/NORMAL/LOW',
  `abnormalities` JSON DEFAULT NULL COMMENT '[{code,description,suggestMedical}]，异常项高亮提示建议就医',
  `photos`        JSON DEFAULT NULL,
  `summary`       VARCHAR(1024) DEFAULT NULL,
  `submitted_at`  DATETIME NOT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_day` (`day_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务报告（每天一份，uk_day 保证唯一）';

CREATE TABLE `order_alert` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`   BIGINT UNSIGNED NOT NULL,
  `alert_type` VARCHAR(24) NOT NULL COMMENT 'TIMEOUT_NO_ACCEPT/CHECKIN_DEVIATION/PROVIDER_EXCEPTION/USER_COMPLAINT/PAYMENT_ABNORMAL',
  `level`      VARCHAR(8) NOT NULL DEFAULT 'LOW' COMMENT 'LOW/MEDIUM/HIGH',
  `alert_text` VARCHAR(255) NOT NULL,
  `handled`    TINYINT(1) NOT NULL DEFAULT 0,
  `handler_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'admin_user.id',
  `handled_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type_handled` (`alert_type`, `handled`, `created_at`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='异常订单告警';

CREATE TABLE `virtual_phone` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT UNSIGNED NOT NULL,
  `provider_id`   BIGINT UNSIGNED NOT NULL,
  `virtual_phone` VARCHAR(20) NOT NULL COMMENT '隐私中间号',
  `expire_at`     DATETIME NOT NULL,
  `status`        VARCHAR(8) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/EXPIRED',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单虚拟号绑定（双方不可见真实号码）';

-- =====================================================================
-- 6. 支付与资金
-- =====================================================================

CREATE TABLE `payment` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `out_trade_no`   VARCHAR(32) NOT NULL COMMENT '商户单号',
  `biz_type`       VARCHAR(8) NOT NULL COMMENT 'ORDER/DEPOSIT（回调按此路由，补接口文档缺口）',
  `biz_id`         BIGINT UNSIGNED NOT NULL COMMENT 'orders.id 或 provider.id',
  `provider_id`    BIGINT UNSIGNED DEFAULT NULL COMMENT 'DEPOSIT 时',
  `prepay_id`      VARCHAR(64) DEFAULT NULL,
  `transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '微信支付订单号',
  `amount`         INT NOT NULL COMMENT '支付金额（分）',
  `status`         VARCHAR(16) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/SUCCESS/FAILED/CLOSED',
  `paid_at`        DATETIME DEFAULT NULL,
  `callback_raw`   JSON DEFAULT NULL COMMENT '回调原文（验签后落档）',
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
  KEY `idx_biz` (`biz_type`, `biz_id`),
  KEY `idx_transaction` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付流水（含订单支付与保证金缴纳）';

CREATE TABLE `refund` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `out_refund_no`   VARCHAR(32) NOT NULL,
  `order_id`        BIGINT UNSIGNED NOT NULL,
  `payment_id`      BIGINT UNSIGNED NOT NULL,
  `wechat_refund_id` VARCHAR(64) DEFAULT NULL,
  `source`          VARCHAR(16) NOT NULL DEFAULT 'USER_APPLY' COMMENT 'USER_APPLY/ADMIN_EXECUTE/SYSTEM_CANCEL',
  `amount`          INT NOT NULL COMMENT '退款金额（分）',
  `reason_code`     VARCHAR(32) DEFAULT NULL COMMENT '字典 refund_reason',
  `remark`          VARCHAR(255) DEFAULT NULL,
  `status`          VARCHAR(16) NOT NULL DEFAULT 'APPLIED' COMMENT 'APPLIED/APPROVED/REJECTED/PROCESSING/SUCCESS/FAILED',
  `applied_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `audited_at`      DATETIME DEFAULT NULL,
  `auditor_id`      BIGINT UNSIGNED DEFAULT NULL,
  `done_at`         DATETIME DEFAULT NULL,
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_out_refund_no` (`out_refund_no`),
  KEY `idx_order` (`order_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退款单（统一两条管理端路径：approve 审批本表，execute 生成 ADMIN_EXECUTE 单）';

CREATE TABLE `settlement` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT UNSIGNED NOT NULL,
  `provider_id`   BIGINT UNSIGNED NOT NULL,
  `order_amount`  INT NOT NULL COMMENT '订单实付（分）',
  `platform_fee`  INT NOT NULL COMMENT '平台服务费（抽佣 15%-20%）',
  `net_amount`    INT NOT NULL COMMENT '服务师实结（分）',
  `status`        VARCHAR(8) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SETTLED/FROZEN；确认后 T+1 入余额，申诉可 FROZEN',
  `settled_at`    DATETIME DEFAULT NULL,
  `frozen_reason` VARCHAR(255) DEFAULT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order` (`order_id`),
  KEY `idx_provider` (`provider_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务师结算明细（一单一行）';

CREATE TABLE `settlement_rule` (
  `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `city`                VARCHAR(32) DEFAULT NULL COMMENT 'NULL = 全局默认规则；按城市覆盖',
  `commission_rate`     DECIMAL(5,4) NOT NULL DEFAULT 0.1500 COMMENT '抽佣比例 0.15 = 15%',
  `settle_cycle`        VARCHAR(8) NOT NULL DEFAULT 'T+1',
  `min_withdraw_amount` INT NOT NULL DEFAULT 10000 COMMENT '最低提现（分）',
  `updated_by`          BIGINT UNSIGNED DEFAULT NULL,
  `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分账规则';

CREATE TABLE `provider_wallet` (
  `provider_id`       BIGINT UNSIGNED NOT NULL,
  `balance`           INT NOT NULL DEFAULT 0 COMMENT '可提现余额（分）',
  `pending_settlement` INT NOT NULL DEFAULT 0 COMMENT '待结算（分）',
  `frozen_deposit`    INT NOT NULL DEFAULT 0 COMMENT '冻结保证金（分），与 deposit_ledger 对账',
  `total_income`      INT NOT NULL DEFAULT 0 COMMENT '累计收入（分）',
  `version`           INT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  `updated_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务师钱包（余额只由 provider_wallet_flow 驱动变更）';

CREATE TABLE `provider_wallet_flow` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `provider_id`   BIGINT UNSIGNED NOT NULL,
  `biz_type`      VARCHAR(16) NOT NULL COMMENT 'SETTLEMENT_IN/WITHDRAWAL_OUT/FINE/REWARD/COMPENSATION',
  `amount`        INT NOT NULL COMMENT '正入账 负出账（分）',
  `balance_after` INT NOT NULL COMMENT '变动后余额（分）',
  `biz_id`        BIGINT UNSIGNED DEFAULT NULL COMMENT 'settlement.id / withdrawal.id / punishment.id',
  `remark`        VARCHAR(255) DEFAULT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_provider_time` (`provider_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钱包流水（资金可追溯，每笔变更一行）';

CREATE TABLE `withdrawal` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `withdrawal_no` VARCHAR(32) NOT NULL,
  `provider_id`   BIGINT UNSIGNED NOT NULL,
  `bank_card_id`  BIGINT UNSIGNED NOT NULL,
  `amount`        INT NOT NULL COMMENT '提现金额（分），每周一次',
  `status`        VARCHAR(16) NOT NULL DEFAULT 'APPLIED' COMMENT 'APPLIED/APPROVED/PAID/REJECTED',
  `reject_reason` VARCHAR(255) DEFAULT NULL,
  `applied_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `audited_at`    DATETIME DEFAULT NULL,
  `auditor_id`    BIGINT UNSIGNED DEFAULT NULL,
  `paid_at`       DATETIME DEFAULT NULL,
  `transfer_no`   VARCHAR(64) DEFAULT NULL COMMENT '商家转账单号',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_withdrawal_no` (`withdrawal_no`),
  KEY `idx_status` (`status`, `applied_at`),
  KEY `idx_provider` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='提现单';

CREATE TABLE `deposit_ledger` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `provider_id`   BIGINT UNSIGNED NOT NULL,
  `type`          VARCHAR(8) NOT NULL COMMENT 'PAY/FREEZE/REFUND/DEDUCT；退出满 30 天无纠纷可退',
  `amount`        INT NOT NULL COMMENT '金额（分）',
  `balance_after` INT NOT NULL COMMENT '变动后冻结额（分）',
  `order_id`      BIGINT UNSIGNED DEFAULT NULL COMMENT 'DEDUCT 关联订单',
  `payment_id`    BIGINT UNSIGNED DEFAULT NULL,
  `remark`        VARCHAR(255) DEFAULT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_provider` (`provider_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='保证金台账';

CREATE TABLE `pay_reconciliation` (
  `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `bill_date`           DATE NOT NULL,
  `pay_amount`          INT NOT NULL DEFAULT 0 COMMENT '支付流水（分）',
  `refund_amount`       INT NOT NULL DEFAULT 0 COMMENT '退款流水（分）',
  `platform_fee`        INT NOT NULL DEFAULT 0,
  `provider_settlement` INT NOT NULL DEFAULT 0,
  `diff_amount`         INT NOT NULL DEFAULT 0 COMMENT '与微信账单差异（分）',
  `status`              VARCHAR(8) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/MATCHED/DIFF',
  `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bill_date` (`bill_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='微信支付日对账';

-- =====================================================================
-- 7. 评价与客诉
-- =====================================================================

CREATE TABLE `review` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`           BIGINT UNSIGNED NOT NULL COMMENT '一单一评，完单后 72h 内',
  `user_id`            BIGINT UNSIGNED NOT NULL,
  `provider_id`        BIGINT UNSIGNED NOT NULL,
  `score_professional` TINYINT UNSIGNED NOT NULL COMMENT '1-5',
  `score_punctual`     TINYINT UNSIGNED NOT NULL,
  `score_communication` TINYINT UNSIGNED NOT NULL,
  `score_care`         TINYINT UNSIGNED NOT NULL,
  `content`            VARCHAR(1024) DEFAULT NULL,
  `images`             JSON DEFAULT NULL,
  `anonymous`          TINYINT(1) NOT NULL DEFAULT 0,
  `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order` (`order_id`),
  KEY `idx_provider_time` (`provider_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户评价（评分冗余到 provider，评价后异步重算）';

CREATE TABLE `complaint` (
  `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `complaint_no`      VARCHAR(32) NOT NULL,
  `order_id`          BIGINT UNSIGNED NOT NULL,
  `user_id`           BIGINT UNSIGNED NOT NULL,
  `provider_id`       BIGINT UNSIGNED DEFAULT NULL,
  `type`              VARCHAR(16) NOT NULL COMMENT 'PET_ABNORMAL/ENV_ABNORMAL/SERVICE_QUALITY/REFUND_DISPUTE/OTHER',
  `title`             VARCHAR(128) NOT NULL DEFAULT '',
  `description`       TEXT NOT NULL,
  `evidence_urls`     JSON DEFAULT NULL,
  `status`            VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/RESOLVED/CLOSED；客服 24h 介入',
  `handler_id`        BIGINT UNSIGNED DEFAULT NULL COMMENT 'admin_user.id',
  `resolution`        TEXT COMMENT '处理与赔付方案',
  `compensation_amount` INT DEFAULT NULL COMMENT '赔付金额（分）',
  `advance_payment`   INT DEFAULT NULL COMMENT '先行赔付金额（分）',
  `freeze_settlement` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '冻结该单结算',
  `created_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_complaint_no` (`complaint_no`),
  KEY `idx_status` (`status`, `created_at`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客诉工单';

CREATE TABLE `complaint_timeline` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `complaint_id`  BIGINT UNSIGNED NOT NULL,
  `action`        VARCHAR(64) NOT NULL COMMENT 'CREATE/ASSIGN/RESOLVE/ADVANCE_PAYMENT/CLOSE/...',
  `operator_id`   BIGINT UNSIGNED DEFAULT NULL COMMENT 'admin_user.id',
  `operator_name` VARCHAR(64) NOT NULL DEFAULT '',
  `remark`        VARCHAR(512) DEFAULT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_complaint` (`complaint_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单流转时间线';

-- =====================================================================
-- 8. 管理后台
-- =====================================================================

CREATE TABLE `admin_user` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username`      VARCHAR(32) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL COMMENT 'bcrypt',
  `real_name`     VARCHAR(32) NOT NULL DEFAULT '',
  `role_id`       BIGINT UNSIGNED NOT NULL,
  `status`        VARCHAR(8) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
  `last_login_at` DATETIME DEFAULT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at`    DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台员工（工单指派 assigneeId 来源，接口文档缺列表端点）';

CREATE TABLE `admin_role` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(32) NOT NULL COMMENT '如 CUSTOMER_SERVICE/OPERATOR/FINANCE/ADMIN',
  `name`        VARCHAR(32) NOT NULL,
  `permissions` JSON DEFAULT NULL COMMENT '权限编码数组',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台角色';

CREATE TABLE `audit_log` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `operator_id`   BIGINT UNSIGNED NOT NULL,
  `operator_name` VARCHAR(64) NOT NULL DEFAULT '',
  `role_code`     VARCHAR(32) NOT NULL DEFAULT '',
  `action`        VARCHAR(64) NOT NULL COMMENT '如 VIEW_ENTRY_CREDENTIAL/REFUND_EXECUTE/BAN_PROVIDER',
  `target_type`   VARCHAR(32) NOT NULL COMMENT 'order/user/provider/complaint/...',
  `target_id`     BIGINT UNSIGNED NOT NULL,
  `detail`        TEXT COMMENT '操作详情 JSON',
  `ip`            VARCHAR(64) DEFAULT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_operator_time` (`operator_id`, `created_at`),
  KEY `idx_action_time` (`action`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='敏感操作审计日志（查看入户信息/退款/封禁全量留痕）';
