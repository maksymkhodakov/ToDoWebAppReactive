CREATE TABLE public.roles (
    id SERIAL PRIMARY KEY,
    user_role VARCHAR(255) NOT NULL,
    create_date TIMESTAMP NOT NULL DEFAULT now(),
    update_date TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE public.privileges (
    id SERIAL PRIMARY KEY,
    user_privilege VARCHAR(255) NOT NULL,
    create_date TIMESTAMP NOT NULL DEFAULT now(),
    update_date TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE public.users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255),
    password VARCHAR(255),
    name VARCHAR(255),
    last_name VARCHAR(255),
    role_id INTEGER NOT NULL,
    is_system BOOLEAN,
    create_date TIMESTAMP NOT NULL DEFAULT now(),
    update_date TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES public.roles (id)
);

CREATE TABLE public.todos (
    id SERIAL PRIMARY KEY,
    description TEXT,
    due_date DATE,
    check_mark BOOLEAN,
    completion_date DATE,
    user_id INTEGER,
    create_date TIMESTAMP NOT NULL DEFAULT now(),
    update_date TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_todos_user FOREIGN KEY (user_id) REFERENCES public.users (id)
);

CREATE TABLE public.roles_privileges (
    role_id INTEGER NOT NULL,
    privilege_id INTEGER NOT NULL,
    PRIMARY KEY (role_id, privilege_id),
    CONSTRAINT fk_roles_privilege_role FOREIGN KEY (role_id) REFERENCES public.roles (id),
    CONSTRAINT fk_roles_privilege_privilege FOREIGN KEY (privilege_id) REFERENCES public.privileges (id)
);
