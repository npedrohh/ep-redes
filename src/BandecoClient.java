import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class BandecoClient {

	private static final String SERVER_ADDRESS = "127.0.0.1";
	private Socket clientSocket;
	private Scanner scanner;
	private PrintWriter out;
	
	public BandecoClient() {
		
		scanner = new Scanner(System.in);
	}
	
	public void start() throws IOException {
		
		clientSocket = new Socket(SERVER_ADDRESS, BandecoServer.PORT);
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		System.out.println("Cliente conectado ao servidor: " + SERVER_ADDRESS + ":" + BandecoServer.PORT);
		loopMensagem();
	}
	
	private void loopMensagem() throws IOException {
		String msg;
		do {
			System.out.print("Digite uma mensagem: ");
			msg = scanner.nextLine();
			out.println(msg);
		} while(!msg.equalsIgnoreCase("sair"));
	}
	
	public static void main (String[] args) {
		
		BandecoClient client = new BandecoClient();
		
		try {
			client.start();
		} catch (IOException ex) {
			System.out.println("Erro ao iniciar o cliente: " + ex.getMessage());
		}
		
		System.out.println("Cliente finalizado!");
	}
	
}
