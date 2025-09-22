package com.infnet.dr1.at.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.dr1.at.controller.dto.NotaRequest;
import com.infnet.dr1.at.model.Aluno;
import com.infnet.dr1.at.model.Disciplina;
import com.infnet.dr1.at.service.AlunoService;
import com.infnet.dr1.at.service.DisciplinaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DisciplinaController.class)
class DisciplinaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DisciplinaService disciplinaService;

    @MockitoBean
    private AlunoService alunoService;

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("GET /disciplinas -> 200")
    void listarTodas_ok() throws Exception {
        when(disciplinaService.listarTodas()).thenReturn(List.of(new Disciplina("1","POO","C1")));

        mockMvc.perform(get("/disciplinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("1")));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("GET /disciplinas/{id} -> 200/404")
    void buscarPorId() throws Exception {
        when(disciplinaService.buscarPorId("1")).thenReturn(Optional.of(new Disciplina("1","POO","C1")));
        mockMvc.perform(get("/disciplinas/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id", is("1")));

        when(disciplinaService.buscarPorId("9")).thenReturn(Optional.empty());
        mockMvc.perform(get("/disciplinas/9")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("POST /disciplinas -> 201")
    void cadastrar_created() throws Exception {
        Disciplina input = new Disciplina(null, "POO", "C1");
        Disciplina saved = new Disciplina("1", "POO", "C1");
        when(disciplinaService.salvar(any(Disciplina.class))).thenReturn(saved);

        mockMvc.perform(post("/disciplinas").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/disciplinas/1"))
                .andExpect(jsonPath("$.id", is("1")));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /disciplinas/{id} -> 200/404")
    void atualizar() throws Exception {
        Disciplina existing = new Disciplina("1","POO","C1");
        Disciplina updated = new Disciplina("1","POO2","C1");
        when(disciplinaService.buscarPorId("1")).thenReturn(Optional.of(existing));
        when(disciplinaService.salvar(any(Disciplina.class))).thenReturn(updated);

        mockMvc.perform(put("/disciplinas/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("POO2")));

        when(disciplinaService.buscarPorId("9")).thenReturn(Optional.empty());
        mockMvc.perform(put("/disciplinas/9").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /disciplinas/{disciplinaId}/alunos/{alunoId}/nota -> 200")
    void atribuirNota_ok() throws Exception {
        NotaRequest req = new NotaRequest();
        req.setNota(8.0);
        when(alunoService.atribuirNota(eq("user"), eq("a1"), eq("d1"), eq(8.0)))
                .thenReturn(Optional.of(new Aluno()));

        mockMvc.perform(put("/disciplinas/d1/alunos/a1/nota").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(() -> "user"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /disciplinas/{disciplinaId}/alunos/{alunoId}/nota -> 403 quando SecurityException")
    void atribuirNota_forbidden() throws Exception {
        NotaRequest req = new NotaRequest();
        req.setNota(8.0);
        when(alunoService.atribuirNota(anyString(), anyString(), anyString(), anyDouble()))
                .thenThrow(new SecurityException("forbidden"));

        mockMvc.perform(put("/disciplinas/d1/alunos/a1/nota").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(() -> "user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /disciplinas/{disciplinaId}/alunos/{alunoId}/nota -> 400 quando IllegalArgument")
    void atribuirNota_badRequest() throws Exception {
        NotaRequest req = new NotaRequest();
        req.setNota(-1.0);
        when(alunoService.atribuirNota(anyString(), anyString(), anyString(), anyDouble()))
                .thenThrow(new IllegalArgumentException("bad"));

        mockMvc.perform(put("/disciplinas/d1/alunos/a1/nota").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(() -> "user"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /disciplinas/{disciplinaId}/alunos/{alunoId}/nota -> 404 quando Optional.empty")
    void atribuirNota_notFound() throws Exception {
        NotaRequest req = new NotaRequest();
        req.setNota(7.0);
        when(alunoService.atribuirNota(anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/disciplinas/d1/alunos/a1/nota").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(() -> "user"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("GET /disciplinas/{id}/aprovados -> 200/400/403")
    void listarAprovados() throws Exception {
        when(alunoService.listarAprovados(eq("user"), eq("d1"))).thenReturn(List.of(new Aluno()));
        mockMvc.perform(get("/disciplinas/d1/aprovados").principal(() -> "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        when(alunoService.listarAprovados(eq("user"), eq("d2"))).thenThrow(new IllegalArgumentException("bad"));
        mockMvc.perform(get("/disciplinas/d2/aprovados").principal(() -> "user"))
                .andExpect(status().isBadRequest());

        when(alunoService.listarAprovados(eq("user"), eq("d3"))).thenThrow(new SecurityException("forbidden"));
        mockMvc.perform(get("/disciplinas/d3/aprovados").principal(() -> "user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("GET /disciplinas/{id}/reprovados -> 200/400/403")
    void listarReprovados() throws Exception {
        when(alunoService.listarReprovados(eq("user"), eq("d1"))).thenReturn(List.of(new Aluno()));
        mockMvc.perform(get("/disciplinas/d1/reprovados").principal(() -> "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        when(alunoService.listarReprovados(eq("user"), eq("d2"))).thenThrow(new IllegalArgumentException("bad"));
        mockMvc.perform(get("/disciplinas/d2/reprovados").principal(() -> "user"))
                .andExpect(status().isBadRequest());

        when(alunoService.listarReprovados(eq("user"), eq("d3"))).thenThrow(new SecurityException("forbidden"));
        mockMvc.perform(get("/disciplinas/d3/reprovados").principal(() -> "user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("DELETE /disciplinas/{id} -> 204")
    void deletar() throws Exception {
        mockMvc.perform(delete("/disciplinas/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
