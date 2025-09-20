package com.infnet.dr1.at.controller;

import com.infnet.dr1.at.controller.dto.NotaRequest;
import com.infnet.dr1.at.model.Disciplina;
import com.infnet.dr1.at.model.Aluno;
import com.infnet.dr1.at.service.DisciplinaService;
import com.infnet.dr1.at.service.AlunoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("disciplinas")
public class DisciplinaController {

    private final DisciplinaService disciplinaService;
    private final AlunoService alunoService;

    public DisciplinaController(DisciplinaService disciplinaService, AlunoService alunoService) {
        this.disciplinaService = disciplinaService;
        this.alunoService = alunoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<List<Disciplina>> listarTodas() {
        return ResponseEntity.ok(disciplinaService.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Disciplina> buscarPorId(@PathVariable String id) {
        return disciplinaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Disciplina> cadastrar(@RequestBody Disciplina disciplina) {
        Disciplina nova = disciplinaService.salvar(disciplina);
        return ResponseEntity.created(URI.create("/disciplinas/" + nova.getId())).body(nova);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Disciplina> atualizar(@PathVariable String id, @RequestBody Disciplina disciplina) {
        return disciplinaService.buscarPorId(id)
                .map(existente -> {
                    disciplina.setId(id);
                    return ResponseEntity.ok(disciplinaService.salvar(disciplina));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{disciplinaId}/alunos/{alunoId}/nota")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<?> atribuirNota(Principal principal,
                                          @PathVariable String disciplinaId,
                                          @PathVariable String alunoId,
                                          @RequestBody NotaRequest request) {
        try {
            return alunoService.atribuirNota(principal.getName(), alunoId, disciplinaId, request.getNota())
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        }
    }

    @GetMapping("/{disciplinaId}/aprovados")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<?> listarAprovados(Principal principal, @PathVariable String disciplinaId) {
        try {
            List<Aluno> aprovados = alunoService.listarAprovados(principal.getName(), disciplinaId);
            return ResponseEntity.ok(aprovados);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        }
    }

    @GetMapping("/{disciplinaId}/reprovados")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<?> listarReprovados(Principal principal, @PathVariable String disciplinaId) {
        try {
            List<Aluno> reprovados = alunoService.listarReprovados(principal.getName(), disciplinaId);
            return ResponseEntity.ok(reprovados);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        disciplinaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
