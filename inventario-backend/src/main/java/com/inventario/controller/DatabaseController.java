package com.inventario.controller;

import com.inventario.dto.DatabaseRequest;
import com.inventario.dto.SyncRequest;
import com.inventario.dto.SyncResult;
import com.inventario.dto.VinculacaoResponseDTO;
import com.inventario.model.Database;
import com.inventario.service.DatabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/databases")
public class DatabaseController {

    private final DatabaseService service;

    public DatabaseController(DatabaseService service) {
        this.service = service;
    }

    @GetMapping
    public List<Database> listar(@RequestParam(required = false) Long appId) {
        return service.listar(appId);
    }

    @GetMapping("/vinculacao")
    public VinculacaoResponseDTO vinculacao() {
        return service.montarVinculacao();
    }

    @PostMapping
    public Database criar(@RequestBody DatabaseRequest body) {
        return service.criar(body.getNome(), body.getAppId(), body.getLgpdScan(), body.getObservacao(),
                body.getStatusVirtualizado(), body.getStatusDatabase(), body.getDescricao());
    }

    @PutMapping("/{id}")
    public Database atualizar(@PathVariable long id, @RequestBody DatabaseRequest body) {
        return service.atualizar(id, body.getNome(), body.getAppId(), body.getLgpdScan(), body.getObservacao(),
                body.getStatusVirtualizado(), body.getStatusDatabase(), body.getDescricao());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    public SyncResult sincronizar(@RequestBody SyncRequest body) {
        return service.sincronizarTabela(body.getLinhas(), body.getIdsOriginais());
    }
}
