import { useEffect, useState } from "react";
import { obterVisaoGeral } from "../api/inventario";
import { mensagemErro } from "../api/client";
import Alert from "../components/Alert";

export default function VisaoGeralPage() {
  const [overview, setOverview] = useState(null);
  const [erro, setErro] = useState("");
  const [carregando, setCarregando] = useState(true);
  const [expandido, setExpandido] = useState({});

  useEffect(() => {
    carregar();
  }, []);

  async function carregar() {
    setCarregando(true);
    setErro("");
    try {
      setOverview(await obterVisaoGeral());
    } catch (e) {
      setErro(mensagemErro(e));
    } finally {
      setCarregando(false);
    }
  }

  function toggle(id) {
    setExpandido((prev) => ({ ...prev, [id]: !prev[id] }));
  }

  if (carregando) return <p>Carregando...</p>;

  return (
    <div>
      <h2>Visão Geral do Inventário</h2>
      <Alert tipo="error" onClose={() => setErro("")}>
        {erro}
      </Alert>

      {overview && (
        <>
          <div className="metrics-row">
            <div className="metric-card">
              <div className="metric-value">{overview.totalTipos}</div>
              <div className="metric-label">Tipos de aplicação</div>
            </div>
            <div className="metric-card">
              <div className="metric-value">{overview.totalApps}</div>
              <div className="metric-label">Aplicações</div>
            </div>
            <div className="metric-card">
              <div className="metric-value">{overview.totalDatabases}</div>
              <div className="metric-label">Databases</div>
            </div>
          </div>

          <hr />

          {overview.apps.length === 0 && <p>Nenhuma aplicação cadastrada ainda.</p>}

          {overview.apps.map((app) => (
            <div key={app.id} className="expander">
              <button className="expander-header" onClick={() => toggle("app-" + app.id)}>
                📦 {app.aplicacao} — ({app.tipoAplicacao ?? "sem tipo"})
              </button>
              {expandido["app-" + app.id] && (
                <div className="expander-body">
                  {app.databases.length === 0 ? (
                    <p className="muted">Nenhum database vinculado.</p>
                  ) : (
                    <table className="simple-table">
                      <thead>
                        <tr>
                          <th>Database</th>
                          <th>LGPD scan</th>
                          <th>Observação</th>
                        </tr>
                      </thead>
                      <tbody>
                        {app.databases.map((d) => (
                          <tr key={d.id}>
                            <td>{d.databaseName}</td>
                            <td>{d.lgpdScan === null ? "" : d.lgpdScan ? "true" : "false"}</td>
                            <td>{d.dsObservacao}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}
                </div>
              )}
            </div>
          ))}

          {overview.databasesOrfaos.length > 0 && (
            <div className="expander">
              <button className="expander-header" onClick={() => toggle("orfaos")}>
                ⚠️ Sem aplicação vinculada ({overview.databasesOrfaos.length})
              </button>
              {expandido["orfaos"] && (
                <div className="expander-body">
                  <p className="muted">
                    Databases cujo id_aplicacao não corresponde a nenhuma aplicação existente
                    no momento.
                  </p>
                  <table className="simple-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Database</th>
                        <th>id_aplicacao</th>
                        <th>LGPD scan</th>
                      </tr>
                    </thead>
                    <tbody>
                      {overview.databasesOrfaos.map((d) => (
                        <tr key={d.id}>
                          <td>{d.id}</td>
                          <td>{d.databaseName}</td>
                          <td>{d.idAplicacao}</td>
                          <td>{d.lgpdScan === null ? "" : d.lgpdScan ? "true" : "false"}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
}
