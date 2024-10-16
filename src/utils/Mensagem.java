package utils;

import java.io.Serializable;

// Classe das mensagens, que serão os objetos usados para comunicação entre cliente e servidor
public class Mensagem implements Serializable {
    private String tipo; // "LOGIN, "USUARIO", "SERVIDOR": especifica a semântica dessa mensagem
    private String login; // Nome do usuário que enviou a mensagem
    private String conteudo; // Conteúdo da mensagem

    public Mensagem(String tipo, String login, String conteudo) {

        this.tipo = tipo;
        this.login = login;
        this.conteudo = conteudo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getLogin() {
        return login;
    }

    public String getConteudo() {
        return conteudo;
    }

}