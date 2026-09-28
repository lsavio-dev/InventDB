package com.inventario.repository;

import com.inventario.model.Aplicacao;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AplicacaoRepository {

    private final JdbcTemplate jdbc;

    public AplicacaoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE =
            "SELECT a.*, t.tipo_aplicacao " +
            "FROM ls_aplicacao a " +
            "LEFT JOIN ls_tipo_aplicacao t ON t.id = a.id_tipo_aplicacao ";

    private static Aplicacao map(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Aplicacao a = new Aplicacao();
        a.setId(rs.getLong("id"));
        a.setAplicacao(rs.getString("aplicacao"));
        a.setIdTipoAplicacao(rs.getLong("id_tipo_aplicacao"));
        a.setTipoAplicacao(rs.getString("tipo_aplicacao"));
        return a;
    }

    public List<Aplicacao> findAll(Long tipoId) {
        if (tipoId != null) {
            return jdbc.query(SELECT_BASE + "WHERE a.id_tipo_aplicacao = ? ORDER BY a.id",
                    AplicacaoRepository::map, tipoId);
        }
        return jdbc.query(SELECT_BASE + "ORDER BY a.id", AplicacaoRepository::map);
    }

    public Optional<Aplicacao> findById(long id) {
        try {
            return Optional.of(jdbc.queryForObject(
                    SELECT_BASE + "WHERE a.id = ?", AplicacaoRepository::map, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Aplicacao> findByNomeIgnoreCase(String nome) {
        List<Aplicacao> resultado = jdbc.query(
                SELECT_BASE + "WHERE lower(a.aplicacao) = lower(?)", AplicacaoRepository::map, nome);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public Aplicacao insert(String nome, long tipoId) {
        Long id = jdbc.queryForObject(
                "INSERT INTO ls_aplicacao (aplicacao, id_tipo_aplicacao) VALUES (?, ?) RETURNING id",
                Long.class, nome, tipoId);
        return findById(id).orElseThrow();
    }

    public Aplicacao update(long id, String nome, Long tipoId) {
        int linhas;
        if (tipoId != null) {
            linhas = jdbc.update(
                    "UPDATE ls_aplicacao SET aplicacao = ?, id_tipo_aplicacao = ? WHERE id = ?",
                    nome, tipoId, id);
        } else {
            linhas = jdbc.update("UPDATE ls_aplicacao SET aplicacao = ? WHERE id = ?", nome, id);
        }
        if (linhas == 0) {
            return null;
        }
        return findById(id).orElseThrow();
    }

    public boolean delete(long id) {
        try {
            int linhas = jdbc.update("DELETE FROM ls_aplicacao WHERE id = ?", id);
            return linhas > 0;
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }
}
