import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;
/**
 * Aplicação cliente de chat utilizando a classe {@link Socket},
 * que permite apenas requisições bloqueantes (blocking).
 *
 * <p>Observe que a classe implementa a interface {@link Runnable}.
 * Com isto, o método {@link #run()} foi incluído (pressionando-se
 * ALT-ENTER após incluir o "implements Runnable")
 * para que ele seja executado por uma nova thread que criamos
 * dentro do {@link #messageLoop()}.
 * O método {@link #run()} fica em loop aguardando
 * mensagens do servidor.</p>
 *
 * @author Manoel Campos da Silva Filho
 */
public class ClienteBandeco implements Runnable {
    /**
     * Endereço IP ou nome DNS para conectar no servidor.
     * O número da porta é obtido diretamente da constante {@link BlockingChatServerApp#PORT}
     * na classe do servidor.
     */
    public static final String SERVER_ADDRESS = "127.0.0.1";

    /**
     * Objeto para capturar dados do teclado e assim
     * permitir que o usuário digite mensagens a enviar.
     */
    private final Scanner scanner;

    /**
     * Objeto que armazena alguns dados do cliente (como o login)
     * e o {@link Socket} que representa a conexão do cliente com o servidor.
     */
    private ClienteSocket ClienteSocket;

    /**
     * Executa a aplicação cliente.
     * Pode-se executar quantas instâncias desta classe desejar.
     * Isto permite ter vários clientes conectados e interagindo
     * por meio do servidor.
     *
     * @param args parâmetros de linha de comando (não usados para esta aplicação)
     */

    public static void main(String[] args) {

        try {
            ClienteBandeco client = new ClienteBandeco();
            client.start();
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }

    /**
     * Instancia um cliente, realizando o mínimo de operações necessárias.
     */
    public ClienteBandeco(){
        scanner = new Scanner(System.in);
    }

    /**
     * Inicia o cliente, conectando ao servidor e
     * entrando no loop de envio e recebimento de mensagens.
     * @throws IOException quando um erro de I/O (Input/Output, ou seja,
     *                     Entrada/Saída) ocorrer, como quando o cliente tentar
     *                     conectar no servidor, mas o servidor não está aberto
     *                     ou o cliente não tem acesso à rede.
     */
    private void start() throws IOException {
        final Socket socket = new Socket(SERVER_ADDRESS, ServidorBandeco.PORT);
        ClienteSocket = new ClienteSocket(socket);
        System.out.println(
                "Cliente conectado ao servidor no endereço " + SERVER_ADDRESS +
                        " e porta " + ServidorBandeco.PORT);

        login();
        Interface.atualizar();
        // escolherBandeco();

        new Thread(this).start();
        messageLoop();
    }

    /**
     * Executa o login no sistema, enviando o login digitado para o servidor.
     * A primeira mensagem que o servidor receber após um cliente conectar é então o login daquele cliente.
     */
    private void login() {

        String login = "";

        do{

            if("servidor".equalsIgnoreCase(login))
                System.out.println("Você é espertinho... אני אוהב את הנושא \"רשתות\" באוניברסיטת סאו פאולו");

            System.out.print("Digite seu login: ");

        } while("servidor".equalsIgnoreCase(login = scanner.nextLine()));

        ClienteSocket.setLogin(login);

        final String idBandeco;
        System.out.println("Digite o ID do bandejão que deseja se conectar:");
        System.out.println("1 - EACH");
        System.out.println("2 - Central");
        System.out.println("3 - Químicas");
        System.out.println("4 - Física");

        idBandeco = scanner.nextLine();
        ClienteSocket.setIdBandeco(idBandeco);

        Mensagem msg = new Mensagem("LOGIN", login, idBandeco);
        ClienteSocket.sendMsg(msg);
    }

    /*
    // adiciona a função que verifica qual o bandeco que o cliente gostaria de se conectar;
    public void escolherBandeco() {
        final String idBandeco;

        System.out.println("Digite o ID do bandejão que deseja se conectar:");
        System.out.println("1 - EACH");
        System.out.println("2 - Central");
        System.out.println("3 - Químicas");
        System.out.println("4 - Física");
        idBandeco = scanner.nextLine();
        ClienteSocket.setIdBandeco(idBandeco);
        ClienteSocket.sendMsg(idBandeco);
    }
     */

    /**
     * Inicia o loop de envio e recebimento de mensagens.
     * O loop é interrompido quando o usuário digitar "sair".
     */
    private void messageLoop() {

        String conteudo;
        do {

            conteudo = scanner.nextLine();

            Mensagem msg = new Mensagem("USUARIO", ClienteSocket.getLogin(), conteudo);
            Interface.adicionarAoBuffer(msg);
            ClienteSocket.sendMsg(msg);

            Interface.atualizar();
        } while(!"/sair".equalsIgnoreCase(conteudo));

        ClienteSocket.close();
    }

    /**
     * Aguarda mensagens do servidor enquanto o socket não for fechado
     * e o cliente não receber uma mensagem null.
     * Se uma mensagem null for recebida, é porque ocorreu erro na conexão com o servidor.
     * Neste caso, podemos encerrar a espera por novas mensagens.
     *
     * <p>
     * O método tem esse nome pois estamos implementando a interface {@link Runnable}
     * na declaração da classe, o que nos obriga a incluir um método com tal nome
     * na nossa classe. Com isto, permitimos que tal método possa ser executado
     * por uma nova thread que criamos no método {@link #messageLoop()},
     * o que facilita a criação da thread.
     * </p>
     */
    @Override
    public void run() {
        Mensagem msg;
        while((msg = ClienteSocket.getMsg())!=null) {

            Interface.adicionarAoBuffer(msg);
            Interface.atualizar();
        }
    }
}