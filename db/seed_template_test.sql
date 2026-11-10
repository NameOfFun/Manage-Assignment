-- ============================================================================
-- Small test data for Assignment Templates.
-- Run AFTER seed_class_management_demo.sql (subjects and expert.demo must exist).
-- Safe to run more than once: nothing is deleted and nothing is added twice.
--
-- Adds:
--   expert.demo manages SWP391 and PRJ301 (needed to see them at /AMS/expert/templates)
--   2 templates for SWP391 and 1 template for PRJ301
--
-- How to test (password 12345678):
--   1. Sign in as teacher.demo -> Assignments -> Create assignment.
--   2. Choose an SWP391 class such as SE2051-D: only the 2 SWP391 templates are listed.
--   3. Choose a template: Assignment name and Instruction are filled in automatically.
--   4. Enter the start date and due date, then save.
-- ============================================================================
DO $$
DECLARE
    u_expert int;
    s_swp    int;
    s_prj    int;
BEGIN
    SELECT user_id INTO u_expert FROM "User" WHERE username = 'expert.demo';
    SELECT settingid INTO s_swp FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND value = 'SWP391' LIMIT 1;
    SELECT settingid INTO s_prj FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND value = 'PRJ301' LIMIT 1;

    IF u_expert IS NULL OR s_swp IS NULL OR s_prj IS NULL THEN
        RAISE NOTICE 'expert.demo, SWP391 or PRJ301 not found. Run seed_class_management_demo.sql first.';
        RETURN;
    END IF;

    -- The expert can only manage templates of the subjects assigned to them
    IF NOT EXISTS (SELECT 1 FROM "SubjectExpert" WHERE user_id = u_expert AND subject_id = s_swp) THEN
        INSERT INTO "SubjectExpert" (user_id, subject_id) VALUES (u_expert, s_swp);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM "SubjectExpert" WHERE user_id = u_expert AND subject_id = s_prj) THEN
        INSERT INTO "SubjectExpert" (user_id, subject_id) VALUES (u_expert, s_prj);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM "AssignmentTemplate" WHERE subject_id = s_swp AND title = '[Template] SRS Document') THEN
        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_swp, '[Template] SRS Document',
            'Write the Software Requirements Specification of your project.' || chr(10) ||
            '- Include the use case diagram and the ERD.' || chr(10) ||
            '- Submit one PDF file: SRS_Group<N>.pdf');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM "AssignmentTemplate" WHERE subject_id = s_swp AND title = '[Template] Project Demo') THEN
        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_swp, '[Template] Project Demo',
            'Prepare a working demo of your project.' || chr(10) ||
            '- Submit the link to your source code repository.' || chr(10) ||
            '- List what each member has done in the README.');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM "AssignmentTemplate" WHERE subject_id = s_prj AND title = '[Template] Servlet CRUD Lab') THEN
        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_prj, '[Template] Servlet CRUD Lab',
            'Build a CRUD screen with Servlet and JSP.' || chr(10) ||
            '- Use the MVC structure and a DAO class.' || chr(10) ||
            '- Submit the project as one ZIP file.');
    END IF;

    RAISE NOTICE 'Template test data is ready.';
END $$;
