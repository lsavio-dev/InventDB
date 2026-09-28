package com.inventario.service;

import com.inventario.exception.ErroNegocioException;
import com.inventario.model.TipoAplicacao;
import com.inventario.repository.TipoAplicacaoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoService {

    private final TipoAplicacaoRepository repo;

    public TipoService(TipoAplicacaoRepository repo) {
        this.repo = repo;
    }

    public List<TipoAplicacao> listar() {
        return repo.findAll();
    }

    public TipoAplicacao criar(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ErroNegocioException("Informe um nome para o tipo.");
        }
        return repo.insert(nome.trim());
    }

    public TipoAplicacao atualizar(long id, String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ErroNegocioException("Informe um nome para o tipo.");
        }
        TipoAplicacao atualizado = repo.update(id, nome.trim());
        if (atualizado == null) {
            throw new ErroNegocioException("tipo_aplicacao com id=" + id + " não encontrado");
        }
        return atualizado;
    }

    public void excluir(long id) {
        boolean excluiu;
        try {
            excluiu = repo.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new ErroNegocioException(
                    "Não é possível excluir o tipo " + id + ": existem aplicações vinculadas a ele.");
        }
        if (!excluiu) {
            throw new ErroNegocioException("tipo_aplicacao com id=" + id + " não encontrado");
        }
    }
}
