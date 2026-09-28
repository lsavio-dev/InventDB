package com.inventario.service;

import com.inventario.exception.ErroNegocioException;
import com.inventario.model.Aplicacao;
import com.inventario.repository.AplicacaoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AplicacaoService {

    private final AplicacaoRepository repo;

    public AplicacaoService(AplicacaoRepository repo) {
        this.repo = repo;
    }

    public List<Aplicacao> listar(Long tipoId) {
        return repo.findAll(tipoId);
    }

    public Aplicacao criar(String nome, Long tipoId) {
        if (nome == null || nome.isBlank()) {
            throw new ErroNegocioException("Informe um nome para a aplicação.");
        }
        if (tipoId == null) {
            throw new ErroNegocioException("Informe o tipo da aplicação.");
        }
        try {
            return repo.insert(nome.trim(), tipoId);
        } catch (DataIntegrityViolationException e) {
            throw new ErroNegocioException("tipo_aplicacao com id=" + tipoId + " não existe");
        }
    }

    public Aplicacao atualizar(long id, String nome, Long tipoId) {
        try {
            Aplicacao atualizado = repo.update(id, nome == null ? null : nome.trim(), tipoId);
            if (atualizado == null) {
                throw new ErroNegocioException("aplicacao com id=" + id + " não encontrada");
            }
            return atualizado;
        } catch (DataIntegrityViolationException e) {
            throw new ErroNegocioException("tipo_aplicacao com id=" + tipoId + " não existe");
        }
    }

    public void excluir(long id) {
        boolean excluiu;
        try {
            excluiu = repo.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new ErroNegocioException(
                    "Não é possível excluir a aplicação " + id + ": existem databases vinculados a ela.");
        }
        if (!excluiu) {
            throw new ErroNegocioException("aplicacao com id=" + id + " não encontrada");
        }
    }
}
