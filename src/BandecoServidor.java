import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;

// Servidor da aplicação em rede
public class BandecoServidor {
    public static int PORTA = 666; // Porta de funcionamento do servidor
    private static ServerSocket socketServidor; // Socket do servidor
    private static LinkedList<SocketCliente> listaClientes; // Lista dos clientes conectados ao servidor

    // Construtor da classe
    public BandecoServidor() {
        listaClientes = new LinkedList<SocketCliente>();
    }

    // Inicialização do servidor
    private void start() throws IOException {
        socketServidor = new ServerSocket(PORTA);
        System.out.println("Servidor de notificações do bandeco iniciado em " + socketServidor.getInetAddress().getHostAddress() + ", na porta " + PORTA + ".");
        loopConexao();
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
                } catch(Error e) {
                    // Imprime o erro, caso ocorra, e fecha o socket do cliente
                    System.err.println("Erro ao criar thread para novo cliente: " + e.getMessage());
                    socketCliente.close();
                }
            }
        } finally {
            this.stop();
        }
    }

    private void loopMensagem(SocketCliente socketCliente) {
        try {
            String mensagem = socketCliente.recebeMensagem();

            // Executa o loop enquanto não ocorrerem erros ou o cliente digitar "sair"
            while (mensagem != null && !(mensagem.equalsIgnoreCase("sair"))) {
                // Verifica se o cliente não possui login e cria um, caso seja o caso
                if (socketCliente.getLogin() == null) {
                    socketCliente.setLogin(mensagem);
                    System.out.println("Cliente " + socketCliente.getSocketIP() + " logado como \"" + socketCliente.getLogin() + "\".");
                    this.enviaMensagem(socketCliente, ("\nCliente " + socketCliente.getLogin() + " logado."));
                } else {
                    System.out.println("Mensagem recebida de " + socketCliente.getLogin() + ": " + mensagem);
                    this.enviaMensagem(socketCliente, ("[" + socketCliente.getLogin() + "]: " + mensagem));
                }

                mensagem = socketCliente.recebeMensagem();
            }

        } finally {
            socketCliente.close();
        }
    }

    // Encaminha a mensagem de um usuário para todos os usuários conectados ao mesmo bandeco
    private void enviaMensagem(SocketCliente remetente, String mensagem) {
        Iterator<SocketCliente> iterator = listaClientes.iterator();
        int total = 0;

        // Percorre a lista de clientes para encaminhar a mensagem
        while (iterator.hasNext()) {
            SocketCliente cliente = iterator.next();

            // Caso o iterator corresponda ao remetente, não encaminha a mensagem
            if (!cliente.equals(remetente)) {
                if (cliente.enviarMensagem(mensagem)) total++;
                else iterator.remove();
            }
        }
        System.out.println("Mensagem encaminhada para " + total + " usuários.");
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
}