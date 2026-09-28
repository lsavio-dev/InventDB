package com.inventario.dto;

/**
 * Uma linha da tabela editavel de databases, ja resolvida pelo front-end
 * (ou seja, "aplicacao" e "tipoAplicacao" ja sao os nomes finais - a
 * logica de "novo item" e resolvida em React antes de enviar para a API).
 */
public class SyncRowDTO {
    private Long id; // null = linha nova
    private String databaseName;
    private String aplicacao;
    private String tipoAplicacao; // so usado se a aplicacao for nova
    private Boolean lgpdScan;
    private String dsObservacao;
    private String statusVirtualizado;
    private String statusDatabase;
    private String descricao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }

    public String getTipoAplicacao() {
        return tipoAplicacao;
    }

    public void setTipoAplicacao(String tipoAplicacao) {
        this.tipoAplicacao = tipoAplicacao;
    }

    public Boolean getLgpdScan() {
        return lgpdScan;
    }

    public void setLgpdScan(Boolean lgpdScan) {
        this.lgpdScan = lgpdScan;
    }

    public String getDsObservacao() {
        return dsObservacao;
    }

    public void setDsObservacao(String dsObservacao) {
        this.dsObservacao = dsObservacao;
    }

    public String getStatusVirtualizado() {
        return statusVirtualizado;
    }

    public void setStatusVirtualizado(String statusVirtualizado) {
        this.statusVirtualizado = statusVirtualizado;
    }

    public String getStatusDatabase() {
        return statusDatabase;
    }

    public void setStatusDatabase(String statusDatabase) {
        this.statusDatabase = statusDatabase;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
