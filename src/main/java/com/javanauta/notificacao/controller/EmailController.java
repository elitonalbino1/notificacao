package com.javanauta.notificacao.controller;

import com.javanauta.notificacao.business.EmailService;
import com.javanauta.notificacao.business.dto.TarefasDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
@Slf4j
public class EmailController {

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<Void> enviarEmail(@Valid @RequestBody TarefasDTO dto) {
        log.info("Requisição de envio de email recebida para: {}", dto.getEmailUsuario());
        emailService.enviaEmail(dto);
        return ResponseEntity.ok().build();
    }
}