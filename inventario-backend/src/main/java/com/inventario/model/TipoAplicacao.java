package com.inventario.model;

public class TipoAplicacao {
    private Long id;
    private String tipoAplicacao;

    public TipoAplicacao() {
    }

    public TipoAplicacao(Long id, String tipoAplicacao) {
        this.id = id;
        this.tipoAplicacao = tipoAplicacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoAplicacao() {
        return tipoAplicacao;
    }

    public void setTipoAplicacao(String tipoAplicacao) {
        this.tipoAplicacao = tipoAplicacao;
    }
}
