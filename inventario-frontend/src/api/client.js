import axios from "axios";

// URL base da API Java (Spring Boot). Ajuste se rodar em outra porta/host.
const API_BASE_URL = "http://localhost:8080/api";

const client = axios.create({
  baseURL: API_BASE_URL,
});

// Extrai uma mensagem de erro amigável do corpo de resposta da API
// (o backend Java devolve {"erro": "mensagem"} em caso de falha).
export function mensagemErro(erro) {
  if (erro.response && erro.response.data && erro.response.data.erro) {
    return erro.response.data.erro;
  }
  if (erro.message) {
    return erro.message;
  }
  return "Erro inesperado ao comunicar com a API.";
}

export default client;
