-- ============================================================================
-- Demo data for the Student Dashboard (optional, for local testing only).
-- Run AFTER databasequery.sql, on database "ams".
--
-- Creates (skips anything that already exists):
--   roles/subjects in "Settings", 1 teacher, 2 students, 2 classes, 2 groups,
--   5 assignments (due at different times, 1 draft), 3 submissions (2 graded).
--
-- Sign in with:  username = student.demo   password = 12345678
-- (Sign-in requires a password of 8-50 characters. Running this script again
--  also resets the demo passwords to 12345678.)
-- Dates are relative to NOW(), so the "upcoming" list stays fresh every time
-- you re-create the data.
-- ============================================================================
DO $$
DECLARE
    rec        record;
    r_teacher  int;
    r_student  int;
    s_swp      int;
    s_prj      int;
    u_teacher  int;
    u_me       int;
    u_friend   int;
    c_swp      int;
    c_prj      int;
    g_team     int;
    g_prj      int;
    a_srs      int;
    a_sds      int;
    a_code     int;
    a_lab      int;
    pw_hash    text := '$2a$10$7CJLpK2MYctHMGdiJtkEuOB2SYjDJeI3Ur1Lnyhc1WpXzbrB48Xja';  -- "12345678" (jBCrypt)
BEGIN
    -- Keep demo passwords valid for the 8-50 character rule (older runs used "123456")
    UPDATE "User" SET password_hash = pw_hash
    WHERE username IN ('student.demo', 'student.demo2', 'teacher.demo');

    IF EXISTS (SELECT 1 FROM "User" WHERE username = 'student.demo') THEN
        RAISE NOTICE 'Demo data already exists - nothing to do.';
        RETURN;
    END IF;

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
    SELECT settingid INTO r_teacher FROM "Settings" WHERE UPPER(type) = 'ROLE' AND UPPER(name) = 'TEACHER' ORDER BY settingid LIMIT 1;
    SELECT settingid INTO r_student FROM "Settings" WHERE UPPER(type) = 'ROLE' AND UPPER(name) = 'STUDENT' ORDER BY settingid LIMIT 1;

    -- Subjects also live in "Settings" (Class.subject_id points there)
    SELECT settingid INTO s_swp FROM "Settings" WHERE type = 'Subject' AND name = 'SWP391 - Software Project';
    IF s_swp IS NULL THEN
        INSERT INTO "Settings"(name, type, value, priority, status)
        VALUES ('SWP391 - Software Project', 'Subject', 'SWP391', 1, 'Active') RETURNING settingid INTO s_swp;
    END IF;
    SELECT settingid INTO s_prj FROM "Settings" WHERE type = 'Subject' AND name = 'PRJ301 - Java Web';
    IF s_prj IS NULL THEN
        INSERT INTO "Settings"(name, type, value, priority, status)
        VALUES ('PRJ301 - Java Web', 'Subject', 'PRJ301', 2, 'Active') RETURNING settingid INTO s_prj;
    END IF;

    -- Users
    -- teacher.demo may already exist (created by seed_class_management_demo.sql)
    INSERT INTO "User"(role_id, username, password_hash, email, full_name, status, created_at)
    VALUES (r_teacher, 'teacher.demo', pw_hash, 'teacher.demo@ams.local', 'Nguyễn Trung Kiên', 'Active', NOW())
    ON CONFLICT DO NOTHING;
    SELECT user_id INTO u_teacher FROM "User" WHERE username = 'teacher.demo';
    INSERT INTO "User"(role_id, username, password_hash, email, full_name, status, created_at)
    VALUES (r_student, 'student.demo', pw_hash, 'student.demo@ams.local', 'Đỗ Quang Minh', 'Active', NOW())
    RETURNING user_id INTO u_me;
    INSERT INTO "User"(role_id, username, password_hash, email, full_name, status, created_at)
    VALUES (r_student, 'student.demo2', pw_hash, 'student.demo2@ams.local', 'Trần Nhật Anh', 'Active', NOW())
    RETURNING user_id INTO u_friend;

    -- Classes + enrolment
    INSERT INTO "Class"(subject_id, teacher_id, class_code, semester, year)
    VALUES (s_swp, u_teacher, 'SE2051-D', 'Fall', 2026) RETURNING class_id INTO c_swp;
    INSERT INTO "Class"(subject_id, teacher_id, class_code, semester, year)
    VALUES (s_prj, u_teacher, 'SE2051-P', 'Summer', 2026) RETURNING class_id INTO c_prj;
    INSERT INTO "ClassStudent"(class_id, student_id, enroll_date) VALUES
        (c_swp, u_me, NOW() - INTERVAL '30 days'),
        (c_prj, u_me, NOW() - INTERVAL '120 days'),
        (c_swp, u_friend, NOW() - INTERVAL '30 days');

    -- A project group in SWP391 with both students
    INSERT INTO "Group"(class_id) VALUES (c_swp) RETURNING group_id INTO g_team;
    INSERT INTO "GroupStudent"(user_id, group_id) VALUES (u_me, g_team), (u_friend, g_team);
    -- Submission.group_id is NOT NULL, so PRJ301 work also needs a group (here: just Minh)
    INSERT INTO "Group"(class_id) VALUES (c_prj) RETURNING group_id INTO g_prj;
    INSERT INTO "GroupStudent"(user_id, group_id) VALUES (u_me, g_prj);

    -- Assignments
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_swp, 'Iteration 1 - SRS document', 'Submit the SRS.', NOW() - INTERVAL '20 days', NOW() - INTERVAL '5 days', 'Published')
    RETURNING assignment_id INTO a_srs;
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_swp, 'Iteration 2 - SDS document', 'Submit the SDS.', NOW() - INTERVAL '7 days', NOW() + INTERVAL '1 day', 'Published')
    RETURNING assignment_id INTO a_sds;
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_swp, 'Iteration 2 - Source code', 'Push code and submit the repo link.', NOW() - INTERVAL '7 days', NOW() + INTERVAL '6 days', 'Published')
    RETURNING assignment_id INTO a_code;
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_prj, 'Lab 3 - Servlet & JSP', 'Build a login page.', NOW() - INTERVAL '40 days', NOW() - INTERVAL '25 days', 'Published')
    RETURNING assignment_id INTO a_lab;
    -- Draft: must NOT appear on the dashboard
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_swp, 'Iteration 3 - Final report (draft)', 'Not published yet.', NOW() - INTERVAL '1 day', NOW() + INTERVAL '30 days', 'Draft');

    -- Submissions: SRS graded (submitted by the teammate for the group), SDS submitted but not graded, Lab 3 graded
    INSERT INTO "Submission"(assignment_id, student_id, group_id, submit_time, status, teacher_id, score, feedback, evaluated_at, file_url)
    VALUES (a_srs, u_friend, g_team, NOW() - INTERVAL '6 days', 'Graded', u_teacher, 8.5,
            'Clear use cases. The ERD is missing the Subject table and some attributes; fix it before Iteration 2.',
            NOW() - INTERVAL '2 days', 'https://example.com/srs.pdf');
    INSERT INTO "Submission"(assignment_id, student_id, group_id, submit_time, status, file_url)
    VALUES (a_sds, u_me, g_team, NOW() - INTERVAL '3 hours', 'Submitted', 'https://example.com/sds.pdf');
    INSERT INTO "Submission"(assignment_id, student_id, group_id, submit_time, status, teacher_id, score, feedback, evaluated_at, file_url)
    VALUES (a_lab, u_me, g_prj, NOW() - INTERVAL '26 days', 'Graded', u_teacher, 9.0,
            'Good job.', NOW() - INTERVAL '20 days', 'https://example.com/lab3.zip');

    RAISE NOTICE 'Demo data created. Sign in as student.demo / 12345678';
END $$;
