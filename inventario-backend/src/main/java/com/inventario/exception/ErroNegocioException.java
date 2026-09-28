package com.inventario.exception;

/**
 * Erro esperado (ex: registro nao encontrado, FK invalida) para exibir uma
 * mensagem amigavel ao usuario, sem stacktrace - equivalente a ErroNegocio
 * na versao Python.
 */
public class ErroNegocioException extends RuntimeException {
    public ErroNegocioException(String mensagem) {
        super(mensagem);
    }
}
