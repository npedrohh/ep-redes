import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketCliente implements Closeable {
    private String login;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public SocketCliente(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    // Envia mensagem
    public boolean enviarMensagem(String mensagem) {
        out.println(mensagem);

        // Retorna false se o envio da mensagem deu erro
        return !out.checkError();
    }

    // Recebe mensagem
    public String recebeMensagem() {
        try {
            return in.readLine();
        } catch (IOException e) {
            return null; // Retorna null em caso de erro ao receber a mensagem
        }
    }

    // Retorna true se o socket estiver aberto e false se estiver fechado
    public boolean estaAberto() {
        return !socket.isClosed();
    }

    // Método get que retorna o login (username) do cliente
    public String getLogin() {
        return this.login;
    }

    // Método set que define o login (username) do cliente
    public void setLogin(String login) {
        this.login = login;
    }

    // Método get que retorna o endereço do cliente
    public SocketAddress getSocketIP() {
        return this.socket.getRemoteSocketAddress();
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
