package com.infnet.dr1.at.controller;

import com.infnet.dr1.at.model.Aluno;
import com.infnet.dr1.at.service.AlunoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<List<Aluno>> listarTodos() {
        return ResponseEntity.ok(alunoService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Aluno> buscarPorId(@PathVariable String id) {
        return alunoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Aluno> cadastrar(@RequestBody Aluno aluno) {
        Aluno novoAluno = alunoService.salvar(aluno);
        return ResponseEntity.created(URI.create("/alunos/" + novoAluno.getId())).body(novoAluno);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Aluno> atualizar(@PathVariable String id, @RequestBody Aluno aluno) {
        return alunoService.buscarPorId(id)
                .map(alunoExistente -> {
                    aluno.setId(id);
                    return ResponseEntity.ok(alunoService.salvar(aluno));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        alunoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}