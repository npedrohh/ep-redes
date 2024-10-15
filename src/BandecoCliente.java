import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

// Cliente da aplicação em rede
public class BandecoCliente implements Runnable {
    public static String ENDERECO_SERVIDOR = "127.0.0.1"; // Endereço da aplicação do servidor
    private SocketCliente socketCliente; // Socket do cliente

    // Construtor da classe
    public BandecoCliente() {

    }

    // Inicializa a aplicação do cliente
    private void start() throws IOException {
        // Criação do socket
        this.socketCliente = new SocketCliente(new Socket(ENDERECO_SERVIDOR, BandecoServidor.PORTA));

        System.out.println("Cliente conectado ao servidor no endereço " + ENDERECO_SERVIDOR + ":" + BandecoServidor.PORTA);

        this.login();

        new Thread(this).start();
        loopMensagem();
    }

    // Define o nome pelo qual o usuário é reconhecido no sistema
    private void login() {
        // Recebe o login da entrada do usuário
        System.out.print("Digite seu login: ");
        String login = new Scanner(System.in).nextLine();

        socketCliente.setLogin(login);
        socketCliente.enviarMensagem(login);
    }

    // Loop que recebe mensagens do cliente e envia para o servidor, finalizando caso o usuário digite "sair"
    private void loopMensagem() {
        String mensagem;
        Scanner scanner = new Scanner(System.in);

        do {
            System.out.print("Digite sua mensagem (ou \"sair\" para encerrar): ");
            mensagem = scanner.nextLine();
            socketCliente.enviarMensagem(mensagem);
        } while (!mensagem.equalsIgnoreCase("sair"));
    }

    public void run() {
        String mensagem;

        while ((mensagem = socketCliente.recebeMensagem()) != null) {
            System.out.println(mensagem);
        }
    }

    // Método principal da classe, chamado quando o código é executado
    public static void main(String[] args) {
        try {
            BandecoCliente cliente = new BandecoCliente();
            cliente.start();
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}
