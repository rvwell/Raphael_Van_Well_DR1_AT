package com.infnet.dr1.at.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.dr1.at.controller.dto.AlocarDisciplinasRequest;
import com.infnet.dr1.at.model.Aluno;
import com.infnet.dr1.at.service.AlunoService;
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
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AlunoController.class)
class AlunoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlunoService alunoService;

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("GET /alunos deve retornar lista 200")
    void listarTodos_ok() throws Exception {
        Aluno a1 = new Aluno("1","Joao","111","j@x","999",null, Set.of(), List.of());
        when(alunoService.listarTodos()).thenReturn(List.of(a1));

        mockMvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("1")));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("GET /alunos/{id} encontrado -> 200")
    void buscarPorId_found() throws Exception {
        Aluno a1 = new Aluno("1","Joao","111","j@x","999",null, null, null);
        when(alunoService.buscarPorId("1")).thenReturn(Optional.of(a1));

        mockMvc.perform(get("/alunos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("GET /alunos/{id} nao encontrado -> 404")
    void buscarPorId_notFound() throws Exception {
        when(alunoService.buscarPorId("9")).thenReturn(Optional.empty());

        mockMvc.perform(get("/alunos/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("POST /alunos -> 201 Created com Location")
    void cadastrar_created() throws Exception {
        Aluno input = new Aluno(null, "Joao", "111", "j@x", "999", null, null, null);
        Aluno saved = new Aluno("10", "Joao", "111", "j@x", "999", null, null, null);
        when(alunoService.salvar(any(Aluno.class))).thenReturn(saved);

        mockMvc.perform(post("/alunos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/alunos/10"))
                .andExpect(jsonPath("$.id", is("10")));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /alunos/{id} -> 200 quando existe")
    void atualizar_ok() throws Exception {
        Aluno existing = new Aluno("1","Old","111","old@x","999",null,null,null);
        Aluno updated = new Aluno("1","New","111","old@x","999",null,null,null);
        when(alunoService.buscarPorId("1")).thenReturn(Optional.of(existing));
        when(alunoService.salvar(any(Aluno.class))).thenReturn(updated);

        mockMvc.perform(put("/alunos/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("New")));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /alunos/{id} -> 404 quando nao existe")
    void atualizar_notFound() throws Exception {
        when(alunoService.buscarPorId("1")).thenReturn(Optional.empty());

        mockMvc.perform(put("/alunos/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Aluno())))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /alunos/{id}/disciplinas -> 200 quando sucesso")
    void alocarDisciplinas_ok() throws Exception {
        AlocarDisciplinasRequest req = new AlocarDisciplinasRequest();
        req.setCodigosDisciplinas(List.of("D1","D2"));
        Aluno a = new Aluno("1","Joao","111","j@x","999",null, Set.of("x","y"), null);
        when(alunoService.alocarEmDisciplinasPorCodigo(eq("1"), eq(req.getCodigosDisciplinas())))
                .thenReturn(Optional.of(a));

        mockMvc.perform(put("/alunos/1/disciplinas").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /alunos/{id}/disciplinas -> 400 quando request invalido")
    void alocarDisciplinas_badRequest() throws Exception {
        AlocarDisciplinasRequest req = new AlocarDisciplinasRequest();
        req.setCodigosDisciplinas(List.of());
        when(alunoService.alocarEmDisciplinasPorCodigo(eq("1"), eq(List.of())))
                .thenThrow(new IllegalArgumentException("erro"));

        mockMvc.perform(put("/alunos/1/disciplinas").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /alunos/{id}/disciplinas -> 404 quando aluno nao encontrado")
    void alocarDisciplinas_notFound() throws Exception {
        AlocarDisciplinasRequest req = new AlocarDisciplinasRequest();
        req.setCodigosDisciplinas(List.of("D1"));
        when(alunoService.alocarEmDisciplinasPorCodigo(eq("1"), eq(List.of("D1"))))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/alunos/1/disciplinas").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("DELETE /alunos/{id} -> 204")
    void deletar_noContent() throws Exception {
        mockMvc.perform(delete("/alunos/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
