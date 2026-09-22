package com.javanauta.notificacao.infrastructure.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private Map<String, Object> buildError(String message, HttpStatus status, String path) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        if (path != null) {
            error.put("path", path);
        }
        return error;
    }

    private Map<String, Object> buildError(String message, HttpStatus status) {
        return buildError(message, status, null);
    }

    // ✅ Erro de autenticação do Gmail (username/password errados)
    @ExceptionHandler(MailAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleMailAuthentication(
            MailAuthenticationException ex, HttpServletRequest request) {

        log.error("Erro de autenticação do Gmail: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError("Falha na autenticação com o servidor de email. Verifique username/password.",
                        HttpStatus.UNAUTHORIZED, request.getRequestURI()));
    }

    // ✅ Erro ao enviar email (Gmail bloqueou, destinatário inválido, etc)
    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<Map<String, Object>> handleMailSend(
            MailSendException ex, HttpServletRequest request) {

        log.error("Erro ao enviar email: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildError("Não foi possível enviar o email. Tente novamente mais tarde.",
                        HttpStatus.SERVICE_UNAVAILABLE, request.getRequestURI()));
    }

    // ✅ Exception customizada de email
    @ExceptionHandler(EmailException.class)
    public ResponseEntity<Map<String, Object>> handleEmailException(
            EmailException ex, HttpServletRequest request) {

        log.error("EmailException: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI()));
    }

    // ✅ Validação de campos (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();

        log.warn("Erros de validação: {}", erros);

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Erro de Validação");
        error.put("message", "Dados inválidos");
        error.put("errors", erros);
        error.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ✅ Erro genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Erro não tratado: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError("Erro inesperado: " + ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI()));
    }
}