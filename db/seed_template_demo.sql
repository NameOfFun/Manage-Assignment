-- ============================================================================
-- Demo data for Assignment Templates.
-- Run AFTER seed_class_management_demo.sql (subjects and expert.demo must exist).
-- Inserts templates for: SWP391, PRJ301, PRN212, SWT301, MAS291
-- ============================================================================
DO $$
DECLARE
    u_expert  int;
    s_swp     int;
    s_prj     int;
    s_prn     int;
    s_swt     int;
    s_mas     int;
BEGIN
    -- Get expert user id
    SELECT user_id INTO u_expert FROM "User" WHERE username = 'expert.demo';
    IF u_expert IS NULL THEN
        RAISE NOTICE 'expert.demo not found. Run seed_class_management_demo.sql first.';
        RETURN;
    END IF;

    -- Xóa hết template của expert.demo trước khi insert lại (tránh duplicate)
    DELETE FROM "AssignmentTemplate" WHERE expert_id = u_expert;

    -- Reset sequence về 1 (chỉ reset nếu bảng rỗng hoàn toàn)
    IF (SELECT COUNT(*) FROM "AssignmentTemplate") = 0 THEN
        PERFORM setval(pg_get_serial_sequence('"AssignmentTemplate"', 'template_id'), 1, false);
    END IF;

    -- Get subject ids
    SELECT settingid INTO s_swp FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND value = 'SWP391' LIMIT 1;
    SELECT settingid INTO s_prj FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND value = 'PRJ301' LIMIT 1;
    SELECT settingid INTO s_prn FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND value = 'PRN212' LIMIT 1;
    SELECT settingid INTO s_swt FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND value = 'SWT301' LIMIT 1;
    SELECT settingid INTO s_mas FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND value = 'MAS291' LIMIT 1;

    -- -----------------------------------------------------------------------
    -- SWP391 - Software Project (6 templates)
    -- -----------------------------------------------------------------------
    IF s_swp IS NOT NULL THEN

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_swp,
            'Iteration 1 – Software Requirements Specification (SRS)',
            'Prepare and submit a Software Requirements Specification (SRS) document for your project.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Follow the IEEE 830 standard structure.' || chr(10) ||
            '- Include: Introduction, Overall Description, Specific Requirements (functional + non-functional).' || chr(10) ||
            '- Attach a Context Diagram, Use Case Diagram, and ERD.' || chr(10) ||
            '- Document must be submitted as a single PDF file (max 20 MB).' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: SRS_<ClassCode>_Group<N>.pdf' || chr(10) ||
            'Note: All group members must contribute. Add a Change Log table to track revisions.')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_swp,
            'Iteration 1 – Software Design Specification (SDS)',
            'Prepare and submit a Software Design Specification (SDS) document for your project.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Include: Architecture Overview, Package Diagram, Database Design (all tables with constraints).' || chr(10) ||
            '- Include detailed sequence diagrams for at least 5 key use cases.' || chr(10) ||
            '- Include class diagrams for each main package.' || chr(10) ||
            '- Document must be submitted as a single PDF file (max 20 MB).' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: SDS_<ClassCode>_Group<N>.pdf' || chr(10) ||
            'Note: Design must be consistent with the SRS submitted in Iteration 1.')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_swp,
            'Iteration 2 – Updated Documents (SRS + SDS)',
            'Submit updated versions of the SRS and SDS documents reflecting all changes made during Iteration 2.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Update all sections affected by scope or design changes.' || chr(10) ||
            '- Include a Change Log table summarising every modification made since Iteration 1.' || chr(10) ||
            '- Both documents must be submitted together as a ZIP archive.' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Docs_Iter2_<ClassCode>_Group<N>.zip' || chr(10) ||
            'Note: No new feature may appear in the implementation without being documented first.')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_swp,
            'Iteration 2 – Source Code Submission',
            'Push your project source code to GitHub and submit the repository link.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- The repository must be public or the instructor must be added as a collaborator.' || chr(10) ||
            '- Include a README.md with: project description, setup instructions, and team members.' || chr(10) ||
            '- All implemented features must match the use cases documented in the SRS.' || chr(10) ||
            '- Submit a text file (.txt) containing only the repository URL.' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: repo_link_<ClassCode>_Group<N>.txt' || chr(10) ||
            'Note: Commits must show individual contributions from each group member.')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_swp,
            'Iteration 3 – Final Report',
            'Submit the final project report consolidating all work done across all iterations.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Include: Executive Summary, Final SRS, Final SDS, Testing Evidence, Lessons Learned.' || chr(10) ||
            '- Attach screenshots of all implemented screens.' || chr(10) ||
            '- Include a complete test case table with expected vs actual results.' || chr(10) ||
            '- Document must be submitted as a single PDF file (max 20 MB).' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: FinalReport_<ClassCode>_Group<N>.pdf' || chr(10) ||
            'Note: This report will be reviewed during the final presentation.')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_swp,
            'Iteration 3 – Final Source Code & Demo Video',
            'Submit the final version of the source code and a demo video of your working application.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Push the final code to GitHub with a tag named v1.0.0.' || chr(10) ||
            '- Record a demo video (5–10 minutes) covering all main features.' || chr(10) ||
            '- Upload the video to Google Drive and submit a shared link.' || chr(10) ||
            '- Submit a ZIP containing: repo link (.txt) + video link (.txt).' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Final_<ClassCode>_Group<N>.zip' || chr(10) ||
            'Note: The application must be deployable and runnable by the evaluator.')
        ON CONFLICT DO NOTHING;

    END IF;

    -- -----------------------------------------------------------------------
    -- PRJ301 - Java Web (4 templates)
    -- -----------------------------------------------------------------------
    IF s_prj IS NOT NULL THEN

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_prj,
            'Lab 1 – HTML & CSS Static Page',
            'Build a static website for a topic of your choice using only HTML5 and CSS3.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- At least 3 pages: Home, About, Contact.' || chr(10) ||
            '- Use CSS Flexbox or Grid for layout.' || chr(10) ||
            '- The website must be responsive (mobile-friendly).' || chr(10) ||
            '- Submit as a ZIP archive containing all HTML, CSS, and image files.' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Lab1_<StudentCode>.zip')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_prj,
            'Lab 2 – JSP & Servlet CRUD Application',
            'Build a simple CRUD web application using JSP and Servlet.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Implement Create, Read, Update, Delete for at least one entity (e.g., Product, Student).' || chr(10) ||
            '- Use JDBC to connect to a SQL Server or MySQL database.' || chr(10) ||
            '- Apply MVC pattern: Servlet as Controller, JSP as View, DAO as Model.' || chr(10) ||
            '- Submit as a ZIP containing the NetBeans/IntelliJ project and a .sql file to recreate the DB.' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Lab2_<StudentCode>.zip')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_prj,
            'Lab 3 – Session, Cookie & Authentication',
            'Extend your Lab 2 application to include user authentication using Session and Cookie.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Implement Login / Logout functionality.' || chr(10) ||
            '- Use HttpSession to maintain login state.' || chr(10) ||
            '- Protect all CRUD pages with a login filter (no access without valid session).' || chr(10) ||
            '- Optionally add a "Remember Me" feature using Cookie.' || chr(10) ||
            '- Submit as a ZIP containing the full updated project.' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Lab3_<StudentCode>.zip')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_prj,
            'Project – Full-Stack Java Web Application',
            'Build and submit a complete Java Web application as the final project for PRJ301.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Choose a real-world domain (e.g., hotel booking, library management, e-commerce).' || chr(10) ||
            '- Must implement: Authentication, Role-based access, CRUD for at least 3 entities, Search & Filter, Pagination.' || chr(10) ||
            '- Apply MVC pattern with DAO layer.' || chr(10) ||
            '- Submit a ZIP containing: source code, .sql file, and a 1-page project description.' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Project_<ClassCode>_Group<N>.zip' || chr(10) ||
            'Note: Each group member must demo at least one feature during the presentation.')
        ON CONFLICT DO NOTHING;

    END IF;

    -- -----------------------------------------------------------------------
    -- PRN212 - .NET Desktop (4 templates)
    -- -----------------------------------------------------------------------
    IF s_prn IS NOT NULL THEN

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_prn,
            'Lab 1 – Windows Forms Basic UI',
            'Create a Windows Forms application that demonstrates basic UI controls.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Use at least 8 different controls: TextBox, Button, Label, ComboBox, ListBox, CheckBox, RadioButton, DataGridView.' || chr(10) ||
            '- Implement basic input validation (no empty required fields, valid email format, etc.).' || chr(10) ||
            '- Submit as a ZIP of the Visual Studio project.' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Lab1_<StudentCode>.zip')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_prn,
            'Lab 2 – ADO.NET Database Access',
            'Build a Windows Forms application that performs CRUD operations using ADO.NET.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Connect to SQL Server using SqlConnection and SqlCommand.' || chr(10) ||
            '- Display records in a DataGridView.' || chr(10) ||
            '- Implement Add, Edit, Delete, and Search functions.' || chr(10) ||
            '- Use parameterized queries to prevent SQL injection.' || chr(10) ||
            '- Submit as a ZIP containing the Visual Studio project and a .sql file.' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Lab2_<StudentCode>.zip')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_prn,
            'Lab 3 – Entity Framework & LINQ',
            'Rewrite the Lab 2 application using Entity Framework (Code First) and LINQ queries.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Define at least 2 related entities with a foreign key relationship.' || chr(10) ||
            '- Use LINQ to Objects and LINQ to Entities for all data operations.' || chr(10) ||
            '- Implement filtering and sorting using LINQ.' || chr(10) ||
            '- Submit as a ZIP of the updated project (no raw SQL allowed).' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Lab3_<StudentCode>.zip')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_prn,
            'Project – Desktop Management Application',
            'Build a full desktop management application using C# Windows Forms and Entity Framework.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Choose a domain: inventory, HR, library, etc.' || chr(10) ||
            '- Must include: Login with roles, Dashboard with summary stats, CRUD for at least 3 entities, Report/Export to Excel.' || chr(10) ||
            '- Apply a 3-layer architecture (Presentation, Business Logic, Data Access).' || chr(10) ||
            '- Submit a ZIP containing: project source, .sql file, and a user manual (PDF).' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Project_<ClassCode>_Group<N>.zip')
        ON CONFLICT DO NOTHING;

    END IF;

    -- -----------------------------------------------------------------------
    -- SWT301 - Software Testing (3 templates)
    -- -----------------------------------------------------------------------
    IF s_swt IS NOT NULL THEN

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_swt,
            'Lab 1 – Black-Box Test Case Design',
            'Design black-box test cases for a given web application feature.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Apply Equivalence Partitioning and Boundary Value Analysis techniques.' || chr(10) ||
            '- Write at least 20 test cases in a table format: ID, Description, Input, Expected Result, Actual Result, Status.' || chr(10) ||
            '- Include both positive and negative test cases.' || chr(10) ||
            '- Submit as an Excel file (.xlsx).' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Lab1_<StudentCode>.xlsx')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_swt,
            'Lab 2 – Unit Testing with JUnit',
            'Write unit tests for a provided Java class using JUnit 5.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Achieve at least 80% code coverage (measured by JaCoCo).' || chr(10) ||
            '- Test at least 3 methods with multiple test cases each.' || chr(10) ||
            '- Use @Test, @BeforeEach, @AfterEach, and @ParameterizedTest appropriately.' || chr(10) ||
            '- Export the JaCoCo coverage report and include it in the submission.' || chr(10) ||
            '- Submit as a ZIP containing the Maven/Gradle project.' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Lab2_<StudentCode>.zip')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_swt,
            'Project – Test Plan & Execution Report',
            'Create a complete Test Plan and execute tests on your team''s software project.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Write a Test Plan document covering: scope, objectives, test strategy, resources, schedule.' || chr(10) ||
            '- Design and execute at least 50 test cases covering functional requirements.' || chr(10) ||
            '- Include bug reports for all defects found (severity, priority, steps to reproduce).' || chr(10) ||
            '- Summarise results in a Test Summary Report.' || chr(10) ||
            '- Submit as a ZIP containing: Test Plan (PDF), Test Cases (Excel), Bug Report (Excel).' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: TestProject_<ClassCode>_Group<N>.zip')
        ON CONFLICT DO NOTHING;

    END IF;

    -- -----------------------------------------------------------------------
    -- MAS291 - Statistics & Probability (3 templates)
    -- -----------------------------------------------------------------------
    IF s_mas IS NOT NULL THEN

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_mas,
            'Assignment 1 – Descriptive Statistics Analysis',
            'Collect a real-world dataset and perform descriptive statistics analysis.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Dataset must have at least 50 observations and 3 variables.' || chr(10) ||
            '- Calculate: mean, median, mode, variance, standard deviation, quartiles.' || chr(10) ||
            '- Create at least 3 charts: histogram, box plot, and one chart of your choice.' || chr(10) ||
            '- Write a 1-page interpretation of your findings.' || chr(10) ||
            '- Submit as a PDF report with the dataset attached as an Excel file.' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Assign1_<StudentCode>.zip (PDF + Excel)')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_mas,
            'Assignment 2 – Probability & Distributions',
            'Solve probability problems and model a real-world scenario using probability distributions.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Part A: Solve 5 probability problems (provided separately by the instructor).' || chr(10) ||
            '- Part B: Choose a real-world scenario and model it using Binomial, Poisson, or Normal distribution.' || chr(10) ||
            '- Show all calculation steps clearly.' || chr(10) ||
            '- Use Excel or Python to verify your calculations and include screenshots.' || chr(10) ||
            '- Submit as a PDF file.' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Assign2_<StudentCode>.pdf')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_mas,
            'Assignment 3 – Hypothesis Testing & Regression',
            'Apply hypothesis testing and linear regression to a real dataset.' || chr(10) ||
            '' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Part A: Perform a t-test or chi-square test on a dataset of your choice. State H0 and H1 clearly.' || chr(10) ||
            '- Part B: Build a simple linear regression model. Report R², slope, intercept, and interpret results.' || chr(10) ||
            '- Use Excel, R, or Python. Include code/formulas and output screenshots.' || chr(10) ||
            '- Submit as a ZIP containing the report (PDF) and analysis file (Excel/ipynb/R).' || chr(10) ||
            '' || chr(10) ||
            'Deliverable: Assign3_<StudentCode>.zip')
        ON CONFLICT DO NOTHING;

    END IF;

    RAISE NOTICE 'Assignment Template demo data inserted successfully (% subjects seeded).',
        (CASE WHEN s_swp IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN s_prj IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN s_prn IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN s_swt IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN s_mas IS NOT NULL THEN 1 ELSE 0 END);
END $$;

-- ============================================================================
-- Additional templates for extra subjects (CSD201, MAD, MAE101, PRO192)
-- Run after the main seed block above
-- ============================================================================
DO $$
DECLARE
    u_expert  int;
    s_csd     int;
    s_mad     int;
    s_mae     int;
    s_pro     int;
BEGIN
    SELECT user_id INTO u_expert FROM "User" WHERE username = 'expert.demo';
    IF u_expert IS NULL THEN RETURN; END IF;

    SELECT settingid INTO s_csd FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND UPPER(value) LIKE '%CSD%' LIMIT 1;
    SELECT settingid INTO s_mad FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND UPPER(value) LIKE '%MAD%' LIMIT 1;
    SELECT settingid INTO s_mae FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND UPPER(value) LIKE '%MAE%' LIMIT 1;
    SELECT settingid INTO s_pro FROM "Settings" WHERE UPPER(type) = 'SUBJECT' AND UPPER(value) LIKE '%PRO%' LIMIT 1;

    -- CSD201 - Data Structure and Algorithm
    IF s_csd IS NOT NULL THEN
        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_csd, 'Lab 1 – Sorting Algorithm Implementation',
            'Implement and compare Bubble Sort, Selection Sort, and Quick Sort in Java or C++.' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Each algorithm must sort an array of at least 1000 random integers.' || chr(10) ||
            '- Measure and display the execution time for each algorithm.' || chr(10) ||
            '- Write a short analysis comparing time complexity (best/worst/average case).' || chr(10) ||
            'Deliverable: Lab1_<StudentCode>.zip (source code + analysis PDF)')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_csd, 'Lab 2 – Linked List & Stack/Queue',
            'Implement a singly linked list, a stack, and a queue from scratch (no built-in collections).' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Linked list: insert, delete, search, reverse.' || chr(10) ||
            '- Stack: push, pop, peek, isEmpty.' || chr(10) ||
            '- Queue: enqueue, dequeue, front, isEmpty.' || chr(10) ||
            '- Demonstrate each structure with a practical use case (e.g., expression evaluation for stack).' || chr(10) ||
            'Deliverable: Lab2_<StudentCode>.zip')
        ON CONFLICT DO NOTHING;
    END IF;

    -- MAD - Discrete Math
    IF s_mad IS NOT NULL THEN
        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_mad, 'Assignment 1 – Logic & Proof Techniques',
            'Solve problems on propositional logic, predicate logic, and proof methods.' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Part A: Construct truth tables for 5 given logical expressions.' || chr(10) ||
            '- Part B: Write formal proofs (direct, contrapositive, contradiction) for 3 given statements.' || chr(10) ||
            '- Show all steps clearly. Submit as a handwritten scan or typed PDF.' || chr(10) ||
            'Deliverable: Assign1_<StudentCode>.pdf')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_mad, 'Assignment 2 – Graph Theory',
            'Apply graph algorithms to solve network problems.' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Represent a given graph using adjacency matrix and adjacency list.' || chr(10) ||
            '- Apply BFS and DFS and trace the traversal order.' || chr(10) ||
            '- Find the shortest path using Dijkstra''s algorithm.' || chr(10) ||
            '- Submit as PDF with drawn diagrams included.' || chr(10) ||
            'Deliverable: Assign2_<StudentCode>.pdf')
        ON CONFLICT DO NOTHING;
    END IF;

    -- MAE101 - Math Basic
    IF s_mae IS NOT NULL THEN
        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_mae, 'Assignment 1 – Calculus: Limits & Derivatives',
            'Solve calculus problems on limits, continuity, and derivatives.' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Compute limits (including L''Hôpital''s rule where applicable) for 5 given functions.' || chr(10) ||
            '- Find the derivative using chain rule, product rule, and quotient rule.' || chr(10) ||
            '- Apply derivatives to find local maxima/minima of 2 given functions.' || chr(10) ||
            '- Show all steps. Submit as a handwritten scan or typed PDF.' || chr(10) ||
            'Deliverable: Assign1_<StudentCode>.pdf')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_mae, 'Assignment 2 – Linear Algebra: Matrices & Systems',
            'Solve matrix operations and systems of linear equations.' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Perform matrix addition, multiplication, transpose, and inverse for given matrices.' || chr(10) ||
            '- Solve a system of 3 linear equations using Gaussian elimination.' || chr(10) ||
            '- Calculate the determinant and verify using cofactor expansion.' || chr(10) ||
            'Deliverable: Assign2_<StudentCode>.pdf')
        ON CONFLICT DO NOTHING;
    END IF;

    -- PRO192 - Programming
    IF s_pro IS NOT NULL THEN
        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_pro, 'Lab 1 – Java OOP Fundamentals',
            'Build a Java console application demonstrating core OOP concepts.' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Define at least 2 classes with fields, constructors, and methods.' || chr(10) ||
            '- Apply encapsulation (private fields, public getters/setters).' || chr(10) ||
            '- Demonstrate inheritance and method overriding with at least 1 subclass.' || chr(10) ||
            '- Include a main class that creates objects and tests all methods.' || chr(10) ||
            'Deliverable: Lab1_<StudentCode>.zip')
        ON CONFLICT DO NOTHING;

        INSERT INTO "AssignmentTemplate" (expert_id, subject_id, title, instruction)
        VALUES (u_expert, s_pro, 'Lab 2 – Collections & File I/O',
            'Build a Java application that manages a list of objects using Collections and reads/writes data from files.' || chr(10) ||
            'Requirements:' || chr(10) ||
            '- Use ArrayList or HashMap to store and manage at least 10 objects.' || chr(10) ||
            '- Read initial data from a .txt or .csv file on startup.' || chr(10) ||
            '- Save any changes back to the file on exit.' || chr(10) ||
            '- Implement search and sort on the collection.' || chr(10) ||
            'Deliverable: Lab2_<StudentCode>.zip (source + sample data file)')
        ON CONFLICT DO NOTHING;
    END IF;

    RAISE NOTICE 'Additional template demo data inserted successfully.';
END $$;
