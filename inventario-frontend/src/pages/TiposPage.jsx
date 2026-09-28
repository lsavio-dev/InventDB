import { useEffect, useState } from "react";
import { listarTipos, criarTipo, atualizarTipo, excluirTipo } from "../api/inventario";
import { mensagemErro } from "../api/client";
import Alert from "../components/Alert";

export default function TiposPage() {
  const [tipos, setTipos] = useState([]);
  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState("");
  const [novoNome, setNovoNome] = useState("");
  const [edicaoId, setEdicaoId] = useState(null);
  const [edicaoNome, setEdicaoNome] = useState("");

  useEffect(() => {
    carregar();
  }, []);

  async function carregar() {
    try {
      setTipos(await listarTipos());
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  async function handleAdicionar(ev) {
    ev.preventDefault();
    setErro("");
    setSucesso("");
    if (!novoNome.trim()) {
      setErro("Informe um nome.");
      return;
    }
    try {
      await criarTipo(novoNome.trim());
      setNovoNome("");
      setSucesso("Tipo adicionado.");
      carregar();
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  function iniciarEdicao(tipo) {
    setEdicaoId(tipo.id);
    setEdicaoNome(tipo.tipoAplicacao);
  }

  async function salvarEdicao(id) {
    setErro("");
    try {
      await atualizarTipo(id, edicaoNome.trim());
      setEdicaoId(null);
      setSucesso("Tipo atualizado.");
      carregar();
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  async function handleExcluir(id) {
    setErro("");
    try {
      await excluirTipo(id);
      setSucesso("Tipo excluído.");
      carregar();
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  return (
    <div>
      <h2>Tipos de Aplicação</h2>
      <Alert tipo="error" onClose={() => setErro("")}>
        {erro}
      </Alert>
      <Alert tipo="success" onClose={() => setSucesso("")}>
        {sucesso}
      </Alert>

      <form className="inline-form" onSubmit={handleAdicionar}>
        <input
          type="text"
          placeholder="Nome do novo tipo"
          value={novoNome}
          onChange={(e) => setNovoNome(e.target.value)}
        />
        <button type="submit">➕ Adicionar</button>
      </form>

      <table className="simple-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nome</th>
            <th style={{ width: 160 }}>Ações</th>
          </tr>
        </thead>
        <tbody>
          {tipos.map((t) => (
            <tr key={t.id}>
              <td>{t.id}</td>
              <td>
                {edicaoId === t.id ? (
                  <input
                    type="text"
                    value={edicaoNome}
                    onChange={(e) => setEdicaoNome(e.target.value)}
                  />
                ) : (
                  t.tipoAplicacao
                )}
              </td>
              <td>
                {edicaoId === t.id ? (
                  <>
                    <button onClick={() => salvarEdicao(t.id)}>Salvar</button>{" "}
                    <button onClick={() => setEdicaoId(null)}>Cancelar</button>
                  </>
                ) : (
                  <>
                    <button onClick={() => iniciarEdicao(t)}>✏️</button>{" "}
                    <button onClick={() => handleExcluir(t.id)}>🗑️</button>
                  </>
                )}
              </td>
            </tr>
          ))}
          {tipos.length === 0 && (
            <tr>
              <td colSpan={3} className="muted">
                Nenhum tipo cadastrado ainda.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
