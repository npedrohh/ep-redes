package app;

import utils.Mensagem;
import utils.SocketCliente;
import utils.Menu;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

// Cliente da aplicação em rede
public class BandecoCliente implements Runnable {
    //public static String ENDERECO_SERVIDOR = "192.168.1.33"; // Endereço da aplicação do servidor
    public static String ENDERECO_SERVIDOR = "127.0.0.1"; // Para testes no mesmo computador
    public static int PORTA_SERVIDOR = 4000; // Porta de funcionamento da aplicação do servidor
    private SocketCliente socketCliente; // Socket do cliente
    private final Scanner scanner; // Objeto utilizado para ler a entrada do usuário

    // Método principal da classe, chamado quando o código é executado
    public static void main(String[] args) {
        try {
            BandecoCliente cliente = new BandecoCliente();
            cliente.start();
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }

    // Construtor da classe - inicializa o scanner
    public BandecoCliente() {
        scanner = new Scanner(System.in);
    }

    // Inicializa a aplicação do cliente
    private void start() throws IOException {
        // this.socketCliente = new SocketCliente(new Socket(ENDERECO_SERVIDOR, PORTA_SERVIDOR)); // Criação do socket
        this.socketCliente = new SocketCliente(new Socket(ENDERECO_SERVIDOR, PORTA_SERVIDOR)); // Para testes no mesmo computador

        System.out.println("Cliente conectado ao servidor no endereço " + ENDERECO_SERVIDOR + ":" + BandecoServidor.PORTA);

        this.login(); // Realiza o login do usuário
        Menu.atualizar(); // Atualiza a interface do menu no console

        // Criação de uma nova thread que mantém o cliente ativo
        new Thread(this).start();
        loopMensagem();
    }

    // Define o nome pelo qual o usuário é reconhecido no sistema
    private void login() {
        String login = "";

        // Recebe o login da entrada do usuário, caso a entrada seja válida
        do {
            System.out.print("Digite seu login: ");

            if (login.equalsIgnoreCase("servidor"))
                System.out.println("Você é espertinho... אני אוהב את הנושא \"רשתות\" באוניברסיטת סאו פאולו");
        } while ((login = scanner.nextLine()).equalsIgnoreCase("servidor"));

        socketCliente.setLogin(login);

        // Realiza a conexão com um dos bandecos
        final String idBandeco;
        System.out.println("""
                            Lista de bandecos
                            1 - EACH
                            2 - Central
                            3 - Químicas
                            4 - Física""");
        System.out.println("Digite o ID do bandejão que deseja se conectar: ");

        idBandeco = scanner.nextLine();
        socketCliente.setIdBandeco(idBandeco);

        // Envia a mensagem com login e id do bandeco para o servidor
        Mensagem mensagem = new Mensagem("LOGIN", login, idBandeco);
        socketCliente.enviarMensagem(mensagem);
    }

    // Loop que recebe mensagens do cliente e envia para o servidor, finalizando caso o usuário digite "sair"
    private void loopMensagem() {
        String conteudo;

        do {
            conteudo = scanner.nextLine(); // Leitura da entrada do usuário

            Mensagem mensagem = new Mensagem("USUARIO", socketCliente.getLogin(), conteudo); // Criação do objeto mensagem
            Menu.adicionarAoBuffer(mensagem); // Adiciona a nova mensagem ao buffer
            socketCliente.enviarMensagem(mensagem); // Envia a mensagem para o servidor

            Menu.atualizar(); // Atualiza a interface do console
        } while (!conteudo.equalsIgnoreCase("/sair"));
    }

    // Mantém o servidor rodando
    public void run() {
        Mensagem mensagem;

        while((mensagem = socketCliente.recebeMensagem())!=null) {

            Menu.adicionarAoBuffer(mensagem);
            Menu.atualizar();
        }
    }
}
