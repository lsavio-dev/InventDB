import { useState } from "react";
import VisaoGeralPage from "./pages/VisaoGeralPage";
import TiposPage from "./pages/TiposPage";
import AplicacoesPage from "./pages/AplicacoesPage";
import DatabasesPage from "./pages/DatabasesPage";
import ImportarPage from "./pages/ImportarPage";

const PAGINAS = [
  { chave: "visao-geral", label: "Visão Geral", componente: VisaoGeralPage },
  { chave: "tipos", label: "Tipos de Aplicação", componente: TiposPage },
  { chave: "aplicacoes", label: "Aplicações", componente: AplicacoesPage },
  { chave: "databases", label: "Databases", componente: DatabasesPage },
  { chave: "importar", label: "Importar em Lote", componente: ImportarPage },
];

export default function App() {
  const [paginaAtual, setPaginaAtual] = useState("visao-geral");
  const pagina = PAGINAS.find((p) => p.chave === paginaAtual);
  const Componente = pagina.componente;

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <h1>🗂️ Inventário de Aplicações</h1>
        <nav>
          {PAGINAS.map((p) => (
            <button
              key={p.chave}
              className={p.chave === paginaAtual ? "nav-item active" : "nav-item"}
              onClick={() => setPaginaAtual(p.chave)}
            >
              {p.label}
            </button>
          ))}
        </nav>
      </aside>
      <main className="content">
        <Componente />
      </main>
    </div>
  );
}
