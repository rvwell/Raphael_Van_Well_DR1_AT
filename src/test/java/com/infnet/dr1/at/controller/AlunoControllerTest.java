package com.infnet.dr1.at.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.dr1.at.controller.dto.AlocarDisciplinasRequest;
import com.infnet.dr1.at.model.Aluno;
import com.infnet.dr1.at.service.AlunoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
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

    private Aluno alunoBase;
    private Aluno alunoSalvo;

    @BeforeEach
    void setup() {
        alunoBase = new Aluno("1","Joao","111","j@x","999",null, Set.of(), List.of());
        alunoSalvo = new Aluno("10","Joao","111","j@x","999",null,null,null);
    }

    private ResultActions doGet(String url) throws Exception {
        return mockMvc.perform(get(url));
    }

    private ResultActions doPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions doPut(String url, Object body) throws Exception {
        return mockMvc.perform(put(url).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions doDelete(String url) throws Exception {
        return mockMvc.perform(delete(url).with(csrf()));
    }

    private AlocarDisciplinasRequest req(String... codigos) {
        AlocarDisciplinasRequest r = new AlocarDisciplinasRequest();
        r.setCodigosDisciplinas(List.of(codigos));
        return r;
    }

    @Nested
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("GET /alunos")
    class GetAlunos {
        @Test
        void listarTodos_ok() throws Exception {
            when(alunoService.listarTodos()).thenReturn(List.of(alunoBase));

            doGet("/alunos")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is("1")));
        }

        @Test
        void buscarPorId_found() throws Exception {
            when(alunoService.buscarPorId("1")).thenReturn(Optional.of(alunoBase));

            doGet("/alunos/1")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is("1")));
        }

        @Test
        void buscarPorId_notFound() throws Exception {
            when(alunoService.buscarPorId("9")).thenReturn(Optional.empty());

            doGet("/alunos/9")
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("POST /alunos")
    class PostAlunos {
        @Test
        void cadastrar_created() throws Exception {
            when(alunoService.salvar(any(Aluno.class))).thenReturn(alunoSalvo);

            doPost("/alunos", alunoBase)
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/alunos/10"))
                    .andExpect(jsonPath("$.id", is("10")));
        }
    }

    @Nested
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("PUT /alunos")
    class PutAlunos {
        @Test
        void atualizar_ok() throws Exception {
            Aluno updated = new Aluno("1","New","111","old@x","999",null,null,null);
            when(alunoService.buscarPorId("1")).thenReturn(Optional.of(alunoBase));
            when(alunoService.salvar(any(Aluno.class))).thenReturn(updated);

            doPut("/alunos/1", updated)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome", is("New")));
        }

        @Test
        void atualizar_notFound() throws Exception {
            when(alunoService.buscarPorId("1")).thenReturn(Optional.empty());

            doPut("/alunos/1", alunoBase)
                    .andExpect(status().isNotFound());
        }

        @Test
        void alocarDisciplinas_ok() throws Exception {
            Aluno comDisciplinas = new Aluno("1","Joao","111","j@x","999",null, Set.of("x","y"), null);
            when(alunoService.alocarEmDisciplinasPorCodigo("1", List.of("D1","D2")))
                    .thenReturn(Optional.of(comDisciplinas));

            doPut("/alunos/1/disciplinas", req("D1","D2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is("1")));
        }

        @Test
        void alocarDisciplinas_badRequest() throws Exception {
            when(alunoService.alocarEmDisciplinasPorCodigo("1", List.of()))
                    .thenThrow(new IllegalArgumentException("erro"));

            doPut("/alunos/1/disciplinas", req())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void alocarDisciplinas_notFound() throws Exception {
            when(alunoService.alocarEmDisciplinasPorCodigo("1", List.of("D1")))
                    .thenReturn(Optional.empty());

            doPut("/alunos/1/disciplinas", req("D1"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @WithMockUser(roles = "PROFESSOR")
    @DisplayName("DELETE /alunos")
    class DeleteAlunos {
        @Test
        void deletar_noContent() throws Exception {
            doDelete("/alunos/1")
                    .andExpect(status().isNoContent());
        }
    }
}

