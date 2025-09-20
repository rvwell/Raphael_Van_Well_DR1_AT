package com.infnet.dr1.at.controller;

import com.infnet.dr1.at.controller.dto.LoginRequest;
import com.infnet.dr1.at.controller.dto.RegisterRequest;
import com.infnet.dr1.at.model.Professor;
import com.infnet.dr1.at.repository.ProfessorRepository;
import com.infnet.dr1.at.security.AuthService;
import com.infnet.dr1.at.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authService.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );
        String token = jwtService.generateToken(authentication);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        if (professorRepository.findByUsername(registerRequest.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Username já está em uso.");
        }
        Professor professor = new Professor();
        professor.setUsername(registerRequest.username());
        professor.setPassword(passwordEncoder.encode(registerRequest.password()));
        professor.setRoles(Set.of("ROLE_PROFESSOR"));
        professorRepository.save(professor);
        return ResponseEntity.ok("Professor registrado com sucesso!");
    }
}
