package com.inventario.model;

public class Aplicacao {
    private Long id;
    private String aplicacao;
    private Long idTipoAplicacao;
    // Nome do tipo, preenchido via LEFT JOIN apenas para exibicao.
    private String tipoAplicacao;

    public Aplicacao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }

    public Long getIdTipoAplicacao() {
        return idTipoAplicacao;
    }

    public void setIdTipoAplicacao(Long idTipoAplicacao) {
        this.idTipoAplicacao = idTipoAplicacao;
    }

    public String getTipoAplicacao() {
        return tipoAplicacao;
    }

    public void setTipoAplicacao(String tipoAplicacao) {
        this.tipoAplicacao = tipoAplicacao;
    }
}
