
INSERT INTO public.roles (user_role, create_date, update_date) VALUES
                                                                   ('ROLE_BASIC_USER', now(), now()),
                                                                   ('ROLE_STANDARD_USER', now(), now()),
                                                                   ('ROLE_PREMIUM_USER', now(), now()),
                                                                   ('ROLE_ADMIN', now(), now());

INSERT INTO public.privileges (user_privilege, create_date, update_date) VALUES
                                                                             ('VIEW_TODOS', now(), now()),
                                                                             ('CREATE_TODOS', now(), now()),
                                                                             ('UPDATE_TODOS', now(), now()),
                                                                             ('DELETE_TODOS', now(), now());

INSERT INTO public.roles_privileges (role_id, privilege_id)
VALUES
    (
     (SELECT id FROM public.roles WHERE user_role = 'ROLE_BASIC_USER'),
     (SELECT id FROM public.privileges WHERE user_privilege = 'VIEW_TODOS')
    ),
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_BASIC_USER'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'CREATE_TODOS')
    ),
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_BASIC_USER'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'UPDATE_TODOS')
    );

INSERT INTO public.roles_privileges (role_id, privilege_id)
VALUES
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_STANDARD_USER'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'VIEW_TODOS')
    ),
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_STANDARD_USER'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'CREATE_TODOS')
    ),
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_STANDARD_USER'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'UPDATE_TODOS')
    );

INSERT INTO public.roles_privileges (role_id, privilege_id)
VALUES
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_PREMIUM_USER'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'VIEW_TODOS')
    ),
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_PREMIUM_USER'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'CREATE_TODOS')
    ),
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_PREMIUM_USER'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'UPDATE_TODOS')
    );

INSERT INTO public.roles_privileges (role_id, privilege_id)
VALUES
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_ADMIN'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'VIEW_TODOS')
    ),
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_ADMIN'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'CREATE_TODOS')
    ),
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_ADMIN'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'UPDATE_TODOS')
    ),
    (
        (SELECT id FROM public.roles WHERE user_role = 'ROLE_ADMIN'),
        (SELECT id FROM public.privileges WHERE user_privilege = 'DELETE_TODOS')
    );
