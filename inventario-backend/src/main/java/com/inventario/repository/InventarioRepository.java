package com.inventario.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acessa ls_inventario - tabela de granularidade de COLUNA (uma linha por
 * coluna detectada em um scan de LGPD). Aqui so precisamos dos
 * database_name distintos, com alguns dados de contexto agregados, para
 * a tela de vinculacao com aplicacao.
 */
@Repository
public class InventarioRepository {

    private final JdbcTemplate jdbc;

    public InventarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Um mapa por linha, com chaves: database_name, hosts, sgbd_tipos,
     * system_name, database_description, total_colunas, ultima_varredura.
     */
    public List<Map<String, Object>> listarDatabaseNamesDistintos(boolean somenteAtivos) {
        String filtroAtivo = somenteAtivos ? "WHERE ativo = true " : "";
        String sql =
                "SELECT database_name, " +
                "       string_agg(DISTINCT sgbd_host, ', ') AS hosts, " +
                "       string_agg(DISTINCT sgbd_tipo, ', ') AS sgbd_tipos, " +
                "       min(system_name) AS system_name, " +
                "       min(database_description) AS database_description, " +
                "       count(*) AS total_colunas, " +
                "       max(ultima_varredura) AS ultima_varredura " +
                "FROM ls_inventario " +
                filtroAtivo +
                "GROUP BY database_name " +
                "ORDER BY database_name";
        return jdbc.queryForList(sql);
    }
}
