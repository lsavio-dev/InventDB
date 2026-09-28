import { useEffect, useState } from "react";
import {
  listarTipos,
  listarAplicacoes,
  obterVinculacaoDatabases,
  sincronizarDatabases,
} from "../api/inventario";
import { mensagemErro } from "../api/client";
import Alert from "../components/Alert";

const SENTINEL_NOVA_APLICACAO = "➕ Nova aplicação...";
const SENTINEL_NOVO_TIPO = "➕ Novo tipo...";
const OPCOES_STATUS_VIRTUALIZADO = ["A iniciar", "Em Andamento", "Virtualizado"];
const OPCOES_STATUS_DATABASE = [
  "Em uso",
  "Manter como referência",
  "Inativo",
  "Em descontinuidade",
];

let proximoIdTemporario = -1;

function paraLinha(row, origemInventario, isNova = false) {
  const id = row.id ?? (isNova ? proximoIdTemporario-- : null);
  return {
    localKey: origemInventario ? `inv:${row.databaseName}` : `extra:${id}`,
    id,
    origemInventario,
    isNova,
    databaseName: row.databaseName || "",
    databaseSchema: row.databaseSchema || "",
    descricao: row.descricao || "",
    aplicacao: row.aplicacao || "",
    novaAplicacao: "",
    tipoAplicacao: "",
    novoTipo: "",
    lgpdScan:
      row.lgpdScan === null || row.lgpdScan === undefined ? "" : row.lgpdScan ? "true" : "false",
    statusVirtualizado: row.statusVirtualizado || "",
    statusDatabase: row.statusDatabase || "",
    dsObservacao: row.dsObservacao || "",
    hosts: row.hosts || "",
    sgbdTipos: row.sgbdTipos || "",
    systemName: row.systemName || "",
    totalColunas: row.totalColunas ?? "",
    ultimaVarredura: row.ultimaVarredura || "",
  };
}

function linhaManualVazia() {
  return paraLinha({ id: proximoIdTemporario--, databaseName: "" }, false, true);
}

function csvEscape(valor) {
  const texto = valor === null || valor === undefined ? "" : String(valor);
  if (/[",\n;]/.test(texto)) {
    return `"${texto.replace(/"/g, '""')}"`;
  }
  return texto;
}

export default function DatabasesPage() {
  const [tipos, setTipos] = useState([]);
  const [apps, setApps] = useState([]);
  const [linhas, setLinhas] = useState([]);
  const [idsOriginais, setIdsOriginais] = useState([]);
  const [filtroTexto, setFiltroTexto] = useState("");
  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState("");
  const [salvando, setSalvando] = useState(false);
  const [resumo, setResumo] = useState(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    carregarTudo();
  }, []);

  async function carregarTudo() {
    setCarregando(true);
    setErro("");
    try {
      const [tiposData, appsData, vinculacao] = await Promise.all([
        listarTipos(),
        listarAplicacoes(),
        obterVinculacaoDatabases(),
      ]);
      setTipos(tiposData);
      setApps(appsData);

      const doInventario = vinculacao.doInventario.map((r) => paraLinha(r, true));
      const foraDoInventario = vinculacao.foraDoInventario.map((r) => paraLinha(r, false));
      const combinadas = [...doInventario, ...foraDoInventario];
      setLinhas(combinadas);

      setIdsOriginais(combinadas.filter((l) => l.id != null).map((l) => l.id));
    } catch (e) {
      setErro(mensagemErro(e));
    } finally {
      setCarregando(false);
    }
  }

  const ordenarAlfabetico = (a, b) => a.localeCompare(b, "pt-BR", { sensitivity: "base" });
  const nomesApp = [...new Set(apps.map((a) => a.aplicacao))].sort(ordenarAlfabetico);
  const nomesTipo = [...new Set(tipos.map((t) => t.tipoAplicacao))].sort(ordenarAlfabetico);

  function atualizarCampo(localKey, campo, valor) {
    setLinhas((prev) =>
      prev.map((l) => (l.localKey === localKey ? { ...l, [campo]: valor } : l))
    );
  }

  function adicionarLinhaManual() {
    setLinhas((prev) => [...prev, linhaManualVazia()]);
  }

  function removerLinha(localKey) {
    setLinhas((prev) => prev.filter((l) => l.localKey !== localKey));
  }

  // Resolve nomes finais (considerando sentinelas de "novo") e devolve o
  // payload pronto para enviar, ou null quando a linha deve ser ignorada
  // (nunca vinculada e ainda em branco).
  function resolverLinha(linha) {
    const nomeApp =
      linha.aplicacao === SENTINEL_NOVA_APLICACAO
        ? linha.novaAplicacao.trim()
        : linha.aplicacao;
    const nomeTipo =
      linha.tipoAplicacao === SENTINEL_NOVO_TIPO
        ? linha.novoTipo.trim()
        : linha.tipoAplicacao;

    const idReal = linha.isNova ? null : linha.id;
    const semAplicacao = !nomeApp;

    if (idReal == null && semAplicacao) {
      return null; // nunca vinculado e ainda em branco: nem envia, nem exclui
    }
    // idReal != null && semAplicacao -> usuario limpou a aplicacao de um
    // vinculo existente: nao entra no payload, e como seu id nao volta na
    // lista enviada, o backend interpreta como exclusao (comparando com
    // idsOriginais).
    if (idReal != null && semAplicacao) {
      return null;
    }
    return {
      id: idReal,
      databaseName: linha.databaseName,
      descricao: linha.descricao || null,
      aplicacao: nomeApp,
      tipoAplicacao: nomeTipo || null,
      lgpdScan: linha.lgpdScan === "" ? null : linha.lgpdScan === "true",
      statusVirtualizado: linha.statusVirtualizado || null,
      statusDatabase: linha.statusDatabase || null,
      dsObservacao: linha.dsObservacao || null,
    };
  }

  async function handleSalvar() {
    setErro("");
    setSucesso("");
    setResumo(null);
    setSalvando(true);
    try {
      const linhasParaEnviar = linhas.map(resolverLinha).filter((p) => p !== null);
      const resultado = await sincronizarDatabases(linhasParaEnviar, idsOriginais);
      setResumo(resultado);
      if (resultado.erros.length === 0) {
        setSucesso("Alterações salvas.");
      }
      await carregarTudo();
    } catch (e) {
      setErro(mensagemErro(e));
    } finally {
      setSalvando(false);
    }
  }

  function handleExportar() {
    const cabecalho = [
      "database_name",
      "database_schema",
      "descricao",
      "host(s)",
      "sgbd",
      "sistema",
      "colunas_detectadas",
      "ultima_varredura",
      "aplicacao",
      "tipo_aplicacao",
      "lgpd_scan",
      "status_virtualizado",
      "status_database",
      "observacao",
      "origem",
    ];
    const linhasCsv = linhas.map((l) => {
      const nomeApp =
        l.aplicacao === SENTINEL_NOVA_APLICACAO ? l.novaAplicacao.trim() : l.aplicacao;
      const nomeTipo =
        l.tipoAplicacao === SENTINEL_NOVO_TIPO ? l.novoTipo.trim() : l.tipoAplicacao;
      return [
        l.databaseName,
        l.databaseSchema,
        l.descricao,
        l.hosts,
        l.sgbdTipos,
        l.systemName,
        l.totalColunas,
        l.ultimaVarredura,
        nomeApp,
        nomeTipo,
        l.lgpdScan,
        l.statusVirtualizado,
        l.statusDatabase,
        l.dsObservacao,
        l.origemInventario ? "inventario" : "manual",
      ]
        .map(csvEscape)
        .join(";");
    });
    const conteudo = [cabecalho.join(";"), ...linhasCsv].join("\n");
    const blob = new Blob(["\uFEFF" + conteudo], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "databases_inventario.csv";
    link.click();
    URL.revokeObjectURL(url);
  }

  const filtro = filtroTexto.trim().toLowerCase();
  const linhasFiltradas = filtro
    ? linhas.filter((l) => l.databaseName.toLowerCase().includes(filtro))
    : linhas;

  if (carregando) return <p>Carregando...</p>;

  return (
    <div>
      <h2>Databases</h2>
      <Alert tipo="error" onClose={() => setErro("")}>
        {erro}
      </Alert>
      <Alert tipo="success" onClose={() => setSucesso("")}>
        {sucesso}
      </Alert>

      <p className="muted">
        Lista de <strong>database_name</strong> distintos detectados em{" "}
        <code>ls_inventario</code> (scan de LGPD), junto com quaisquer vínculos manuais que já
        existiam. Escolha a aplicação de cada um — se ela ainda não existir, selecione "
        {SENTINEL_NOVA_APLICACAO}" e preencha o nome (e o tipo, se também for novo). Para
        desvincular um item já vinculado, limpe o campo Aplicação. Nada é gravado até clicar em
        "Salvar alterações".
      </p>

      <div className="databases-toolbar">
        <div className="field" style={{ marginBottom: 0 }}>
          <label>Filtrar por nome do database</label>
          <input value={filtroTexto} onChange={(e) => setFiltroTexto(e.target.value)} />
        </div>
        <button onClick={handleExportar}>📤 Exportar CSV</button>
      </div>

      <div className="table-scroll table-scroll-tall">
        <table className="editable-table">
          <thead>
            <tr>
              <th>Database</th>
              <th>Database schema</th>
              <th>Descrição</th>
              <th>Host(s)</th>
              <th>SGBD</th>
              <th>Colunas</th>
              <th>Aplicação</th>
              <th>Nova aplicação</th>
              <th>Tipo</th>
              <th>Novo tipo</th>
              <th>LGPD scan</th>
              <th>Status virtualização</th>
              <th>Status database</th>
              <th>Observação</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {linhasFiltradas.map((linha) => (
              <tr key={linha.localKey}>
                <td>
                  {linha.origemInventario ? (
                    <strong>{linha.databaseName}</strong>
                  ) : (
                    <input
                      value={linha.databaseName}
                      onChange={(e) =>
                        atualizarCampo(linha.localKey, "databaseName", e.target.value)
                      }
                    />
                  )}
                </td>
                <td className="muted">{linha.databaseSchema}</td>
                <td>
                  <input
                    maxLength={200}
                    value={linha.descricao}
                    onChange={(e) => atualizarCampo(linha.localKey, "descricao", e.target.value)}
                  />
                </td>
                <td className="muted">{linha.hosts}</td>
                <td className="muted">{linha.sgbdTipos}</td>
                <td className="muted">{linha.totalColunas}</td>
                <td>
                  <select
                    value={linha.aplicacao}
                    onChange={(e) => atualizarCampo(linha.localKey, "aplicacao", e.target.value)}
                  >
                    <option value=""></option>
                    {nomesApp.map((nome) => (
                      <option key={nome} value={nome}>
                        {nome}
                      </option>
                    ))}
                    <option value={SENTINEL_NOVA_APLICACAO}>{SENTINEL_NOVA_APLICACAO}</option>
                  </select>
                </td>
                <td>
                  <input
                    value={linha.novaAplicacao}
                    onChange={(e) =>
                      atualizarCampo(linha.localKey, "novaAplicacao", e.target.value)
                    }
                    disabled={linha.aplicacao !== SENTINEL_NOVA_APLICACAO}
                  />
                </td>
                <td>
                  <select
                    value={linha.tipoAplicacao}
                    onChange={(e) =>
                      atualizarCampo(linha.localKey, "tipoAplicacao", e.target.value)
                    }
                  >
                    <option value=""></option>
                    {nomesTipo.map((nome) => (
                      <option key={nome} value={nome}>
                        {nome}
                      </option>
                    ))}
                    <option value={SENTINEL_NOVO_TIPO}>{SENTINEL_NOVO_TIPO}</option>
                  </select>
                </td>
                <td>
                  <input
                    value={linha.novoTipo}
                    onChange={(e) => atualizarCampo(linha.localKey, "novoTipo", e.target.value)}
                    disabled={linha.tipoAplicacao !== SENTINEL_NOVO_TIPO}
                  />
                </td>
                <td>
                  <select
                    value={linha.lgpdScan}
                    onChange={(e) => atualizarCampo(linha.localKey, "lgpdScan", e.target.value)}
                  >
                    <option value=""></option>
                    <option value="true">true</option>
                    <option value="false">false</option>
                  </select>
                </td>
                <td>
                  <select
                    value={linha.statusVirtualizado}
                    onChange={(e) =>
                      atualizarCampo(linha.localKey, "statusVirtualizado", e.target.value)
                    }
                  >
                    <option value=""></option>
                    {OPCOES_STATUS_VIRTUALIZADO.map((opcao) => (
                      <option key={opcao} value={opcao}>
                        {opcao}
                      </option>
                    ))}
                  </select>
                </td>
                <td>
                  <select
                    value={linha.statusDatabase}
                    onChange={(e) =>
                      atualizarCampo(linha.localKey, "statusDatabase", e.target.value)
                    }
                  >
                    <option value=""></option>
                    {OPCOES_STATUS_DATABASE.map((opcao) => (
                      <option key={opcao} value={opcao}>
                        {opcao}
                      </option>
                    ))}
                  </select>
                </td>
                <td>
                  <input
                    maxLength={100}
                    value={linha.dsObservacao}
                    onChange={(e) =>
                      atualizarCampo(linha.localKey, "dsObservacao", e.target.value)
                    }
                  />
                </td>
                <td>
                  {!linha.origemInventario && (
                    <button onClick={() => removerLinha(linha.localKey)} title="Excluir linha">
                      🗑️
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {linhasFiltradas.length === 0 && (
              <tr>
                <td colSpan={15} className="muted">
                  Nenhum database encontrado com esse filtro.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="table-actions">
        <button onClick={adicionarLinhaManual}>➕ Adicionar registro manual</button>
        <button className="primary" onClick={handleSalvar} disabled={salvando}>
          {salvando ? "Salvando..." : "💾 Salvar alterações"}
        </button>
      </div>

      {resumo && (
        <div className="metrics-row">
          <div className="metric-card">
            <div className="metric-value">{resumo.criados}</div>
            <div className="metric-label">Criados</div>
          </div>
          <div className="metric-card">
            <div className="metric-value">{resumo.atualizados}</div>
            <div className="metric-label">Atualizados</div>
          </div>
          <div className="metric-card">
            <div className="metric-value">{resumo.excluidos}</div>
            <div className="metric-label">Excluídos/desvinculados</div>
          </div>
        </div>
      )}

      {resumo && resumo.erros.length > 0 && (
        <Alert tipo="error">
          <div>
            <strong>{resumo.erros.length} problema(s) encontrado(s):</strong>
            <pre>{resumo.erros.join("\n")}</pre>
          </div>
        </Alert>
      )}
    </div>
  );
}
