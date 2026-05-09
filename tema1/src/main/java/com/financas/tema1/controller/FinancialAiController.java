package com.financas.tema1.controller;

import com.financas.tema1.ai.AiInsightRequest;
import com.financas.tema1.ai.AiInsightResponse;
import com.financas.tema1.domain.User;
import com.financas.tema1.exceptions.AiInsightException;
import com.financas.tema1.exceptions.UserNotFoundException;
import com.financas.tema1.repository.UserRepository;
import com.financas.tema1.service.FinancialAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "IA Financeira", description = "Insights e análises geradas por IA")
public class FinancialAiController {

    private final FinancialAiService financialAiService;
    private final UserRepository userRepository;

    public FinancialAiController(FinancialAiService financialAiService,
                                 UserRepository userRepository) {
        this.financialAiService = financialAiService;
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "Responder pergunta financeira",
            description = "Envia uma pergunta livre sobre suas finanças e recebe uma resposta gerada por IA com base em todo o histórico.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resposta gerada com sucesso",
                            content = @Content(schema = @Schema(implementation = AiInsightResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Pergunta ausente ou em branco", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
            }
    )
    @PostMapping("/insights")
    public AiInsightResponse insights(
            @RequestBody AiInsightRequest request,
            Authentication authentication) {

        User user = currentUser(authentication);
        String question = validateQuestion(request);
        return financialAiService.answer(user.getId(), question);
    }

    @Operation(
            summary = "Resumo dos últimos 30 dias via IA",
            description = "Gera automaticamente um resumo financeiro do último mês com padrões de gasto e recomendações.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resumo gerado",
                            content = @Content(schema = @Schema(implementation = AiInsightResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
            }
    )
    @GetMapping("/summary/last30days")
    public AiInsightResponse getSummaryLast30Days(Authentication authentication) {
        User user = currentUser(authentication);

        String question = "Faça um resumo detalhado da minha movimentação financeira dos últimos 30 dias, "
                + "incluindo padrões de gastos, categorias principais e recomendações.";

        return financialAiService.answerLast30Days(user.getId(), question);
    }

    @Operation(
            summary = "Insights por categoria",
            description = "Analisa os gastos por categoria e identifica oportunidades de economia.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Análise gerada",
                            content = @Content(schema = @Schema(implementation = AiInsightResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
            }
    )
    @GetMapping("/insights/categories")
    public AiInsightResponse getCategoriesInsights(Authentication authentication) {
        User user = currentUser(authentication);
        String question = "Analise meus gastos por categoria e identifique onde posso economizar.";
        return financialAiService.answer(user.getId(), question);
    }

    private User currentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private String validateQuestion(AiInsightRequest request) {
        if (request == null || request.question() == null || request.question().isBlank()) {
            throw new AiInsightException("A pergunta é obrigatória");
        }
        return request.question().trim();
    }
}
