-- H2 initialization script for tests
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    user_role VARCHAR(255) NOT NULL,
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS privileges (
    id BIGSERIAL PRIMARY KEY,
    user_privilege VARCHAR(255) NOT NULL,
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255),
    password VARCHAR(255),
    name VARCHAR(255),
    last_name VARCHAR(255),
    role_id BIGINT NOT NULL,
    is_system BOOLEAN,
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE IF NOT EXISTS todos (
    id BIGSERIAL PRIMARY KEY,
    description TEXT,
    due_date DATE,
    check_mark BOOLEAN,
    completion_date DATE,
    user_id BIGINT,
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_todos_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS roles_privileges (
    role_id BIGINT NOT NULL,
    privilege_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, privilege_id),
    CONSTRAINT fk_roles_privilege_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_roles_privilege_privilege FOREIGN KEY (privilege_id) REFERENCES privileges (id)
);

-- Insert default roles
INSERT INTO roles (user_role, create_date, update_date) VALUES
    ('ROLE_BASIC_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_STANDARD_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_PREMIUM_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert default privileges
INSERT INTO privileges (user_privilege, create_date, update_date) VALUES
    ('VIEW_TODOS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CREATE_TODOS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('UPDATE_TODOS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('DELETE_TODOS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MANAGE_USERS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Assign privileges to roles
INSERT INTO roles_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r, privileges p
WHERE r.user_role = 'ROLE_BASIC_USER' AND p.user_privilege IN ('VIEW_TODOS', 'CREATE_TODOS', 'UPDATE_TODOS', 'DELETE_TODOS')
ON CONFLICT DO NOTHING;

INSERT INTO roles_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r, privileges p
WHERE r.user_role = 'ROLE_STANDARD_USER' AND p.user_privilege IN ('VIEW_TODOS', 'CREATE_TODOS', 'UPDATE_TODOS', 'DELETE_TODOS')
ON CONFLICT DO NOTHING;

INSERT INTO roles_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r, privileges p
WHERE r.user_role = 'ROLE_PREMIUM_USER' AND p.user_privilege IN ('VIEW_TODOS', 'CREATE_TODOS', 'UPDATE_TODOS', 'DELETE_TODOS')
ON CONFLICT DO NOTHING;

INSERT INTO roles_privileges (role_id, privilege_id)
SELECT r.id, p.id FROM roles r, privileges p
WHERE r.user_role = 'ROLE_ADMIN' AND p.user_privilege IN ('VIEW_TODOS', 'CREATE_TODOS', 'UPDATE_TODOS', 'DELETE_TODOS', 'MANAGE_USERS')
ON CONFLICT DO NOTHING;

