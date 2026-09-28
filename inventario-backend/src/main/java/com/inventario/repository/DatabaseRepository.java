package com.inventario.repository;

import com.inventario.model.Database;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseRepository {

    private final JdbcTemplate jdbc;

    public DatabaseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE =
            "SELECT d.*, a.aplicacao " +
            "FROM ls_database d " +
            "LEFT JOIN ls_aplicacao a ON a.id = d.id_aplicacao ";

    private static Database map(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Database d = new Database();
        d.setId(rs.getLong("id"));
        d.setDatabaseName(rs.getString("database_name"));
        d.setIdAplicacao(rs.getLong("id_aplicacao"));
        Object lgpd = rs.getObject("lgpd_scan");
        d.setLgpdScan(lgpd == null ? null : (Boolean) lgpd);
        d.setDsObservacao(rs.getString("ds_observacao"));
        d.setStatusVirtualizado(rs.getString("status_virtualizado"));
        d.setStatusDatabase(rs.getString("status_database"));
        d.setDescricao(rs.getString("descricao"));
        d.setDatabaseSchema(rs.getString("database_schema"));
        d.setAplicacao(rs.getString("aplicacao"));
        return d;
    }

    public List<Database> findAll(Long appId) {
        if (appId != null) {
            return jdbc.query(SELECT_BASE + "WHERE d.id_aplicacao = ? ORDER BY d.id",
                    DatabaseRepository::map, appId);
        }
        return jdbc.query(SELECT_BASE + "ORDER BY d.id", DatabaseRepository::map);
    }

    public Optional<Database> findById(long id) {
        try {
            return Optional.of(jdbc.queryForObject(
                    SELECT_BASE + "WHERE d.id = ?", DatabaseRepository::map, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Database> findExistingByNameAndApp(String nome, long appId) {
        List<Database> resultado = jdbc.query(
                SELECT_BASE + "WHERE d.id_aplicacao = ? AND lower(d.database_name) = lower(?)",
                DatabaseRepository::map, appId, nome);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public Database insert(String nome, long appId, Boolean lgpdScan, String observacao,
                            String statusVirtualizado, String statusDatabase, String descricao) {
        Long id = jdbc.queryForObject(
                "INSERT INTO ls_database (database_name, id_aplicacao, lgpd_scan, ds_observacao, " +
                "status_virtualizado, status_database, descricao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id",
                Long.class, nome, appId, lgpdScan, observacao, statusVirtualizado, statusDatabase, descricao);
        return findById(id).orElseThrow();
    }

    public Database update(long id, String nome, Long appId, Boolean lgpdScan, String observacao,
                            String statusVirtualizado, String statusDatabase, String descricao) {
        StringBuilder sql = new StringBuilder("UPDATE ls_database SET ");
        java.util.List<Object> valores = new java.util.ArrayList<>();
        java.util.List<String> campos = new java.util.ArrayList<>();
        if (nome != null) {
            campos.add("database_name = ?");
            valores.add(nome);
        }
        if (appId != null) {
            campos.add("id_aplicacao = ?");
            valores.add(appId);
        }
        if (lgpdScan != null) {
            campos.add("lgpd_scan = ?");
            valores.add(lgpdScan);
        }
        if (observacao != null) {
            campos.add("ds_observacao = ?");
            valores.add(observacao);
        }
        if (statusVirtualizado != null) {
            campos.add("status_virtualizado = ?");
            valores.add(statusVirtualizado);
        }
        if (statusDatabase != null) {
            campos.add("status_database = ?");
            valores.add(statusDatabase);
        }
        if (descricao != null) {
            campos.add("descricao = ?");
            valores.add(descricao);
        }
        if (campos.isEmpty()) {
            return findById(id).orElse(null);
        }
        sql.append(String.join(", ", campos)).append(" WHERE id = ?");
        valores.add(id);

        int linhas = jdbc.update(sql.toString(), valores.toArray());
        if (linhas == 0) {
            return null;
        }
        return findById(id).orElseThrow();
    }

    /**
     * Atualizacao "completa" usada pela tabela editavel: todos os campos sao
     * gravados como vierem (inclusive nulos), pois a linha representa o
     * estado atual e completo do registro na tela.
     */
    public Database updateCompleto(long id, String nome, long appId, Boolean lgpdScan, String observacao,
                                    String statusVirtualizado, String statusDatabase, String descricao) {
        int linhas = jdbc.update(
                "UPDATE ls_database SET database_name = ?, id_aplicacao = ?, lgpd_scan = ?, " +
                "ds_observacao = ?, status_virtualizado = ?, status_database = ?, descricao = ? " +
                "WHERE id = ?",
                nome, appId, lgpdScan, observacao, statusVirtualizado, statusDatabase, descricao, id);
        if (linhas == 0) {
            return null;
        }
        return findById(id).orElseThrow();
    }

    public boolean delete(long id) {
        int linhas = jdbc.update("DELETE FROM ls_database WHERE id = ?", id);
        return linhas > 0;
    }
}
