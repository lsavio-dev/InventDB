package com.inventario.dto;

/** Corpo de requisicao simples com apenas um nome (usado por tipo_aplicacao). */
public class NomeRequest {
    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
