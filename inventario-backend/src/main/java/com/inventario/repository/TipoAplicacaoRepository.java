package com.inventario.repository;

import com.inventario.model.TipoAplicacao;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TipoAplicacaoRepository {

    private final JdbcTemplate jdbc;

    public TipoAplicacaoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static TipoAplicacao map(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new TipoAplicacao(rs.getLong("id"), rs.getString("tipo_aplicacao"));
    }

    public List<TipoAplicacao> findAll() {
        return jdbc.query("SELECT * FROM ls_tipo_aplicacao ORDER BY id", TipoAplicacaoRepository::map);
    }

    public Optional<TipoAplicacao> findById(long id) {
        try {
            return Optional.of(jdbc.queryForObject(
                    "SELECT * FROM ls_tipo_aplicacao WHERE id = ?", TipoAplicacaoRepository::map, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<TipoAplicacao> findByNomeIgnoreCase(String nome) {
        List<TipoAplicacao> resultado = jdbc.query(
                "SELECT * FROM ls_tipo_aplicacao WHERE lower(tipo_aplicacao) = lower(?)",
                TipoAplicacaoRepository::map, nome);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public TipoAplicacao insert(String nome) {
        Long id = jdbc.queryForObject(
                "INSERT INTO ls_tipo_aplicacao (tipo_aplicacao) VALUES (?) RETURNING id",
                Long.class, nome);
        return new TipoAplicacao(id, nome);
    }

    public TipoAplicacao update(long id, String nome) {
        int linhas = jdbc.update(
                "UPDATE ls_tipo_aplicacao SET tipo_aplicacao = ? WHERE id = ?", nome, id);
        if (linhas == 0) {
            return null;
        }
        return new TipoAplicacao(id, nome);
    }

    /** @return true se excluiu, false se o id nao existia */
    public boolean delete(long id) {
        try {
            int linhas = jdbc.update("DELETE FROM ls_tipo_aplicacao WHERE id = ?", id);
            return linhas > 0;
        } catch (DataIntegrityViolationException e) {
            throw e; // tratado no service, que converte para ErroNegocioException
        }
    }
}
