package com.inventario.dto;

public class DatabaseRequest {
    private String nome;
    private Long appId;
    private Boolean lgpdScan;
    private String observacao;
    private String statusVirtualizado;
    private String statusDatabase;
    private String descricao;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Boolean getLgpdScan() {
        return lgpdScan;
    }

    public void setLgpdScan(Boolean lgpdScan) {
        this.lgpdScan = lgpdScan;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
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
