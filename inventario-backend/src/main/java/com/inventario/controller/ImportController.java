package com.inventario.controller;

import com.inventario.dto.ImportResult;
import com.inventario.dto.ImportRowDTO;
import com.inventario.service.DatabaseService;
import com.inventario.service.ImportFileParser;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportFileParser parser;
    private final DatabaseService databaseService;

    public ImportController(ImportFileParser parser, DatabaseService databaseService) {
        this.parser = parser;
        this.databaseService = databaseService;
    }

    @PostMapping
    public ImportResult importar(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        List<ImportRowDTO> linhas = parser.parse(arquivo);
        return databaseService.importar(linhas);
    }

    @GetMapping("/template")
    public ResponseEntity<ByteArrayResource> template() {
        String csv = "tipo_aplicacao,aplicacao,database_name,lgpd_scan,ds_observacao,descricao\n" +
                "Web,Sistema X,db_prod_x,true,Base principal de produção,Banco transacional do Sistema X\n" +
                "Web,Sistema X,db_homolog_x,,,\n";
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modelo_importacao.csv")
                .contentLength(bytes.length)
                .body(resource);
    }
}
