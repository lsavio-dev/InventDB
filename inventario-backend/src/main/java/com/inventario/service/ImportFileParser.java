package com.inventario.service;

import com.inventario.dto.ImportRowDTO;
import com.inventario.exception.ErroNegocioException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ImportFileParser {

    private static final Set<String> COLUNAS_OBRIGATORIAS =
            Set.of("tipo_aplicacao", "aplicacao", "database_name");

    public List<ImportRowDTO> parse(MultipartFile arquivo) throws IOException {
        String nome = arquivo.getOriginalFilename() == null ? "" : arquivo.getOriginalFilename().toLowerCase();
        if (nome.endsWith(".csv")) {
            return parseCsv(arquivo);
        } else if (nome.endsWith(".xlsx") || nome.endsWith(".xls")) {
            return parseExcel(arquivo);
        }
        throw new ErroNegocioException("Formato de arquivo não suportado. Use .csv ou .xlsx");
    }

    private List<ImportRowDTO> parseCsv(MultipartFile arquivo) throws IOException {
        List<ImportRowDTO> linhas = new ArrayList<>();
        try (var reader = new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreSurroundingSpaces(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            validarCabecalho(parser.getHeaderNames());
            for (CSVRecord record : parser) {
                ImportRowDTO linha = new ImportRowDTO();
                linha.setTipoAplicacao(valor(record, "tipo_aplicacao"));
                linha.setAplicacao(valor(record, "aplicacao"));
                linha.setDatabaseName(valor(record, "database_name"));
                linha.setLgpdScan(valor(record, "lgpd_scan"));
                linha.setDsObservacao(valor(record, "ds_observacao"));
                linha.setDescricao(valor(record, "descricao"));
                linhas.add(linha);
            }
        }
        return linhas;
    }

    private static String valor(CSVRecord record, String coluna) {
        return record.isMapped(coluna) ? record.get(coluna) : null;
    }

    private List<ImportRowDTO> parseExcel(MultipartFile arquivo) throws IOException {
        List<ImportRowDTO> linhas = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(arquivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Row cabecalho = sheet.getRow(sheet.getFirstRowNum());
            if (cabecalho == null) {
                throw new ErroNegocioException("Planilha vazia.");
            }
            java.util.Map<String, Integer> indicePorColuna = new java.util.HashMap<>();
            for (Cell celula : cabecalho) {
                String nomeColuna = formatter.formatCellValue(celula).trim();
                if (!nomeColuna.isEmpty()) {
                    indicePorColuna.put(nomeColuna, celula.getColumnIndex());
                }
            }
            validarCabecalho(indicePorColuna.keySet());

            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row linhaExcel = sheet.getRow(i);
                if (linhaExcel == null) {
                    continue;
                }
                ImportRowDTO linha = new ImportRowDTO();
                linha.setTipoAplicacao(celulaTexto(linhaExcel, indicePorColuna, formatter, "tipo_aplicacao"));
                linha.setAplicacao(celulaTexto(linhaExcel, indicePorColuna, formatter, "aplicacao"));
                linha.setDatabaseName(celulaTexto(linhaExcel, indicePorColuna, formatter, "database_name"));
                linha.setLgpdScan(celulaTexto(linhaExcel, indicePorColuna, formatter, "lgpd_scan"));
                linha.setDsObservacao(celulaTexto(linhaExcel, indicePorColuna, formatter, "ds_observacao"));
                linha.setDescricao(celulaTexto(linhaExcel, indicePorColuna, formatter, "descricao"));

                // pula linhas totalmente vazias
                if (isBlank(linha.getTipoAplicacao()) && isBlank(linha.getAplicacao())
                        && isBlank(linha.getDatabaseName())) {
                    continue;
                }
                linhas.add(linha);
            }
        }
        return linhas;
    }

    private static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    private static String celulaTexto(Row linha, java.util.Map<String, Integer> indicePorColuna,
                                       DataFormatter formatter, String coluna) {
        Integer indice = indicePorColuna.get(coluna);
        if (indice == null) {
            return null;
        }
        Cell celula = linha.getCell(indice);
        return celula == null ? "" : formatter.formatCellValue(celula).trim();
    }

    private static void validarCabecalho(java.util.Collection<String> colunas) {
        List<String> faltando = new ArrayList<>();
        for (String obrigatoria : COLUNAS_OBRIGATORIAS) {
            if (!colunas.contains(obrigatoria)) {
                faltando.add(obrigatoria);
            }
        }
        if (!faltando.isEmpty()) {
            throw new ErroNegocioException(
                    "Colunas obrigatórias ausentes no arquivo: " + String.join(", ", faltando));
        }
    }
}
