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
import org.springframework.test.web.servlet.ResultActions;

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

    private ResultActions doGet(String url) throws Exception {
        return mockMvc.perform(get(url).principal(() -> "user"));
    }

    private ResultActions doPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions doPut(String url, Object body) throws Exception {
        return mockMvc.perform(put(url).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .principal(() -> "user"));
    }

    private ResultActions doDelete(String url) throws Exception {
        return mockMvc.perform(delete(url).with(csrf()));
    }

    private NotaRequest nota(double valor) {
        NotaRequest req = new NotaRequest();
        req.setNota(valor);
        return req;
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("GET /disciplinas -> 200")
    void listarTodas_ok() throws Exception {
        when(disciplinaService.listarTodas()).thenReturn(List.of(new Disciplina("1","POO","C1")));

        doGet("/disciplinas")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("1")));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("GET /disciplinas/{id} -> 200/404")
    void buscarPorId() throws Exception {
        when(disciplinaService.buscarPorId("1"))
                .thenReturn(Optional.of(new Disciplina("1","POO","C1")));
        doGet("/disciplinas/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")));

        when(disciplinaService.buscarPorId("9")).thenReturn(Optional.empty());
        doGet("/disciplinas/9")
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("POST /disciplinas -> 201")
    void cadastrar_created() throws Exception {
        Disciplina saved = new Disciplina("1", "POO", "C1");
        when(disciplinaService.salvar(any())).thenReturn(saved);

        doPost("/disciplinas", new Disciplina(null, "POO", "C1"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/disciplinas/1"))
                .andExpect(jsonPath("$.id", is("1")));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /disciplinas/{id} -> 200/404")
    void atualizar() throws Exception {
        Disciplina updated = new Disciplina("1","POO2","C1");
        when(disciplinaService.buscarPorId("1")).thenReturn(Optional.of(new Disciplina("1","POO","C1")));
        when(disciplinaService.salvar(any())).thenReturn(updated);

        doPut("/disciplinas/1", updated)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("POO2")));

        when(disciplinaService.buscarPorId("9")).thenReturn(Optional.empty());
        doPut("/disciplinas/9", updated)
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    void atribuirNota_variacoes() throws Exception {
        when(alunoService.atribuirNota("user","a1","d1",8.0))
                .thenReturn(Optional.of(new Aluno()));
        doPut("/disciplinas/d1/alunos/a1/nota", nota(8.0))
                .andExpect(status().isOk());

        when(alunoService.atribuirNota(any(),any(),any(),anyDouble()))
                .thenThrow(new SecurityException("forbidden"));
        doPut("/disciplinas/d1/alunos/a1/nota", nota(8.0))
                .andExpect(status().isForbidden());
        
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    void listarAprovados_variacoes() throws Exception {
        when(alunoService.listarAprovados("user","d1")).thenReturn(List.of(new Aluno()));
        doGet("/disciplinas/d1/aprovados")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        when(alunoService.listarAprovados("user","d2")).thenThrow(new IllegalArgumentException());
        doGet("/disciplinas/d2/aprovados")
                .andExpect(status().isBadRequest());

        when(alunoService.listarAprovados("user","d3")).thenThrow(new SecurityException());
        doGet("/disciplinas/d3/aprovados")
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    void listarReprovados_variacoes() throws Exception {
        when(alunoService.listarReprovados("user","d1")).thenReturn(List.of(new Aluno()));
        doGet("/disciplinas/d1/reprovados")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        when(alunoService.listarReprovados("user","d2")).thenThrow(new IllegalArgumentException());
        doGet("/disciplinas/d2/reprovados")
                .andExpect(status().isBadRequest());

        when(alunoService.listarReprovados("user","d3")).thenThrow(new SecurityException());
        doGet("/disciplinas/d3/reprovados")
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    void deletar() throws Exception {
        doDelete("/disciplinas/1")
                .andExpect(status().isNoContent());
    }
}

