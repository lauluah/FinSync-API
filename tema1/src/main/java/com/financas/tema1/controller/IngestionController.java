package com.financas.tema1.controller;

import com.financas.tema1.DTO.TransactionDTO;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import com.financas.tema1.exceptions.UserNotFoundException;
import com.financas.tema1.repository.UserRepository;
import com.financas.tema1.service.IngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ingest")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ingestão", description = "Importação de transações de fontes externas")
public class IngestionController {

    private final IngestionService ingestionService;
    private final UserRepository userRepository;

    public IngestionController(IngestionService ingestionService,
                               UserRepository userRepository) {
        this.ingestionService = ingestionService;
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "Importar transações externas",
            description = "Dispara o pipeline de ingestão que busca transações em fontes externas (APIs de bancos, etc.) e as salva deduplicadas.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transações importadas com sucesso",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionDTO.class)))),
                    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<List<TransactionDTO>> ingest(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        List<Transaction> imported = ingestionService.ingestFromExternalSources(user);

        List<TransactionDTO> result = imported.stream()
                .map(t -> new TransactionDTO(
                        t.getId(),
                        t.getDescription(),
                        t.getAmount(),
                        t.getDate(),
                        t.getCategory(),
                        t.getType()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
