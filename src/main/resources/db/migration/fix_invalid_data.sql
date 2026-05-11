-- 修复数据库中的无效数据
-- 此脚本用于修复因表结构变更导致的数据不兼容问题

-- 1. 修复 final_grade 表中的无效 grade_level 值
-- 将无效的枚举值设置为 NULL 或默认值
UPDATE final_grade 
SET grade_level = NULL 
WHERE grade_level NOT IN ('EXCELLENT', 'GOOD', 'AVERAGE', 'PASS', 'FAIL');

-- 2. 修复 score_sheet 表中的无效 created_at 值
-- 将 '0000-00-00 00:00:00' 或 NULL 值更新为当前时间
UPDATE score_sheet 
SET created_at = NOW() 
WHERE created_at IS NULL 
   OR created_at = '0000-00-00 00:00:00' 
   OR created_at < '1970-01-01 00:00:00';

-- 3. 修复 thesis_process 表中的无效 status 值
-- 将无效的枚举值设置为默认值 'INIT'
UPDATE thesis_process 
SET status = 'INIT' 
WHERE status NOT IN (
    'INIT', 
    'TOPIC_SUBMITTED', 
    'TOPIC_APPROVED', 
    'OPENING_SUBMITTED', 
    'OPENING_APPROVED', 
    'MIDTERM_SUBMITTED', 
    'MIDTERM_APPROVED', 
    'FINAL_SUBMITTED', 
    'FINAL_APPROVED', 
    'DEFENSE_SCORED', 
    'COMPLETED'
);

-- 4. 确保所有表的 created_at 字段都有有效值（如果需要）
-- 注意：这只会更新 NULL 或无效值，不会覆盖已有有效值
UPDATE thesis_process 
SET created_at = NOW() 
WHERE created_at IS NULL 
   OR created_at = '0000-00-00 00:00:00' 
   OR created_at < '1970-01-01 00:00:00';

