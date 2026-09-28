import { useEffect, useState } from "react";
import {
  listarTipos,
  listarAplicacoes,
  criarTipo,
  criarAplicacao,
  atualizarAplicacao,
  excluirAplicacao,
} from "../api/inventario";
import { mensagemErro } from "../api/client";
import Alert from "../components/Alert";

const SENTINEL_NOVO_TIPO = "➕ Novo tipo...";

export default function AplicacoesPage() {
  const [tipos, setTipos] = useState([]);
  const [apps, setApps] = useState([]);
  const [filtroTipoId, setFiltroTipoId] = useState("");
  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState("");

  const [novoNome, setNovoNome] = useState("");
  const [novoTipoEscolhido, setNovoTipoEscolhido] = useState("");
  const [novoTipoNome, setNovoTipoNome] = useState("");

  const [edicaoId, setEdicaoId] = useState(null);
  const [edicaoNome, setEdicaoNome] = useState("");
  const [edicaoTipoEscolhido, setEdicaoTipoEscolhido] = useState("");
  const [edicaoTipoNome, setEdicaoTipoNome] = useState("");

  useEffect(() => {
    carregarTipos();
  }, []);

  useEffect(() => {
    carregarApps();
  }, [filtroTipoId]);

  async function carregarTipos() {
    try {
      setTipos(await listarTipos());
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  async function carregarApps() {
    try {
      setApps(await listarAplicacoes(filtroTipoId || undefined));
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  async function resolverTipoId(tipoEscolhido, tipoNomeNovo) {
    if (tipoEscolhido === SENTINEL_NOVO_TIPO) {
      if (!tipoNomeNovo.trim()) {
        throw new Error("Informe o nome do novo tipo.");
      }
      const criado = await criarTipo(tipoNomeNovo.trim());
      return criado.id;
    }
    const encontrado = tipos.find((t) => t.tipoAplicacao === tipoEscolhido);
    if (!encontrado) {
      throw new Error("Selecione um tipo.");
    }
    return encontrado.id;
  }

  async function handleAdicionar(ev) {
    ev.preventDefault();
    setErro("");
    setSucesso("");
    if (!novoNome.trim()) {
      setErro("Informe um nome para a aplicação.");
      return;
    }
    try {
      const tipoId = await resolverTipoId(novoTipoEscolhido, novoTipoNome);
      await criarAplicacao(novoNome.trim(), tipoId);
      setNovoNome("");
      setNovoTipoEscolhido("");
      setNovoTipoNome("");
      setSucesso("Aplicação adicionada.");
      carregarTipos();
      carregarApps();
    } catch (e) {
      setErro(e.response ? mensagemErro(e) : e.message);
    }
  }

  function iniciarEdicao(app) {
    setEdicaoId(app.id);
    setEdicaoNome(app.aplicacao);
    setEdicaoTipoEscolhido(app.tipoAplicacao || "");
    setEdicaoTipoNome("");
  }

  async function salvarEdicao(id) {
    setErro("");
    try {
      const tipoId = await resolverTipoId(edicaoTipoEscolhido, edicaoTipoNome);
      await atualizarAplicacao(id, edicaoNome.trim(), tipoId);
      setEdicaoId(null);
      setSucesso("Aplicação atualizada.");
      carregarTipos();
      carregarApps();
    } catch (e) {
      setErro(e.response ? mensagemErro(e) : e.message);
    }
  }

  async function handleExcluir(id) {
    setErro("");
    try {
      await excluirAplicacao(id);
      setSucesso("Aplicação excluída.");
      carregarApps();
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  return (
    <div>
      <h2>Aplicações</h2>
      <Alert tipo="error" onClose={() => setErro("")}>
        {erro}
      </Alert>
      <Alert tipo="success" onClose={() => setSucesso("")}>
        {sucesso}
      </Alert>

      <div className="field">
        <label>Filtrar por tipo</label>
        <select value={filtroTipoId} onChange={(e) => setFiltroTipoId(e.target.value)}>
          <option value="">Todos</option>
          {tipos.map((t) => (
            <option key={t.id} value={t.id}>
              {t.tipoAplicacao}
            </option>
          ))}
        </select>
      </div>

      <form className="card-form" onSubmit={handleAdicionar}>
        <h3>➕ Nova aplicação</h3>
        <div className="field">
          <label>Nome da aplicação</label>
          <input value={novoNome} onChange={(e) => setNovoNome(e.target.value)} />
        </div>
        <div className="field">
          <label>Tipo</label>
          <select
            value={novoTipoEscolhido}
            onChange={(e) => setNovoTipoEscolhido(e.target.value)}
          >
            <option value="">Selecione...</option>
            {tipos.map((t) => (
              <option key={t.id} value={t.tipoAplicacao}>
                {t.tipoAplicacao}
              </option>
            ))}
            <option value={SENTINEL_NOVO_TIPO}>{SENTINEL_NOVO_TIPO}</option>
          </select>
        </div>
        {novoTipoEscolhido === SENTINEL_NOVO_TIPO && (
          <div className="field">
            <label>Nome do novo tipo</label>
            <input value={novoTipoNome} onChange={(e) => setNovoTipoNome(e.target.value)} />
          </div>
        )}
        <button type="submit">Adicionar</button>
      </form>

      <table className="simple-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>Tipo</th>
            <th style={{ width: 320 }}>Ações</th>
          </tr>
        </thead>
        <tbody>
          {apps.map((a) => (
            <tr key={a.id}>
              <td>{a.id}</td>
              <td>
                {edicaoId === a.id ? (
                  <input value={edicaoNome} onChange={(e) => setEdicaoNome(e.target.value)} />
                ) : (
                  a.aplicacao
                )}
              </td>
              <td>
                {edicaoId === a.id ? (
                  <>
                    <select
                      value={edicaoTipoEscolhido}
                      onChange={(e) => setEdicaoTipoEscolhido(e.target.value)}
                    >
                      {tipos.map((t) => (
                        <option key={t.id} value={t.tipoAplicacao}>
                          {t.tipoAplicacao}
                        </option>
                      ))}
                      <option value={SENTINEL_NOVO_TIPO}>{SENTINEL_NOVO_TIPO}</option>
                    </select>
                    {edicaoTipoEscolhido === SENTINEL_NOVO_TIPO && (
                      <input
                        placeholder="Nome do novo tipo"
                        value={edicaoTipoNome}
                        onChange={(e) => setEdicaoTipoNome(e.target.value)}
                      />
                    )}
                  </>
                ) : (
                  a.tipoAplicacao ?? <span className="muted">sem tipo</span>
                )}
              </td>
              <td>
                {edicaoId === a.id ? (
                  <>
                    <button onClick={() => salvarEdicao(a.id)}>Salvar</button>{" "}
                    <button onClick={() => setEdicaoId(null)}>Cancelar</button>
                  </>
                ) : (
                  <>
                    <button onClick={() => iniciarEdicao(a)}>✏️</button>{" "}
                    <button onClick={() => handleExcluir(a.id)}>🗑️</button>
                  </>
                )}
              </td>
            </tr>
          ))}
          {apps.length === 0 && (
            <tr>
              <td colSpan={4} className="muted">
                Nenhuma aplicação cadastrada ainda.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
