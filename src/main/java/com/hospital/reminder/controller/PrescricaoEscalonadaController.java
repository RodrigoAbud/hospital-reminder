package com.hospital.reminder.controller;

import com.hospital.reminder.dto.AtualizarDadosRequest;
import com.hospital.reminder.dto.CriarPrescricaoCompletaRequest;
import com.hospital.reminder.dto.FinalizarPrescricaoRequest;
import com.hospital.reminder.dto.PrescricaoResponse;
import com.hospital.reminder.entity.PrescricaoEscalonada;
import com.hospital.reminder.service.PrescricaoEscalonadaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prescricoes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Prescrições Escalonadas", description = "API para gerenciamento de prescrições escalonadas de retorno")
public class PrescricaoEscalonadaController {

    private final PrescricaoEscalonadaService service;

    @PostMapping
    @Operation(summary = "Criar nova prescrição escalonada", description = "Cria uma prescrição de retorno com dados completos e escalonamento por fases")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Prescrição criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflito")
    })
    public ResponseEntity<PrescricaoResponse> criarPrescricao(
            @Valid @RequestBody CriarPrescricaoCompletaRequest request) {

        PrescricaoEscalonada salva = service.criarPrescricao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PrescricaoResponse.fromEntity(salva));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar prescrição por ID", description = "Recupera uma prescrição específica pelo seu identificador")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Prescrição encontrada"),
        @ApiResponse(responseCode = "404", description = "Prescrição não encontrada")
    })
    public ResponseEntity<PrescricaoResponse> buscarPorId(
            @Parameter(description = "ID da prescrição") @PathVariable UUID id) {

        PrescricaoEscalonada prescricao = service.buscarPorId(id);
        return ResponseEntity.ok(PrescricaoResponse.fromEntity(prescricao));
    }

    @GetMapping("/paciente/{pacienteIdExterno}")
    @Operation(summary = "Buscar prescrições por id externo do paciente", description = "Recupera prescrições vinculadas ao paciente no sistema hospitalar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Prescrições encontradas")
    })
    public ResponseEntity<List<PrescricaoResponse>> buscarPorPacienteIdExterno(
            @Parameter(description = "ID externo do paciente") @PathVariable String pacienteIdExterno) {

        List<PrescricaoResponse> resp = service.buscarPorPacienteExterno(pacienteIdExterno)
                .stream()
                .map(PrescricaoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}/dados")
    @Operation(summary = "Atualizar dados de contato", description = "Atualiza telefone/email do paciente mantendo o restante do registro")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados atualizados"),
        @ApiResponse(responseCode = "404", description = "Prescrição não encontrada")
    })
    public ResponseEntity<PrescricaoResponse> atualizarDados(
            @Parameter(description = "ID da prescrição") @PathVariable UUID id,
            @Valid @RequestBody AtualizarDadosRequest request) {

        PrescricaoEscalonada atualizada = service.atualizarDados(id, request);
        return ResponseEntity.ok(PrescricaoResponse.fromEntity(atualizada));
    }

    @PutMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar prescrição", description = "Marca a prescrição como finalizada e registra motivo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Finalizada"),
        @ApiResponse(responseCode = "404", description = "Prescrição não encontrada")
    })
    public ResponseEntity<PrescricaoResponse> finalizar(
            @Parameter(description = "ID da prescrição") @PathVariable UUID id,
            @Valid @RequestBody FinalizarPrescricaoRequest request) {

        PrescricaoEscalonada finalizada = service.finalizar(id, request);
        return ResponseEntity.ok(PrescricaoResponse.fromEntity(finalizada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Apagar prescrição (LGPD)", description = "Remove o registro do banco conforme solicitação LGPD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Removida")
    })
    public ResponseEntity<Void> apagarLgpd(
            @Parameter(description = "ID da prescrição") @PathVariable UUID id) {

        service.apagarLGPD(id);
        return ResponseEntity.noContent().build();
    }
}
