package com.inventario.dto;

import java.util.ArrayList;
import java.util.List;

public class SyncResult {
    public int criados = 0;
    public int atualizados = 0;
    public int excluidos = 0;
    public List<String> erros = new ArrayList<>();
}
