package com.inventario.model;

public class Database {
    private Long id;
    private String databaseName;
    private Long idAplicacao;
    private Boolean lgpdScan;
    private String dsObservacao;
    private String statusVirtualizado;
    private String statusDatabase;
    private String descricao;
    // Concatenacao de database_name + schema, mantida em ls_database.
    private String databaseSchema;
    // Nome da aplicacao, preenchido via LEFT JOIN apenas para exibicao.
    // Fica null quando o database esta "orfao" (id_aplicacao sem correspondencia).
    private String aplicacao;

    public Database() {
    }

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

    public Long getIdAplicacao() {
        return idAplicacao;
    }

    public void setIdAplicacao(Long idAplicacao) {
        this.idAplicacao = idAplicacao;
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

    public String getDatabaseSchema() {
        return databaseSchema;
    }

    public void setDatabaseSchema(String databaseSchema) {
        this.databaseSchema = databaseSchema;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }
}
