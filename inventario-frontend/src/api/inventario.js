import client from "./client";

// --- tipo_aplicacao ---------------------------------------------------

export async function listarTipos() {
  const { data } = await client.get("/tipos");
  return data;
}

export async function criarTipo(nome) {
  const { data } = await client.post("/tipos", { nome });
  return data;
}

export async function atualizarTipo(id, nome) {
  const { data } = await client.put(`/tipos/${id}`, { nome });
  return data;
}

export async function excluirTipo(id) {
  await client.delete(`/tipos/${id}`);
}

// --- aplicacao ----------------------------------------------------------

export async function listarAplicacoes(tipoId) {
  const { data } = await client.get("/aplicacoes", {
    params: tipoId ? { tipoId } : {},
  });
  return data;
}

export async function criarAplicacao(nome, tipoId) {
  const { data } = await client.post("/aplicacoes", { nome, tipoId });
  return data;
}

export async function atualizarAplicacao(id, nome, tipoId) {
  const { data } = await client.put(`/aplicacoes/${id}`, { nome, tipoId });
  return data;
}

export async function excluirAplicacao(id) {
  await client.delete(`/aplicacoes/${id}`);
}

// --- database -------------------------------------------------------------

export async function listarDatabases(appId) {
  const { data } = await client.get("/databases", {
    params: appId ? { appId } : {},
  });
  return data;
}

export async function obterVinculacaoDatabases() {
  const { data } = await client.get("/databases/vinculacao");
  return data;
}

export async function sincronizarDatabases(linhas, idsOriginais) {
  const { data } = await client.post("/databases/sync", { linhas, idsOriginais });
  return data;
}

// --- importação em lote -----------------------------------------------

export async function importarArquivo(arquivo) {
  const formData = new FormData();
  formData.append("arquivo", arquivo);
  const { data } = await client.post("/import", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}

export function urlTemplateImportacao() {
  return `${client.defaults.baseURL}/import/template`;
}

// --- visão geral --------------------------------------------------------

export async function obterVisaoGeral() {
  const { data } = await client.get("/overview");
  return data;
}
