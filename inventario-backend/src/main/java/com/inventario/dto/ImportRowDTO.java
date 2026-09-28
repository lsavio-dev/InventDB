package com.inventario.dto;

/** Uma linha lida do CSV/XLSX de importacao em lote. Valores ainda em texto bruto. */
public class ImportRowDTO {
    private String tipoAplicacao;
    private String aplicacao;
    private String databaseName;
    private String lgpdScan;
    private String dsObservacao;
    private String descricao;

    public String getTipoAplicacao() {
        return tipoAplicacao;
    }

    public void setTipoAplicacao(String tipoAplicacao) {
        this.tipoAplicacao = tipoAplicacao;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getLgpdScan() {
        return lgpdScan;
    }

    public void setLgpdScan(String lgpdScan) {
        this.lgpdScan = lgpdScan;
    }

    public String getDsObservacao() {
        return dsObservacao;
    }

    public void setDsObservacao(String dsObservacao) {
        this.dsObservacao = dsObservacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
