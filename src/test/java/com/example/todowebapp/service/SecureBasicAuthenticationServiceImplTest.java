package com.example.todowebapp.service;

import com.example.todowebapp.domain.dto.LoginData;
import com.example.todowebapp.domain.dto.LoginResponseDTO;
import com.example.todowebapp.domain.dto.RegisterData;
import com.example.todowebapp.domain.dto.UserDTO;
import com.example.todowebapp.domain.entity.Role;
import com.example.todowebapp.domain.entity.User;
import com.example.todowebapp.domain.enumerated.UserRole;
import com.example.todowebapp.exceptions.ApiException;
import com.example.todowebapp.exceptions.ErrorCode;
import com.example.todowebapp.repository.RoleRepository;
import com.example.todowebapp.repository.UserRepository;
import com.example.todowebapp.security.AuthenticationUserDetails;
import com.example.todowebapp.service.impl.SecureBasicAuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecureBasicAuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private SecureBasicAuthenticationServiceImpl authService;

    private AuthenticationUserDetails authUserDetails;

    @BeforeEach
    void setUp() {
        authUserDetails = AuthenticationUserDetails.authBuilder()
                .username("test@example.com")
                .password("password")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(Collections.emptyList())
                .userId(1L)
                .userRole(UserRole.ROLE_BASIC_USER)
                .system(false)
                .build();
    }

    // ========================= getCurrentUser ====================================

    @Test
    void testGetCurrentUser_NullPrincipal_ReturnsNull() {
        // when
        UserDTO result = authService.getCurrentUser(null);
        // then
        assertThat(result).isNull();
    }

    @Test
    void testGetCurrentUser_WithPrincipal_ReturnsUserDTO() {
        // when
        UserDTO result = authService.getCurrentUser(authUserDetails);
        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(authUserDetails.getUserId());
        assertThat(result.getEmail()).isEqualTo(authUserDetails.getUsername());
        assertThat(result.getUserRole()).isEqualTo(authUserDetails.getUserRole());
        // Перевіряємо, що привілеї співпадають з authorities principal-а
        assertThat(result.getPrivileges()).isEqualTo(authUserDetails.getAuthorities());
    }

    // ========================= register ====================================

    @Test
    void testRegister_Success() {
        // given
        RegisterData registerData = RegisterData.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("secret")
                .userRole(UserRole.ROLE_BASIC_USER)
                .build();

        Role role = Role.builder()
                .id(10L)
                .build();

        // Немає користувача з таким email
        when(userRepository.findByEmail(registerData.getEmail())).thenReturn(Optional.empty());
        // Знаходимо роль за userRole
        when(roleRepository.findByUserRole(registerData.getUserRole())).thenReturn(Optional.of(role));
        // Енкодимо пароль
        when(passwordEncoder.encode(registerData.getPassword())).thenReturn("encodedSecret");

        // when
        authService.register(registerData);

        // then
        // Перевіряємо, що користувача додали до ролі
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        Role savedRole = roleCaptor.getValue();

        // Переконуємось, що в ролі з'явився новий користувач з очікуваними даними
        User savedUser = savedRole.getUsers().get(savedRole.getUsers().size() - 1);
        assertThat(savedUser.getEmail()).isEqualTo(registerData.getEmail());
        assertThat(savedUser.getName()).isEqualTo(registerData.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(registerData.getLastName());
        assertThat(savedUser.getPassword()).isEqualTo("encodedSecret");
    }

    @Test
    void testRegister_RoleNotFound_ThrowsException() {
        // given
        RegisterData registerData = RegisterData.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .password("secret")
                .userRole(UserRole.ROLE_STANDARD_USER)
                .build();

        // Немає користувача з таким email
        when(userRepository.findByEmail(registerData.getEmail())).thenReturn(Optional.empty());
        // Роль не знайдена
        when(roleRepository.findByUserRole(registerData.getUserRole())).thenReturn(Optional.empty());

        // when/then
        final ApiException exception = assertThrows(ApiException.class, () -> authService.register(registerData));
        assertEquals(exception.getMessage(), ErrorCode.ROLE_NOT_FOUND.getData());
    }

    @Test
    void testRegister_UserAlreadyExists_ThrowsException() {
        // given
        RegisterData registerData = RegisterData.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .password("secret")
                .userRole(UserRole.ROLE_BASIC_USER)
                .build();

        // Існує користувач з таким email
        when(userRepository.findByEmail(registerData.getEmail())).thenReturn(Optional.of(new User()));

        // when/then
        final ApiException exception = assertThrows(ApiException.class, () -> authService.register(registerData));
        assertEquals(exception.getMessage(), ErrorCode.USER_ALREADY_EXISTS.getData());
    }

    @Test
    void testRegister_AttemptToCreateAdminUser_ThrowsException() {
        // given
        RegisterData registerData = RegisterData.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@example.com")
                .password("secret")
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        // when/then
        final ApiException exception = assertThrows(ApiException.class, () -> authService.register(registerData));
        assertEquals(exception.getMessage(), ErrorCode.YOU_CANNOT_CREATE_AN_ADMIN_USER.getData());
    }

    @Test
    void testLogin_Success() {
        // given
        LoginData loginData = LoginData.builder()
                .email("test@example.com")
                .password("password")
                .build();

        // Створюємо об'єкт, який буде повернуто методом authenticate
        UsernamePasswordAuthenticationToken authResult = new UsernamePasswordAuthenticationToken(
                loginData.getEmail(), loginData.getPassword(), Collections.emptyList());

        // Імітуємо аутентифікацію: authenticate повертає authResult
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authResult);

        // Імітація завантаження UserDetails
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(loginData.getEmail())
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername(loginData.getEmail())).thenReturn(userDetails);

        // Імітація генерації JWT
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // when
        LoginResponseDTO response = authService.login(loginData);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");

        // Переконуємось, що метод authenticate викликаний з правильними параметрами
        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        UsernamePasswordAuthenticationToken token = tokenCaptor.getValue();
        assertThat(token.getPrincipal()).isEqualTo(loginData.getEmail());
        assertThat(token.getCredentials()).isEqualTo(loginData.getPassword());
    }
}
