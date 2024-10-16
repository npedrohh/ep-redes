package app;

import utils.Mensagem;
import utils.SocketCliente;

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

// Servidor da aplicação em rede
public class BandecoServidor {
    public static int PORTA = 4000; // Porta de funcionamento do servidor
    private static ServerSocket socketServidor; // Socket do servidor
    private static LinkedList<SocketCliente> listaClientes; // Lista dos clientes conectados ao servidor
    private static HashMap<String, String> bandecos; // Objeto que armazena o nome e id dos bandecos disponíveis
    private ScheduledExecutorService scheduler; // COMENTAR

    // Método principal da classe, chamado quando o código é executado
    public static void main(String[] args) {
        BandecoServidor servidor = new BandecoServidor();

        // Tenta iniciar o servidor e avisa em caso de erro
        try {
            servidor.start();
        } catch(IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }

        return;
    }

    // Construtor da classe
    public BandecoServidor() {
        listaClientes = new LinkedList<SocketCliente>();
        bandecos = new HashMap<String, String>();
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Definição dos bandecos disponíveis
        bandecos.put("EACH", "1");
        bandecos.put("Central", "2");
        bandecos.put("Quimica", "3");
        bandecos.put("Fisica", "4");
    }

    // Inicialização do servidor
    private void start() throws IOException {
        socketServidor = new ServerSocket(PORTA);
        System.out.println("Servidor de notificações do bandeco iniciado em " + socketServidor.getInetAddress().getHostAddress() + ", na porta " + PORTA + ".");
        scheduler.scheduleAtFixedRate(this::estaAberto, 0, 60000, TimeUnit.MILLISECONDS);
        this.loopConexao();
    }

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

    // Mantém o servidor operando através de um loop infinito que aguarda conexões dos clientes e atende suas requisições
    private void loopConexao() throws IOException {
        try {
            while (true) {
                System.out.println("Aguardando conexão de novo cliente.");

                SocketCliente socketCliente;

                // Realiza a conexão com o socket do cliente a partir do socket do servidor
                try {
                    socketCliente = new SocketCliente(socketServidor.accept());
                    System.out.println("Cliente " + socketCliente.getSocketIP()+ " conectado.");
                } catch(SocketException e) {
                    // Imprime o erro, caso ocorra, e continua a execução do loop
                    System.err.println("Erro ao aceitar conexão do cliente: " + e.getMessage());
                    continue;
                }

                // Criação de uma thread para o novo cliente conectado, impedindo que o servidor fique bloqueado
                try {
                    new Thread(() -> loopMensagem(socketCliente)).start();
                    listaClientes.add(socketCliente);
                } catch(OutOfMemoryError e){
                    System.err.println("Não foi possível criar thread para novo cliente." +
                                        "O servidor possivelmente está sobrecarregdo. A conexão será fechada.\n"
                                        + e.getMessage());
                    socketCliente.close();
                }
            }
        } finally {
            this.stop();
        }
    }

    // Loop que recebe as mensagens e toma ações de acordo com o tipo da mensagem
    private void loopMensagem(SocketCliente socketCliente) {
        try {
            Mensagem mensagem;

            while ((mensagem = socketCliente.recebeMensagem()) != null) {
                final SocketAddress clientIP = socketCliente.getSocketIP();

                switch (mensagem.getTipo()) {
                    case "LOGIN":
                        socketCliente.setLogin(mensagem.getLogin());
                        socketCliente.setIdBandeco(mensagem.getConteudo());
                        System.out.println("Cliente " + clientIP + " logado como " + socketCliente.getLogin() + ".");
                        System.out.println(socketCliente.getLogin() + " logou no bandeco " + bandecos.get(socketCliente.getIdBandeco() + "."));
                        break;

                    case "WHISPER":
                        // acho que funciona colocar a parte de mensagem privada aqui kaue!!
                        // perfeito pedro!
                        break;

                    default:
                        if ("/sair".equalsIgnoreCase(mensagem.getConteudo())) return; // DARIA PRA SER UM BREAK NÃO?
                        System.out.println("Mensagem recebida de " + socketCliente.getLogin() + ": " + mensagem.getConteudo());
                        this.enviarMensagemParaChat(socketCliente, mensagem);
                }
            }
        } finally {
            socketCliente.close();
        }
    }

    // Encaminha a mensagem do servidor para todos os clientes
    private void enviarMensagemParaTodos(Mensagem mensagem) {
        Iterator<SocketCliente> iterator = listaClientes.iterator();
        int total = 0;

        // Percorre a lista de clientes para encaminhar a mensagem
        while (iterator.hasNext()) {
            SocketCliente cliente = iterator.next();

            if(cliente.enviarMensagem(mensagem)) total++;
            else iterator.remove();
        }
        System.out.println("utils.Mensagem encaminhada para " + total + " usuários.");
    }

    // Envia uma mensagem apenas para clientes de um bandeco específico
    private void enviarMensagemParaChat(final SocketCliente sender, Mensagem mensagem) {
        final Iterator<SocketCliente> iterator = listaClientes.iterator();
        int total = 0;

        /*Percorre a lista usando o iterator enquanto existir um próxima elemento (hasNext)
        para processar, ou seja, enquanto não percorrer a lista inteira.*/
        while (iterator.hasNext()) {
            //Obtém o elemento atual da lista para ser processado.
            final SocketCliente cliente = iterator.next();

            if (cliente.getIdBandeco() == null) continue;

            /*Verifica se o elemento atual da lista (cliente) não é o cliente que enviou a mensagem.
            Se não for e o cliente pertencer ao mesmo chat, encaminha a mensagem pra tal cliente.*/
            if (!cliente.equals(sender) && cliente.getIdBandeco().equals(sender.getIdBandeco())) {
                if(cliente.enviarMensagem(mensagem)) total++;
                else iterator.remove();
            }
        }

        System.out.println("Mensagem encaminhada para " + total + " clientes");
        }

    // Fecha o socket do servidor
    private void stop() {
        try {
            System.out.println("Finalizando o servidor.");
            socketServidor.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar socket do servidor: " + e.getMessage());
        }
    }
}