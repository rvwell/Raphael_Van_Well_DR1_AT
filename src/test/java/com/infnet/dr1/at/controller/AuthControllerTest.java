package com.infnet.dr1.at.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.dr1.at.controller.dto.LoginRequest;
import com.infnet.dr1.at.controller.dto.RegisterRequest;
import com.infnet.dr1.at.model.Professor;
import com.infnet.dr1.at.repository.ProfessorRepository;
import com.infnet.dr1.at.security.AuthService;
import com.infnet.dr1.at.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ProfessorRepository professorRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private LoginRequest loginReq;
    private RegisterRequest registerReq;

    @BeforeEach
    void setup() {
        loginReq = new LoginRequest("user", "pass");
        registerReq = new RegisterRequest("novo", "senha");
    }

    private ResultActions doPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url).with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)));
    }

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {
        @Test
        void login_ok() throws Exception {
            Authentication authentication = mock(Authentication.class);
            when(authService.authenticate(any())).thenReturn(authentication);
            when(jwtService.generateToken(authentication)).thenReturn("jwt-token");

            doPost("/auth/login", loginReq)
                    .andExpect(status().isOk())
                    .andExpect(content().string("jwt-token"));

            verify(authService).authenticate(any());
            verify(jwtService).generateToken(authentication);
        }
    }

    @Nested
    @DisplayName("POST /auth/register")
    class RegisterTests {
        @Test
        void usernameEmUso() throws Exception {
            when(professorRepository.findByUsername("user"))
                    .thenReturn(Optional.of(new Professor()));

            doPost("/auth/register", new RegisterRequest("user", "pass"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(is("Username já está em uso.")));

            verify(professorRepository).findByUsername("user");
            verifyNoMoreInteractions(professorRepository);
            verifyNoInteractions(passwordEncoder);
        }

        @Test
        void register_ok() throws Exception {
            when(professorRepository.findByUsername("novo")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("senha")).thenReturn("ENC_senha");
            when(professorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            doPost("/auth/register", registerReq)
                    .andExpect(status().isOk())
                    .andExpect(content().string(is("Professor registrado com sucesso!")));

            ArgumentCaptor<Professor> captor = ArgumentCaptor.forClass(Professor.class);
            verify(professorRepository).save(captor.capture());

            Professor saved = captor.getValue();
            assertEquals("novo", saved.getUsername());
            assertEquals("ENC_senha", saved.getPassword());
            assertEquals(Set.of("ROLE_PROFESSOR"), saved.getRoles());
        }
    }
}

