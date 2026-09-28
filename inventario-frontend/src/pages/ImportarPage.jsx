import { useState } from "react";
import { importarArquivo, urlTemplateImportacao } from "../api/inventario";
import { mensagemErro } from "../api/client";
import Alert from "../components/Alert";

export default function ImportarPage() {
  const [arquivo, setArquivo] = useState(null);
  const [erro, setErro] = useState("");
  const [resultado, setResultado] = useState(null);
  const [enviando, setEnviando] = useState(false);

  function handleSelecionarArquivo(e) {
    setArquivo(e.target.files[0] || null);
    setResultado(null);
    setErro("");
  }

  async function handleImportar() {
    if (!arquivo) {
      setErro("Selecione um arquivo primeiro.");
      return;
    }
    setErro("");
    setResultado(null);
    setEnviando(true);
    try {
      const dados = await importarArquivo(arquivo);
      setResultado(dados);
    } catch (e) {
      setErro(mensagemErro(e));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div>
      <h2>Importar Aplicações e Databases em Lote</h2>

      <p>
        Envie um arquivo <strong>CSV ou Excel (.xlsx)</strong> com as colunas:
      </p>
      <ul>
        <li>
          <code>tipo_aplicacao</code> (obrigatório)
        </li>
        <li>
          <code>aplicacao</code> (obrigatório)
        </li>
        <li>
          <code>database_name</code> (obrigatório)
        </li>
        <li>
          <code>lgpd_scan</code> (opcional — deixe em branco, ou use verdadeiro/falso)
        </li>
        <li>
          <code>ds_observacao</code> (opcional, até 100 caracteres)
        </li>
        <li>
          <code>descricao</code> (opcional, até 200 caracteres)
        </li>
      </ul>
      <p className="muted">
        Cada linha representa um database. Se o tipo ou a aplicação ainda não existirem, eles
        são criados automaticamente. Databases já existentes na mesma aplicação (mesmo nome)
        são pulados, evitando duplicar caso o mesmo arquivo seja importado de novo.
      </p>

      <a href={urlTemplateImportacao()} download>
        <button>📥 Baixar modelo de planilha (CSV)</button>
      </a>

      <hr />

      <div className="field">
        <input type="file" accept=".csv,.xlsx" onChange={handleSelecionarArquivo} />
      </div>

      <button className="primary" onClick={handleImportar} disabled={enviando || !arquivo}>
        {enviando ? "Importando..." : "✅ Confirmar importação"}
      </button>

      <Alert tipo="error" onClose={() => setErro("")}>
        {erro}
      </Alert>

      {resultado && (
        <>
          <div className="metrics-row">
            <div className="metric-card">
              <div className="metric-value">{resultado.processadas}</div>
              <div className="metric-label">Linhas processadas</div>
            </div>
            <div className="metric-card">
              <div className="metric-value">{resultado.tiposCriados}</div>
              <div className="metric-label">Tipos criados</div>
            </div>
            <div className="metric-card">
              <div className="metric-value">{resultado.appsCriados}</div>
              <div className="metric-label">Aplicações criadas</div>
            </div>
            <div className="metric-card">
              <div className="metric-value">{resultado.dbsCriados}</div>
              <div className="metric-label">Databases criados</div>
            </div>
            <div className="metric-card">
              <div className="metric-value">{resultado.dbsPulados}</div>
              <div className="metric-label">Já existiam</div>
            </div>
          </div>

          {resultado.erros.length > 0 ? (
            <Alert tipo="error">
              <div>
                <strong>{resultado.erros.length} linha(s) com erro:</strong>
                <pre>{resultado.erros.join("\n")}</pre>
              </div>
            </Alert>
          ) : (
            <Alert tipo="success">Importação concluída sem erros.</Alert>
          )}
        </>
      )}
    </div>
  );
}
