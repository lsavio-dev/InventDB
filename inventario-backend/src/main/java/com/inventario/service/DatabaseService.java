package com.inventario.service;

import com.inventario.dto.ImportResult;
import com.inventario.dto.ImportRowDTO;
import com.inventario.dto.SyncResult;
import com.inventario.dto.SyncRowDTO;
import com.inventario.dto.VinculacaoResponseDTO;
import com.inventario.dto.VinculacaoRowDTO;
import com.inventario.exception.ErroNegocioException;
import com.inventario.model.Aplicacao;
import com.inventario.model.Database;
import com.inventario.model.TipoAplicacao;
import com.inventario.repository.AplicacaoRepository;
import com.inventario.repository.DatabaseRepository;
import com.inventario.repository.InventarioRepository;
import com.inventario.repository.TipoAplicacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DatabaseService {

    private static final Set<String> VALORES_VERDADEIROS =
            Set.of("1", "true", "t", "sim", "s", "yes", "y", "verdadeiro");
    private static final Set<String> VALORES_FALSOS =
            Set.of("0", "false", "f", "nao", "não", "n", "no", "falso");

    private final DatabaseRepository dbRepo;
    private final AplicacaoRepository appRepo;
    private final TipoAplicacaoRepository tipoRepo;
    private final InventarioRepository inventarioRepo;
    private final TransactionTemplate transactionTemplate;

    public DatabaseService(DatabaseRepository dbRepo,
                            AplicacaoRepository appRepo,
                            TipoAplicacaoRepository tipoRepo,
                            InventarioRepository inventarioRepo,
                            PlatformTransactionManager txManager) {
        this.dbRepo = dbRepo;
        this.appRepo = appRepo;
        this.tipoRepo = tipoRepo;
        this.inventarioRepo = inventarioRepo;
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    // ------------------------------------------------------------------
    // CRUD simples
    // ------------------------------------------------------------------

    public List<Database> listar(Long appId) {
        return dbRepo.findAll(appId);
    }

    public Database criar(String nome, Long appId, Boolean lgpdScan, String observacao,
                           String statusVirtualizado, String statusDatabase, String descricao) {
        if (nome == null || nome.isBlank()) {
            throw new ErroNegocioException("Informe um nome para o database.");
        }
        if (appId == null) {
            throw new ErroNegocioException("Informe a aplicação do database.");
        }
        if (appRepo.findById(appId).isEmpty()) {
            throw new ErroNegocioException("aplicacao com id=" + appId + " não existe");
        }
        return dbRepo.insert(nome.trim(), appId, lgpdScan, blankToNull(observacao),
                blankToNull(statusVirtualizado), blankToNull(statusDatabase), blankToNull(descricao));
    }

    public Database atualizar(long id, String nome, Long appId, Boolean lgpdScan, String observacao,
                               String statusVirtualizado, String statusDatabase, String descricao) {
        if (appId != null && appRepo.findById(appId).isEmpty()) {
            throw new ErroNegocioException("aplicacao com id=" + appId + " não existe");
        }
        Database atualizado = dbRepo.update(
                id, nome == null ? null : nome.trim(), appId, lgpdScan, blankToNull(observacao),
                blankToNull(statusVirtualizado), blankToNull(statusDatabase), blankToNull(descricao));
        if (atualizado == null) {
            throw new ErroNegocioException("database com id=" + id + " não encontrado");
        }
        return atualizado;
    }

    public void excluir(long id) {
        if (!dbRepo.delete(id)) {
            throw new ErroNegocioException("database com id=" + id + " não encontrado");
        }
    }

    // ------------------------------------------------------------------
    // Busca ou cria (usado pela importacao e pela tabela editavel)
    // ------------------------------------------------------------------

    private long getOrCreateTipo(String nome) {
        return tipoRepo.findByNomeIgnoreCase(nome)
                .map(TipoAplicacao::getId)
                .orElseGet(() -> tipoRepo.insert(nome).getId());
    }

    private long getOrCreateApp(String nome, String tipoNome) {
        var existente = appRepo.findByNomeIgnoreCase(nome);
        if (existente.isPresent()) {
            return existente.get().getId();
        }
        if (tipoNome == null || tipoNome.isBlank()) {
            throw new ErroNegocioException(
                    "aplicação '" + nome + "' não existe; informe o tipo para criá-la");
        }
        long tipoId = getOrCreateTipo(tipoNome.trim());
        return appRepo.insert(nome, tipoId).getId();
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static Boolean parseBool(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim().toLowerCase();
        if (texto.isEmpty() || texto.equals("nan") || texto.equals("none") || texto.equals("nat")) {
            return null;
        }
        if (VALORES_VERDADEIROS.contains(texto)) {
            return true;
        }
        if (VALORES_FALSOS.contains(texto)) {
            return false;
        }
        throw new ErroNegocioException("valor de lgpd_scan inválido: '" + valor + "' (use verdadeiro/falso)");
    }

    // ------------------------------------------------------------------
    // Importacao em lote (CSV/Excel)
    // ------------------------------------------------------------------

    public ImportResult importar(List<ImportRowDTO> linhas) {
        ImportResult resultado = new ImportResult();
        int numeroLinha = 2; // linha 1 e o cabecalho
        for (ImportRowDTO linha : linhas) {
            try {
                transactionTemplate.executeWithoutResult(status -> processarLinhaImportacao(linha, resultado));
                resultado.processadas++;
            } catch (ErroNegocioException e) {
                resultado.erros.add("Linha " + numeroLinha + ": " + e.getMessage());
            } catch (Exception e) {
                resultado.erros.add("Linha " + numeroLinha + ": erro inesperado - " + e.getMessage());
            }
            numeroLinha++;
        }
        return resultado;
    }

    private void processarLinhaImportacao(ImportRowDTO linha, ImportResult resultado) {
        String tipoNome = trim(linha.getTipoAplicacao());
        String appNome = trim(linha.getAplicacao());
        String dbNome = trim(linha.getDatabaseName());
        Boolean lgpdScan = parseBool(linha.getLgpdScan());
        String observacao = blankToNull(linha.getDsObservacao());

        List<String> faltando = new ArrayList<>();
        if (tipoNome.isEmpty()) faltando.add("tipo_aplicacao");
        if (appNome.isEmpty()) faltando.add("aplicacao");
        if (dbNome.isEmpty()) faltando.add("database_name");
        if (!faltando.isEmpty()) {
            throw new ErroNegocioException("campos obrigatórios vazios: " + String.join(", ", faltando));
        }

        boolean tipoExistia = tipoRepo.findByNomeIgnoreCase(tipoNome).isPresent();
        boolean appExistiaAntes = appRepo.findByNomeIgnoreCase(appNome).isPresent();

        long appId = getOrCreateApp(appNome, tipoNome);

        if (!tipoExistia) resultado.tiposCriados++;
        if (!appExistiaAntes) resultado.appsCriados++;

        if (dbRepo.findExistingByNameAndApp(dbNome, appId).isPresent()) {
            resultado.dbsPulados++;
        } else {
            dbRepo.insert(dbNome, appId, lgpdScan, observacao, null, null, blankToNull(linha.getDescricao()));
            resultado.dbsCriados++;
        }
    }

    private static String trim(String v) {
        return v == null ? "" : v.trim();
    }

    // ------------------------------------------------------------------
    // Sincronizacao da tabela editavel (front-end React)
    // ------------------------------------------------------------------

    public SyncResult sincronizarTabela(List<SyncRowDTO> linhas, List<Long> idsOriginais) {
        SyncResult resultado = new SyncResult();
        Set<Long> idsMantidos = new HashSet<>();
        int numeroLinha = 1;

        for (SyncRowDTO linha : linhas) {
            try {
                boolean tinhaId = linha.getId() != null;
                Database salvo = transactionTemplate.execute(status -> upsertLinha(linha));
                idsMantidos.add(salvo.getId());
                if (tinhaId) {
                    resultado.atualizados++;
                } else {
                    resultado.criados++;
                }
            } catch (ErroNegocioException e) {
                resultado.erros.add("Linha " + numeroLinha + ": " + e.getMessage());
            } catch (Exception e) {
                resultado.erros.add("Linha " + numeroLinha + ": erro inesperado - " + e.getMessage());
            }
            numeroLinha++;
        }

        if (idsOriginais != null) {
            for (Long id : idsOriginais) {
                if (!idsMantidos.contains(id)) {
                    try {
                        dbRepo.delete(id);
                        resultado.excluidos++;
                    } catch (Exception e) {
                        resultado.erros.add("Exclusão id=" + id + ": " + e.getMessage());
                    }
                }
            }
        }

        return resultado;
    }

    private Database upsertLinha(SyncRowDTO linha) {
        String nomeDb = trim(linha.getDatabaseName());
        String nomeApp = trim(linha.getAplicacao());
        String nomeTipo = blankToNull(linha.getTipoAplicacao());
        Boolean lgpdScan = linha.getLgpdScan();
        String observacao = blankToNull(linha.getDsObservacao());
        String statusVirtualizado = blankToNull(linha.getStatusVirtualizado());
        String statusDatabase = blankToNull(linha.getStatusDatabase());
        String descricao = blankToNull(linha.getDescricao());

        if (nomeDb.isEmpty()) {
            throw new ErroNegocioException("database_name é obrigatório");
        }
        if (nomeApp.isEmpty()) {
            throw new ErroNegocioException("aplicacao é obrigatória");
        }

        long appId = getOrCreateApp(nomeApp, nomeTipo);

        if (linha.getId() != null) {
            Database atualizado = dbRepo.updateCompleto(
                    linha.getId(), nomeDb, appId, lgpdScan, observacao,
                    statusVirtualizado, statusDatabase, descricao);
            if (atualizado == null) {
                throw new ErroNegocioException("database com id=" + linha.getId() + " não encontrado");
            }
            return atualizado;
        }
        return dbRepo.insert(nomeDb, appId, lgpdScan, observacao, statusVirtualizado, statusDatabase, descricao);
    }

    // ------------------------------------------------------------------
    // Tela de vinculacao: database_name distintos de ls_inventario,
    // cruzados com o vinculo (se houver) em ls_database
    // ------------------------------------------------------------------

    public VinculacaoResponseDTO montarVinculacao() {
        List<Map<String, Object>> agregados = inventarioRepo.listarDatabaseNamesDistintos(true);
        List<Database> linksExistentes = dbRepo.findAll(null);

        Map<String, Database> linkPorNomeLower = new HashMap<>();
        for (Database d : linksExistentes) {
            linkPorNomeLower.put(d.getDatabaseName().toLowerCase(), d);
        }

        Set<String> nomesDoInventario = new HashSet<>();
        List<VinculacaoRowDTO> doInventario = new ArrayList<>();

        for (Map<String, Object> agregado : agregados) {
            String nome = (String) agregado.get("database_name");
            nomesDoInventario.add(nome.toLowerCase());

            VinculacaoRowDTO row = new VinculacaoRowDTO();
            row.databaseName = nome;
            row.hosts = (String) agregado.get("hosts");
            row.sgbdTipos = (String) agregado.get("sgbd_tipos");
            row.systemName = (String) agregado.get("system_name");
            row.databaseDescription = (String) agregado.get("database_description");
            Object totalColunas = agregado.get("total_colunas");
            row.totalColunas = totalColunas == null ? null : ((Number) totalColunas).longValue();
            Object ultimaVarredura = agregado.get("ultima_varredura");
            row.ultimaVarredura = ultimaVarredura == null ? null : ultimaVarredura.toString();
            row.origemInventario = true;

            Database link = linkPorNomeLower.get(nome.toLowerCase());
            if (link != null) {
                row.id = link.getId();
                row.aplicacao = link.getAplicacao();
                row.lgpdScan = link.getLgpdScan();
                row.dsObservacao = link.getDsObservacao();
                row.statusVirtualizado = link.getStatusVirtualizado();
                row.statusDatabase = link.getStatusDatabase();
                row.descricao = link.getDescricao();
                row.databaseSchema = link.getDatabaseSchema();
            }
            doInventario.add(row);
        }

        List<VinculacaoRowDTO> foraDoInventario = new ArrayList<>();
        for (Database d : linksExistentes) {
            if (!nomesDoInventario.contains(d.getDatabaseName().toLowerCase())) {
                VinculacaoRowDTO row = new VinculacaoRowDTO();
                row.id = d.getId();
                row.databaseName = d.getDatabaseName();
                row.aplicacao = d.getAplicacao();
                row.lgpdScan = d.getLgpdScan();
                row.dsObservacao = d.getDsObservacao();
                row.statusVirtualizado = d.getStatusVirtualizado();
                row.statusDatabase = d.getStatusDatabase();
                row.descricao = d.getDescricao();
                row.databaseSchema = d.getDatabaseSchema();
                row.origemInventario = false;
                foraDoInventario.add(row);
            }
        }

        VinculacaoResponseDTO resposta = new VinculacaoResponseDTO();
        resposta.doInventario = doInventario;
        resposta.foraDoInventario = foraDoInventario;
        return resposta;
    }
}
