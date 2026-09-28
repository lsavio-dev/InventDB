package com.inventario.controller;

import com.inventario.dto.NomeRequest;
import com.inventario.model.TipoAplicacao;
import com.inventario.service.TipoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos")
public class TipoController {

    private final TipoService service;

    public TipoController(TipoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoAplicacao> listar() {
        return service.listar();
    }

    @PostMapping
    public TipoAplicacao criar(@RequestBody NomeRequest body) {
        return service.criar(body.getNome());
    }

    @PutMapping("/{id}")
    public TipoAplicacao atualizar(@PathVariable long id, @RequestBody NomeRequest body) {
        return service.atualizar(id, body.getNome());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
