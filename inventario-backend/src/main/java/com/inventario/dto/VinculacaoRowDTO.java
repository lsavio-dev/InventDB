package com.inventario.dto;

public class VinculacaoRowDTO {
    // id em ls_database, se ja existir vinculo. null = ainda nao vinculado.
    public Long id;
    public String databaseName;

    // Contexto vindo de ls_inventario (somente leitura na tela).
    public String hosts;
    public String sgbdTipos;
    public String systemName;
    public String databaseDescription;
    // database_schema (database_name + schema) vindo de ls_database.
    public String databaseSchema;
    public Long totalColunas;
    public String ultimaVarredura;
    // true = veio do agrupamento de ls_inventario; false = so existe em
    // ls_database e nao aparece mais no inventario atual.
    public boolean origemInventario;

    // Vinculo atual (vem de ls_database via LEFT JOIN), tudo nullable.
    public String aplicacao;
    public Boolean lgpdScan;
    public String dsObservacao;
    public String statusVirtualizado;
    public String statusDatabase;
    public String descricao;
}
