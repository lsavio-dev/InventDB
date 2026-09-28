package com.inventario.dto;

import com.inventario.model.Database;

import java.util.List;

public class OverviewDTO {
    public int totalTipos;
    public int totalApps;
    public int totalDatabases;
    public List<AppComDatabasesDTO> apps;
    public List<Database> databasesOrfaos;

    public static class AppComDatabasesDTO {
        public Long id;
        public String aplicacao;
        public String tipoAplicacao;
        public List<Database> databases;
    }
}
