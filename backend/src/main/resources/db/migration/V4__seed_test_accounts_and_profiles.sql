INSERT INTO users (id, email, password, first_name, last_name, role, enabled)
VALUES
    (1, 'admin@staffforge.com', '$2y$10$qe4CzgMw/E/7hdSNtmwSEOLnSjktjvcmq6ARuAIuufGXByYRD.BHq', 'Alex', 'Admin', 'ADMIN', TRUE),
    (2, 'pm@staffforge.com', '$2y$10$dJOAatY3Lhpuo5hoRqIqyOVhFD8O8WbT/0wGvd8ibC6H8igSbuTt6', 'Maria', 'Popescu', 'PROJECT_MANAGER', TRUE),
    (3, 'angajat1@staffforge.com', '$2y$10$ZCwJQ4x9Gt54yjqQUd6iBOWy2J7.RD0SV8Xx.iErL0BRvffJmj3Vm', 'Radu', 'Ionescu', 'EMPLOYEE', TRUE),
    (4, 'angajat2@staffforge.com', '$2y$10$ZCwJQ4x9Gt54yjqQUd6iBOWy2J7.RD0SV8Xx.iErL0BRvffJmj3Vm', 'Elena', 'Dumitrescu', 'EMPLOYEE', TRUE);

INSERT INTO employee_profiles (id, user_id, job_title, department, seniority_level, capacity_percentage, bio)
VALUES
    (1, 1, 'System Administrator', 'Operations', 'LEAD', 80, 'Manages platform access, deployments, and monitoring for the organization.'),
    (2, 2, 'Project Manager', 'Product', 'SENIOR', 100, 'Coordinates delivery, stakeholder communication, and sprint planning across projects.'),
    (3, 3, 'Backend Developer', 'Engineering', 'MID', 90, 'Builds and maintains server-side APIs, integrations, and business logic.'),
    (4, 4, 'Frontend Developer', 'Engineering', 'JUNIOR', 75, 'Develops responsive interfaces and collaborates on UX implementation.');

SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 0), true);
SELECT setval('employee_profiles_id_seq', COALESCE((SELECT MAX(id) FROM employee_profiles), 0), true);