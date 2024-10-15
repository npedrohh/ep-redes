import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class ClienteSocket implements Closeable {
    private String login;
    private String idBandeco;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public ClienteSocket(final Socket socket) throws IOException {

        this.socket = socket;

        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();

        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public boolean sendMsg(Mensagem msg) {

        try {
            out.writeObject(msg);
            out.flush();
            return true;
        } catch (IOException e) {

            System.out.println("Erro no envio da mensagem: " + e.getMessage());
            return false;
        }
    }

    public Mensagem getMsg() {

        try {
            return (Mensagem) in.readObject();
        } catch (IOException e) {
            System.out.println("Erro no recebimento da mensagem: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            System.out.println("Erro na classe da mensagem: " + e.getMessage());
            return null;
        }
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setIdBandeco(final String idBandeco) {
        this.idBandeco = idBandeco;
    }

    public String getIdBandeco() {
        return idBandeco;
    }

    @Override
    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch(IOException e){
            System.err.println("Erro ao fechar socket: " + e.getMessage());
        }
    }

    public SocketAddress getRemoteSocketAddress(){
        return socket.getRemoteSocketAddress();
    }

    public boolean isOpen(){
        return !socket.isClosed();
    }
}