package com.inventario.controller;

import com.inventario.dto.AplicacaoRequest;
import com.inventario.model.Aplicacao;
import com.inventario.service.AplicacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aplicacoes")
public class AplicacaoController {

    private final AplicacaoService service;

    public AplicacaoController(AplicacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Aplicacao> listar(@RequestParam(required = false) Long tipoId) {
        return service.listar(tipoId);
    }

    @PostMapping
    public Aplicacao criar(@RequestBody AplicacaoRequest body) {
        return service.criar(body.getNome(), body.getTipoId());
    }

    @PutMapping("/{id}")
    public Aplicacao atualizar(@PathVariable long id, @RequestBody AplicacaoRequest body) {
        return service.atualizar(id, body.getNome(), body.getTipoId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
