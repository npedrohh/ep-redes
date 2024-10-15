import java.io.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Aplicação servidora de chat utilizando a classe {@link ServerSocket}, 
 * que permite apenas requisições bloqueantes (blocking).
 *
 * @author Manoel Campos da Silva Filho
 */
public class ServidorBandeco {
    /**
     * Porta na qual o servidor vai ficar escutando (aguardando conexões dos clientes).
     * Em um determinado computador só pode haver uma única aplicação servidora
     * escutando em uma porta específica.
     */
    public static final int PORT = 4000;

    /**
     * Objeto que permite ao servidor ficar escutando na porta especificada acima.
     */
    private ServerSocket serverSocket;

    /**
     * Lista de todos os clientes conectados ao servidor.
     */
    private final List<ClienteSocket> ClienteSocketList;
    private HashMap<String, String> bandecos = new HashMap<String, String>();
    private ScheduledExecutorService scheduler;


    public ServidorBandeco() {

        this.scheduler = Executors.newScheduledThreadPool(1);
        ClienteSocketList = new LinkedList<>();


        bandecos.put("EACH", "1");
        bandecos.put("Central", "2");
        bandecos.put("Quimica", "3");
        bandecos.put("Fisica", "4");
    }

    // a função está aberto verifica o horário atual e compara com os horarios de abertura e fechamento do bandeco, além de verificar se falta 30 minutos para fechamento ou abertura do mesmo.
    private void estaAberto() {
        LocalTime now = LocalTime.now();
        if (estaDentroIntervalo(now, LocalTime.of(19, 30, 0)) ||
                estaDentroIntervalo(now, LocalTime.of(10, 45, 0)) ||
                estaDentroIntervalo(now, LocalTime.of(6, 30, 0))) {

            Mensagem msg = new Mensagem("SERVIDOR", "SERVIDOR", "O BANDECO ABRIRÁ EM MEIA HORA!");
            enviarMensagemParaTodos(msg);
            System.out.println("Aviso de servidor enviado: BANDECO ABRIRÁ EM MEIA HORA");

        } else if (estaDentroIntervalo(now, LocalTime.of(17, 30, 0)) ||
                estaDentroIntervalo(now, LocalTime.of(11, 15, 0)) ||
                estaDentroIntervalo(now, LocalTime.of(7, 0, 0))) {

            Mensagem msg = new Mensagem("SERVIDOR", "SERVIDOR", "O BANDECO ESTÁ ABERTO!");
            enviarMensagemParaTodos(msg);
            System.out.println("Aviso de servidor enviado: BANDECO ESTÁ ABERTO");

        } else if (estaDentroIntervalo(now, LocalTime.of(19, 15, 0)) ||
                estaDentroIntervalo(now, LocalTime.of(13, 45, 0)) ||
                estaDentroIntervalo(now, LocalTime.of(7, 30, 0))) {

            Mensagem msg = new Mensagem("SERVIDOR", "SERVIDOR", "O BANDECO FECHARÁ EM MEIA HORA!");
            enviarMensagemParaTodos(msg);
            System.out.println("Aviso de servidor enviado: BANDECO FECHARÁ EM MEIA HORA");

        } else if (estaDentroIntervalo(now, LocalTime.of(19, 45, 0)) ||
                estaDentroIntervalo(now, LocalTime.of(14, 15, 0)) ||
                estaDentroIntervalo(now, LocalTime.of(8, 0, 0))) {

            Mensagem msg = new Mensagem("SERVIDOR", "SERVIDOR", "O BANDECO ACABOU DE FECHAR!");
            enviarMensagemParaTodos(msg);
            System.out.println("Aviso de servidor enviado: BANDECO ESTÁ FECHADO");
        }
    }
    // verifica um intervalo de 30 segundos antes e depois do horário do bandeco, para garantirmos que a mensagem seja entregue aos usuários.
    private boolean estaDentroIntervalo(LocalTime time, LocalTime target) {
        return Math.abs(ChronoUnit.SECONDS.between(time, target)) <= 31;
    }

    /**
     * Executa a aplicação servidora que fica em loop infinito aguardando conexões
     * dos clientes.
     * @param args parâmetros de linha de comando (não usados para esta aplicação)
     */
    public static void main(String[] args) {
        final ServidorBandeco server = new ServidorBandeco();
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }

    /*
     * Inicia a aplicação, criando um socket para o servidor
     * ficar escutando na porta {@link #PORT}.
     *
     * @throws IOException quando um erro de I/O (Input/Output, ou seja, Entrada/Saída) ocorrer,
     *                     como quando o servidor tentar iniciar mas a porta que ele deseja
     *                     escutar já estiver em uso
     */
    private void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println(
                "Servidor de chat bloqueante iniciado no endereço " + serverSocket.getInetAddress().getHostAddress() +
                        " e porta " + PORT);
        scheduler.scheduleAtFixedRate(this::estaAberto, 0, 60000, TimeUnit.MILLISECONDS);
        clientConnectionLoop();
    }

    /*
     * Inicia o loop infinito de espera por conexões dos clientes. Cada vez que um
     * cliente conecta, uma {@link Thread} é criada para executar o méto do
     * {@link #clientMessageLoop(com.manoelcampos.chat.ClienteSocket)} que ficará
     * esperando mensagens do cliente.
     *
     * @throws IOException quando um erro de I/O (Input/Output, ou seja,
     *                     Entrada/Saída) ocorrer, como quando o servidor tentar
     *                     aceitar a conexão de um cliente, mas ele desconectar
     *                     antes disso (porque a conexão dele ou do servidor cairam, por exemplo)
     */
    private void clientConnectionLoop() throws IOException {
        try {
            while (true) {
                System.out.println("Aguardando conexão de novo cliente");

                final ClienteSocket ClienteSocket;
                try {
                    ClienteSocket = new ClienteSocket(serverSocket.accept());
                    System.out.println("Cliente " + ClienteSocket.getRemoteSocketAddress() + " conectado");
                }catch(SocketException e){
                    System.err.println("Erro ao aceitar conexão do cliente. O servidor possivelmente está sobrecarregado:");
                    System.err.println(e.getMessage());
                    continue;
                }

                /*
                Cria uma nova Thread para permitir que o servidor não fique bloqueado enquanto
                atende às requisições de um único cliente.
                */
                try {
                    new Thread(() -> clientMessageLoop(ClienteSocket)).start();
                    ClienteSocketList.add(ClienteSocket);
                }catch(OutOfMemoryError ex){
                    System.err.println(
                            "Não foi possível criar thread para novo cliente. O servidor possivelmente está sobrecarregdo. Conexão será fechada: ");
                    System.err.println(ex.getMessage());
                    ClienteSocket.close();
                }
            }
        } finally{
            /*Se sair do laço de repetição por algum erro, exibe uma mensagem
            indicando que o servidor finalizou e fecha o socket do servidor.*/
            stop();
        }
    }
    /**
     * Método executado sempre que um cliente conectar ao servidor.
     * O método fica em loop aguardando mensagens do cliente,
     * até que este desconecte.
     * A primeira mensagem que o servidor receber após um cliente conectar é o login enviado pelo cliente.
     *
     * @param ClienteSocket socket do cliente, por meio do qual o servidor
     *                     pode se comunicar com ele.
     */
    private void clientMessageLoop(final ClienteSocket ClienteSocket){
        try {


            Mensagem msg;
            while((msg = ClienteSocket.getMsg()) != null){

                final SocketAddress clientIP = ClienteSocket.getRemoteSocketAddress();

                switch (msg.getTipo()) {

                    case "LOGIN":
                        ClienteSocket.setLogin(msg.getLogin());
                        ClienteSocket.setIdBandeco(msg.getConteudo());
                        System.out.println("Cliente " + clientIP + " logado como " + ClienteSocket.getLogin() + ".");
                        System.out.println(ClienteSocket.getLogin() + " logou no bandeco " + bandecos.get(ClienteSocket.getIdBandeco() + "."));
                        break;

                    case "WHISPER":
                        // acho que funciona colocar a parte de mensagem privada aqui kaue!!
                        break;

                    default:
                        if ("/sair".equalsIgnoreCase(msg.getConteudo())) {
                            return;
                        }
                        System.out.println("Mensagem recebida de " + ClienteSocket.getLogin() + ": " + msg.getConteudo());
                        enviarMensagemParaChat(ClienteSocket, msg);

                }
            }
        } finally {
            ClienteSocket.close();
        }
    }

    private void enviarMensagemParaTodos(Mensagem msg) {
        final Iterator<ClienteSocket> iterator = ClienteSocketList.iterator();
        int count = 0;

        /*Percorre a lista usando o iterator enquanto existir um próxima elemento (hasNext)
        para processar, ou seja, enquanto não percorrer a lista inteira.*/
        while (iterator.hasNext()) {
            //Obtém o elemento atual da lista para ser processado.
            final ClienteSocket client = iterator.next();


            if(client.sendMsg(msg))
                count++;
            else iterator.remove();

        }
        System.out.println("Mensagem encaminhada para " + count + " clientes");
    }

    private void enviarMensagemParaChat(final ClienteSocket sender, Mensagem msg) {
        final Iterator<ClienteSocket> iterator = ClienteSocketList.iterator();
        int count = 0;

        /*Percorre a lista usando o iterator enquanto existir um próxima elemento (hasNext)
        para processar, ou seja, enquanto não percorrer a lista inteira.*/
        while (iterator.hasNext()) {
            //Obtém o elemento atual da lista para ser processado.
            final ClienteSocket client = iterator.next();

            if(client.getIdBandeco() == null) continue;
            /*Verifica se o elemento atual da lista (cliente) não é o cliente que enviou a mensagem.
            Se não for, encaminha a mensagem pra tal cliente.*/
            if (!client.equals(sender) && client.getIdBandeco().equals(sender.getIdBandeco())) {

                if(client.sendMsg(msg))
                    count++;
                else iterator.remove();
            }
        }
        System.out.println("Mensagem encaminhada para " + count + " clientes");
    }

    /**
     * Fecha o socket do servidor quando a aplicação estiver sendo finalizada.
     */
    private void stop()  {
        try {
            System.out.println("Finalizando servidor");
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar socket do servidor: " + e.getMessage());
        }
    }
}