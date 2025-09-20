package com.infnet.dr1.at.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.dr1.at.controller.dto.LoginRequest;
import com.infnet.dr1.at.controller.dto.RegisterRequest;
import com.infnet.dr1.at.model.Professor;
import com.infnet.dr1.at.repository.ProfessorRepository;
import com.infnet.dr1.at.security.AuthService;
import com.infnet.dr1.at.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.is;
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

    @Test
    @DisplayName("POST /auth/login -> 200 com token")
    void login_ok() throws Exception {
        LoginRequest req = new LoginRequest("user", "pass");
        Authentication authentication = mock(Authentication.class);
        when(authService.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(authentication)).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));

        verify(authService, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken(authentication);
    }

    @Test
    @DisplayName("POST /auth/register -> 400 quando username já em uso")
    void register_usernameEmUso() throws Exception {
        RegisterRequest req = new RegisterRequest("user", "pass");
        when(professorRepository.findByUsername("user")).thenReturn(Optional.of(new Professor()))
                ;

        mockMvc.perform(post("/auth/register").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is("Username já está em uso.")));

        verify(professorRepository, times(1)).findByUsername("user");
        verifyNoMoreInteractions(professorRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("POST /auth/register -> 200 registra professor com senha codificada e ROLE_PROFESSOR")
    void register_ok() throws Exception {
        RegisterRequest req = new RegisterRequest("novo", "senha");
        when(professorRepository.findByUsername("novo")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha")).thenReturn("ENC_senha");
        when(professorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/auth/register").with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(is("Professor registrado com sucesso!")));

        ArgumentCaptor<Professor> captor = ArgumentCaptor.forClass(Professor.class);
        verify(professorRepository).save(captor.capture());
        Professor saved = captor.getValue();
        // Verifica dados
        org.junit.jupiter.api.Assertions.assertEquals("novo", saved.getUsername());
        org.junit.jupiter.api.Assertions.assertEquals("ENC_senha", saved.getPassword());
        org.junit.jupiter.api.Assertions.assertEquals(Set.of("ROLE_PROFESSOR"), saved.getRoles());
    }
}
