package utils;

import java.io.Serializable;

public class Mensagem implements Serializable {
    private String tipo; // "LOGIN, "USUARIO", "SERVIDOR"
    private String login;
    private String conteudo;

    public Mensagem(String tipo, String login, String conteudo) {

        this.tipo = tipo;
        this.login = login;
        this.conteudo = conteudo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}