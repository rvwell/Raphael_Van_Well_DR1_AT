package com.infnet.dr1.at.service;

import com.infnet.dr1.at.model.Disciplina;
import com.infnet.dr1.at.repository.DisciplinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DisciplinaServiceTest {

    private DisciplinaRepository repository;
    private DisciplinaService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(DisciplinaRepository.class);
        service = new DisciplinaService(repository);
    }

    @Test
    @DisplayName("listarTodas delega para repository.findAll()")
    void listarTodas() {
        List<Disciplina> expected = Arrays.asList(
                new Disciplina("1","POO","DR1001"),
                new Disciplina("2","BD","DR1002")
        );
        when(repository.findAll()).thenReturn(expected);

        List<Disciplina> result = service.listarTodas();

        assertEquals(expected, result);
        verify(repository, times(1)).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("buscarPorId retorna Optional do repository")
    void buscarPorId() {
        Disciplina d = new Disciplina("1","POO","DR1001");
        when(repository.findById("1")).thenReturn(Optional.of(d));

        Optional<Disciplina> result = service.buscarPorId("1");

        assertTrue(result.isPresent());
        assertEquals(d, result.get());
        verify(repository).findById("1");
    }

    @Test
    @DisplayName("buscarPorCodigo retorna Optional do repository")
    void buscarPorCodigo() {
        Disciplina d = new Disciplina("1","POO","DR1001");
        when(repository.findByCodigo("DR1001")).thenReturn(Optional.of(d));

        Optional<Disciplina> result = service.buscarPorCodigo("DR1001");

        assertTrue(result.isPresent());
        assertEquals(d, result.get());
        verify(repository).findByCodigo("DR1001");
    }

    @Test
    @DisplayName("salvar delega para repository.save() e retorna entidade")
    void salvar() {
        Disciplina input = new Disciplina(null,"POO","DR1001");
        Disciplina saved = new Disciplina("1","POO","DR1001");
        when(repository.save(input)).thenReturn(saved);

        Disciplina result = service.salvar(input);

        assertEquals(saved, result);
        ArgumentCaptor<Disciplina> captor = ArgumentCaptor.forClass(Disciplina.class);
        verify(repository).save(captor.capture());
        assertEquals(input, captor.getValue());
    }

    @Test
    @DisplayName("deletar delega para repository.deleteById()")
    void deletar() {
        service.deletar("abc");
        verify(repository).deleteById("abc");
    }
}
