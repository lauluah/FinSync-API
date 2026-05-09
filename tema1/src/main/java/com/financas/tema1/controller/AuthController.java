package com.financas.tema1.controller;

import com.financas.tema1.DTO.UserRegisterDTO;
import com.financas.tema1.DTO.UserResponseDTO;
import com.financas.tema1.domain.User;
import com.financas.tema1.exceptions.InvalidCredentialsException;
import com.financas.tema1.repository.UserRepository;
import com.financas.tema1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Registro e login de usuários")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(
            summary = "Registrar novo usuário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário criado com sucesso",
                            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
                    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                            content = @Content)
            }
    )
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRegisterDTO dto) {
        return ResponseEntity.ok(userService.registerUser(dto));
    }

    @Operation(
            summary = "Login",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                            content = @Content)
            }
    )
    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody UserRegisterDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return ResponseEntity.ok(new UserResponseDTO(user.getId(), user.getEmail()));
    }
}
