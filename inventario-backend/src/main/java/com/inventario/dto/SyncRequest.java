package com.inventario.dto;

import java.util.List;

public class SyncRequest {
    private List<SyncRowDTO> linhas;
    private List<Long> idsOriginais;

    public List<SyncRowDTO> getLinhas() {
        return linhas;
    }

    public void setLinhas(List<SyncRowDTO> linhas) {
        this.linhas = linhas;
    }

    public List<Long> getIdsOriginais() {
        return idsOriginais;
    }

    public void setIdsOriginais(List<Long> idsOriginais) {
        this.idsOriginais = idsOriginais;
    }
}
