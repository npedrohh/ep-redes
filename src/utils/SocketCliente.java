package utils;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketCliente implements Closeable {
    private String login; // Nome de usuário do cliente
    private String idBandeco; // Id do bandeco no qual o cliente está conectado
    private Socket socket; // Socket de conexão com o servidor
    private ObjectInputStream in; // Objeto de leitura das mensagens do servidor/outros clientes
    private ObjectOutputStream out; // Objeto de escrita das mensagens do próprio cliente

    // Construtor da classe
    public SocketCliente(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    // Envia mensagem para o servidor
    public boolean enviarMensagem(Mensagem mensagem) {
        try {
            out.writeObject(mensagem);
            out.flush();
            return true;
        } catch (IOException e) {

            System.err.println("Erro no envio da mensagem: " + e.getMessage());
            return false;
        }
    }

    // Recebe mensagem do servidor
    public Mensagem recebeMensagem() {
        try {
            return (Mensagem) in.readObject();
        } catch (IOException e) {
            System.err.println("Erro no recebimento da mensagem: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Erro na classe da mensagem: " + e.getMessage());
            return null;
        }
    }

    // Retorna true se o socket estiver aberto e false se estiver fechado
    public boolean estaAberto() {
        return !socket.isClosed();
    }

    // Método get que retorna o endereço do cliente
    public SocketAddress getSocketIP() {
        return this.socket.getRemoteSocketAddress();
    }

    // Método get que retorna o login do cliente
    public String getLogin() {
        return this.login;
    }

    // Método set que define o login do cliente
    public void setLogin(String login) {
        this.login = login;
    }

    // Método get que retorna o id do bandeco conectado
    public String getIdBandeco() {
        return this.idBandeco;
    }

    // Método set que define o id do bandeco conectado
    public void setIdBandeco(String idBandeco) {
        this.idBandeco = idBandeco;
    }

    public void close() {
        try {
            this.in.close();
            this.out.close();
            this.socket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar o socket: " + e.getMessage());
        }
    }
}
