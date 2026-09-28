package com.inventario.service;

import com.inventario.dto.OverviewDTO;
import com.inventario.model.Aplicacao;
import com.inventario.model.Database;
import com.inventario.repository.AplicacaoRepository;
import com.inventario.repository.DatabaseRepository;
import com.inventario.repository.TipoAplicacaoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OverviewService {

    private final TipoAplicacaoRepository tipoRepo;
    private final AplicacaoRepository appRepo;
    private final DatabaseRepository dbRepo;

    public OverviewService(TipoAplicacaoRepository tipoRepo, AplicacaoRepository appRepo,
                            DatabaseRepository dbRepo) {
        this.tipoRepo = tipoRepo;
        this.appRepo = appRepo;
        this.dbRepo = dbRepo;
    }

    public OverviewDTO montar() {
        List<Aplicacao> apps = appRepo.findAll(null);
        List<Database> databases = dbRepo.findAll(null);

        OverviewDTO overview = new OverviewDTO();
        overview.totalTipos = tipoRepo.findAll().size();
        overview.totalApps = apps.size();
        overview.totalDatabases = databases.size();

        List<OverviewDTO.AppComDatabasesDTO> appsDto = new ArrayList<>();
        Set<Long> idsAppsValidos = new HashSet<>();
        for (Aplicacao app : apps) {
            idsAppsValidos.add(app.getId());
            OverviewDTO.AppComDatabasesDTO item = new OverviewDTO.AppComDatabasesDTO();
            item.id = app.getId();
            item.aplicacao = app.getAplicacao();
            item.tipoAplicacao = app.getTipoAplicacao();
            item.databases = databases.stream()
                    .filter(d -> d.getIdAplicacao().equals(app.getId()))
                    .toList();
            appsDto.add(item);
        }
        overview.apps = appsDto;

        overview.databasesOrfaos = databases.stream()
                .filter(d -> !idsAppsValidos.contains(d.getIdAplicacao()))
                .toList();

        return overview;
    }
}
