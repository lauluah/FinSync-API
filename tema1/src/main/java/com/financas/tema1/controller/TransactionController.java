package com.financas.tema1.controller;

import com.financas.tema1.DTO.TransactionCreateDTO;
import com.financas.tema1.DTO.TransactionDTO;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import com.financas.tema1.exceptions.UserNotFoundException;
import com.financas.tema1.repository.TransactionRepository;
import com.financas.tema1.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transações", description = "Criação e consulta de transações financeiras")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionController(TransactionRepository transactionRepository,
                                 UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "Criar transação",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Transação criada",
                            content = @Content(schema = @Schema(implementation = TransactionDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<TransactionDTO> create(
            @RequestBody TransactionCreateDTO dto,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        Transaction transaction = new Transaction(
                dto.description(),
                dto.amount(),
                dto.category(),
                dto.date(),
                dto.type(),
                user
        );

        Transaction saved = transactionRepository.save(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @Operation(
            summary = "Listar todas as transações",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de transações",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionDTO.class)))),
                    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAll(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        List<TransactionDTO> transactions = transactionRepository
                .findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactions);
    }

    @Operation(
            summary = "Transações dos últimos 30 dias",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transações filtradas",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionDTO.class)))),
                    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
            }
    )
    @GetMapping("/last30days")
    public ResponseEntity<List<TransactionDTO>> getLast30Days(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        List<TransactionDTO> transactions = transactionRepository
                .findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .filter(t -> !t.getDate().isBefore(thirtyDaysAgo))
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactions);
    }

    @Operation(
            summary = "Resumo de gastos por categoria",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Mapa categoria → total",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
            }
    )
    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getSummary(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        Map<String, BigDecimal> summary = transactionRepository
                .findByUserId(user.getId())
                .stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().toString(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));

        return ResponseEntity.ok(summary);
    }

    // ─── helpers ──────────────────────────────────────────────────
    private User getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private TransactionDTO toDTO(Transaction t) {
        return new TransactionDTO(
                t.getId(),
                t.getDescription(),
                t.getAmount(),
                t.getDate(),
                t.getCategory(),
                t.getType()
        );
    }
}
