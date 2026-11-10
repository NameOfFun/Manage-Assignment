

-- Sign in (password 12345678 for all - Sign-in requires 8-50 characters;
-- running this script again also resets the demo passwords to 12345678):
--   admin.demo    -> Admin Dashboard; /AMS/classes : all classes & all subjects
--   expert.demo   -> /AMS/classes : manages SWP391, PRJ301, SWT301
--   expert2.demo  -> /AMS/classes : manages PRN212, MAS291
--   teacher.demo  -> /AMS/classes : own classes, manage student roster
-- ============================================================================
DO $$
DECLARE
    pw_hash   text := '$2a$10$7CJLpK2MYctHMGdiJtkEuOB2SYjDJeI3Ur1Lnyhc1WpXzbrB48Xja';  -- "12345678" (jBCrypt)
    r_admin   int;
    r_expert  int;
    r_teacher int;
    r_student int;
    u_expert  int;
    u_expert2 int;
    u_t1      int;
    u_t2      int;
    c_main    int;
    c_second  int;
    rec       record;
    i         int;
    names     text[] := ARRAY['Nguyễn Duy Nhật Anh', 'Lê Duy Hưng', 'Đỗ Chí Hào', 'Phạm Minh Châu',
                              'Trần Bảo Ngọc', 'Vũ Đức Thắng', 'Hoàng Thu Trang', 'Bùi Gia Huy',
                              'Đặng Khánh Linh', 'Ngô Quốc Bảo', 'Phan Thảo Vy', 'Lý Hải Nam'];
BEGIN
    -- Roles live in "Settings". The app expects Admin = settingid 1 and Student = settingid 2
    -- (AdminDashboard, UserServlet and registration use those ids), so missing roles are
    -- created with those ids when they are free.
    PERFORM setval(pg_get_serial_sequence('"Settings"', 'settingid'),
                   GREATEST((SELECT COALESCE(MAX(settingid), 0) FROM "Settings"), 1),
                   (SELECT COUNT(*) > 0 FROM "Settings"));
    FOR rec IN SELECT * FROM (VALUES (1, 'Admin', 'ADMIN'), (2, 'Student', 'STUDENT'),
                                     (3, 'Teacher', 'TEACHER'), (4, 'Expert', 'EXPERT')) AS r(id, name, val)
    LOOP
        IF NOT EXISTS (SELECT 1 FROM "Settings" WHERE UPPER(type) = 'ROLE' AND UPPER(name) = UPPER(rec.name)) THEN
            IF NOT EXISTS (SELECT 1 FROM "Settings" WHERE settingid = rec.id) THEN
                INSERT INTO "Settings"(settingid, name, type, value, priority, status)
                VALUES (rec.id, rec.name, 'Role', rec.val, rec.id, 'Active');
            ELSE
                INSERT INTO "Settings"(name, type, value, priority, status)
                VALUES (rec.name, 'Role', rec.val, rec.id, 'Active');
            END IF;
            PERFORM setval(pg_get_serial_sequence('"Settings"', 'settingid'), (SELECT MAX(settingid) FROM "Settings"));
        END IF;
    END LOOP;
    SELECT settingid INTO r_admin FROM "Settings" WHERE UPPER(type) = 'ROLE' AND UPPER(name) = 'ADMIN' ORDER BY settingid LIMIT 1;
    SELECT settingid INTO r_expert FROM "Settings" WHERE UPPER(type) = 'ROLE' AND UPPER(name) = 'EXPERT' ORDER BY settingid LIMIT 1;
    SELECT settingid INTO r_teacher FROM "Settings" WHERE UPPER(type) = 'ROLE' AND UPPER(name) = 'TEACHER' ORDER BY settingid LIMIT 1;
    SELECT settingid INTO r_student FROM "Settings" WHERE UPPER(type) = 'ROLE' AND UPPER(name) = 'STUDENT' ORDER BY settingid LIMIT 1;

    -- ---------- subjects ----------
    FOR rec IN SELECT * FROM (VALUES
            ('SWP391', 'SWP391 - Software Project'),
            ('PRJ301', 'PRJ301 - Java Web'),
            ('PRN212', 'PRN212 - .NET Desktop'),
            ('SWT301', 'SWT301 - Software Testing'),
            ('MAS291', 'MAS291 - Statistics & Probability')) AS s(code, full_name)
    LOOP
        IF NOT EXISTS (SELECT 1 FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND (value = rec.code OR name = rec.full_name)) THEN
            INSERT INTO "Settings"(name, type, value, priority, status)
            VALUES (rec.full_name, 'Subject', rec.code, 1, 'Active');
        END IF;
    END LOOP;

    -------- semestesr ------------
    INSERT INTO "Settings" (name, type, value, priority, status)
    VALUES ('Fall', 'Semester', 'FALL', 1, 'Active'),
           ('Spring', 'Semester', 'SPRING', 2, 'Active'),
           ('Summer', 'Semester', 'SUMMER', 3, 'Active');

    -- ---------- staff ----------
    INSERT INTO "User"(role_id, username, password_hash, email, full_name, status, created_at)
    VALUES (r_admin, 'admin.demo', pw_hash, 'admin.demo@ams.local', 'Quản Trị Viên', 'Active', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO "User"(role_id, username, password_hash, email, full_name, status, created_at)
    VALUES (r_expert, 'expert.demo', pw_hash, 'expert.demo@ams.local', 'Trần Thị Chuyên Gia', 'Active', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO "User"(role_id, username, password_hash, email, full_name, status, created_at)
    VALUES (r_expert, 'expert2.demo', pw_hash, 'expert2.demo@ams.local', 'Lê Văn Chuyên Gia', 'Active', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO "User"(role_id, username, password_hash, email, full_name, status, created_at)
    VALUES (r_teacher, 'teacher.demo', pw_hash, 'teacher.demo@ams.local', 'Nguyễn Trung Kiên', 'Active', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO "User"(role_id, username, password_hash, email, full_name, status, created_at)
    VALUES (r_teacher, 'teacher2.demo', pw_hash, 'teacher2.demo@ams.local', 'Phạm Thu Hà', 'Active', NOW())
    ON CONFLICT DO NOTHING;
    SELECT user_id INTO u_expert FROM "User" WHERE username = 'expert.demo';
    SELECT user_id INTO u_expert2 FROM "User" WHERE username = 'expert2.demo';
    SELECT user_id INTO u_t1 FROM "User" WHERE username = 'teacher.demo';
    SELECT user_id INTO u_t2 FROM "User" WHERE username = 'teacher2.demo';

    -- ---------- subject experts ----------
    -- expert.demo manages SWP391, PRJ301, SWT301
    DELETE FROM "SubjectExpert" WHERE user_id IN (u_expert, u_expert2);
    INSERT INTO "SubjectExpert"(subject_id, user_id)
    SELECT s.settingid, u_expert
    FROM "Settings" s
    WHERE UPPER(s.type) = 'SUBJECT' AND s.value IN ('SWP391', 'PRJ301', 'SWT301')
    ON CONFLICT DO NOTHING;

    -- expert2.demo manages PRN212, MAS291
    INSERT INTO "SubjectExpert"(subject_id, user_id)
    SELECT s.settingid, u_expert2
    FROM "Settings" s
    WHERE UPPER(s.type) = 'SUBJECT' AND s.value IN ('PRN212', 'MAS291')
    ON CONFLICT DO NOTHING;

    -- ---------- students student01 .. student12 ----------
    FOR i IN 1..12 LOOP
        INSERT INTO "User"(role_id, username, password_hash, email, full_name, status, created_at)
        VALUES (r_student, 'student' || LPAD(i::text, 2, '0'), pw_hash,
                'student' || LPAD(i::text, 2, '0') || '@ams.local', names[i], 'Active', NOW())
        ON CONFLICT DO NOTHING;
    END LOOP;

    -- Keep demo passwords valid for the 8-50 character rule (older runs used "123456")
    UPDATE "User" SET password_hash = pw_hash
    WHERE username IN ('admin.demo', 'expert.demo', 'expert2.demo', 'teacher.demo', 'teacher2.demo', 'student.demo', 'student.demo2')
       OR username ~ '^student[0-9]{2}$';

    -- ---------- classes ----------
    FOR rec IN SELECT * FROM (VALUES
            ('SE2051-NET', 'SWP391', 1, 'Fall',   2026),
            ('SE2052-NET', 'SWP391', 1, 'Fall',   2026),
            ('SE2053-NET', 'SWP391', 2, 'Fall',   2026),
            ('SE2054-JS',  'PRJ301', 2, 'Fall',   2026),
            ('SE2041-NET', 'SWP391', 1, 'Summer', 2026),
            ('SE2042-NET', 'PRJ301', 1, 'Summer', 2026),
            ('SE2031-NET', 'SWP391', 2, 'Spring', 2026),
            ('SE2032-JS',  'PRN212', 1, 'Spring', 2026),
            ('SE1951-NET', 'SWT301', 2, 'Fall',   2025),
            ('SE1952-NET', 'MAS291', 1, 'Fall',   2025),
            ('SE1941-NET', 'PRN212', 2, 'Summer', 2025),
            ('SE1931-NET', 'SWP391', 1, 'Spring', 2025),
            ('SE1932-JS',  'PRJ301', 2, 'Spring', 2025),
            ('SE1921-NET', 'SWT301', 1, 'Fall',   2024)) AS c(code, subj, teacher_no, sem, yr)
    LOOP
        INSERT INTO "Class"(subject_id, teacher_id, class_code, semester, year)
        SELECT s.settingid, CASE rec.teacher_no WHEN 1 THEN u_t1 ELSE u_t2 END, rec.code, rec.sem, rec.yr
        FROM "Settings" s
        WHERE UPPER(s.type) = 'SUBJECT' AND s.value = rec.subj
        ORDER BY s.settingid LIMIT 1
        ON CONFLICT DO NOTHING;
    END LOOP;

    -- ---------- enrolments ----------
    SELECT class_id INTO c_main FROM "Class" WHERE class_code = 'SE2051-NET';
    SELECT class_id INTO c_second FROM "Class" WHERE class_code = 'SE2052-NET';
    INSERT INTO "ClassStudent"(class_id, student_id, enroll_date)
    SELECT c_main, u.user_id, NOW() - INTERVAL '20 days'
    FROM "User" u WHERE u.username ~ '^student[0-9]{2}$'
    ON CONFLICT DO NOTHING;
    INSERT INTO "ClassStudent"(class_id, student_id, enroll_date)
    SELECT c_second, u.user_id, NOW() - INTERVAL '10 days'
    FROM "User" u WHERE u.username IN ('student01', 'student02', 'student03', 'student04', 'student05')
    ON CONFLICT DO NOTHING;

    RAISE NOTICE 'Class Management demo data ready. Sign in as admin.demo, expert.demo or teacher.demo (password 12345678).';
END $$;
