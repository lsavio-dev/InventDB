package com.inventario.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    public int processadas = 0;
    public int tiposCriados = 0;
    public int appsCriados = 0;
    public int dbsCriados = 0;
    public int dbsPulados = 0;
    public List<String> erros = new ArrayList<>();
}
