import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class BandecoClient implements Runnable {
	private static final String SERVER_ADDRESS = "127.0.0.1";
	private ClientSocket clientSocket;
	private Scanner scanner;
	
	public BandecoClient() {
		scanner = new Scanner(System.in);
	}
	
	public void start() throws IOException {
		final Socket socket = new Socket(SERVER_ADDRESS, BandecoServer.PORT);
		clientSocket = new ClientSocket(socket);
		System.out.println(
	            "Cliente conectado ao servidor no endereço " + SERVER_ADDRESS +
	            " e porta " + BandecoServer.PORT);
		login();
		new Thread(this).start();
		loopMensagem();
	}
	
	private void login() {
        System.out.print("Digite seu login: ");
        final String login = scanner.nextLine();
        clientSocket.setLogin(login);
        clientSocket.sendMsg(login);
    }
	
	private void loopMensagem() throws IOException {
		String msg;
		do {
			System.out.print("Digite uma mensagem (ou 'sair' para encerrar): ");
			msg = scanner.nextLine();
			clientSocket.sendMsg(msg);
		} while(!msg.equalsIgnoreCase(msg));
		clientSocket.close();
	}
	
	@Override
    public void run() {
        String msg;
        while((msg = clientSocket.getMessage())!=null) {
            System.out.println(msg);
        }
    }
	
	public static void main (String[] args) {	
		try {
			BandecoClient client = new BandecoClient();
			client.start();
		} catch (IOException ex) {
			System.out.println("Erro ao iniciar o cliente: " + ex.getMessage());
		}
	}
}
