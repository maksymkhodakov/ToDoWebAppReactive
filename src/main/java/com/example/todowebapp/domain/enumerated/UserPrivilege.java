package com.example.todowebapp.domain.enumerated;


public enum UserPrivilege {
    VIEW_TODOS,
    CREATE_TODOS,
    UPDATE_TODOS,
    DELETE_TODOS;

    public static class Authority {
        private Authority() {
        }

        public static final String VIEW_TODOS = "hasAuthority('VIEW_TODOS')";
        public static final String CREATE_TODOS = "hasAuthority('CREATE_TODOS')";
        public static final String UPDATE_TODOS = "hasAuthority('UPDATE_TODOS')";
        public static final String DELETE_TODOS = "hasAuthority('DELETE_TODOS')";
    }
}
