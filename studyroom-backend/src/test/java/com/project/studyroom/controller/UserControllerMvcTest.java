package com.project.studyroom.controller;

import com.project.studyroom.config.security.UserResolver;
import com.project.studyroom.exception.GlobalExceptionHandler;
import com.project.studyroom.model.User;
import com.project.studyroom.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de API REST com MockMvc (standalone) — sem subir Firebase/Firestore.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerMvcTest {

    @Mock
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private MockMvc mockMvc;

    /**
     * MockMvc standalone não aplica o filter chain do Spring Security; o {@code user()} request
     * post-processor nem sempre preenche {@link SecurityContextHolder} antes do {@link UserResolver}.
     * Definimos a autenticação explicitamente (e limpamos após cada teste).
     */
    private static RequestPostProcessor authenticatedUser(String uid) {
        return request -> {
            var token = UsernamePasswordAuthenticationToken.authenticated(
                    uid,
                    "n/a",
                    List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
            SecurityContextHolder.getContext().setAuthentication(token);
            return request;
        };
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new UserResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void postUsers_returns201_whenBodyValid() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Maria\",\"email\":\"maria@example.com\"}")
                        .with(authenticatedUser("uid-firebase-1")))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("Perfil sincronizado")));

        verify(userService).create(userCaptor.capture());
        User created = userCaptor.getValue();
        assertEquals("uid-firebase-1", created.getId());
        assertEquals("Maria", created.getName());
        assertEquals("maria@example.com", created.getEmail());
    }

    @Test
    void postUsers_returns400_whenEmailInvalid() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"email\":\"not-an-email\"}")
                        .with(authenticatedUser("uid-2")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMe_returns200_andJson() throws Exception {
        User u = new User();
        u.setId("uid-3");
        u.setName("João");
        u.setEmail("joao@example.com");
        u.setRole("STUDENT");
        when(userService.findById("uid-3")).thenReturn(u);

        mockMvc.perform(get("/api/users/me")
                        .with(authenticatedUser("uid-3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("João"))
                .andExpect(jsonPath("$.email").value("joao@example.com"));
    }
}
