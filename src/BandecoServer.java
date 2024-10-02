

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class BandecoServer {

	public static final int PORT = 666;
	private ServerSocket serverSocket;
	
	public void start() throws IOException {
		
			System.out.println("Servidor iniciado na porta: " + PORT);
			serverSocket = new ServerSocket(PORT);
			loopConexaoCliente();
	}
	
	private void loopConexaoCliente() throws IOException {
		
		while (true) {
			Socket clientSocket = serverSocket.accept();
			System.out.println("Cliente " + clientSocket.getRemoteSocketAddress());
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String msg = in.readLine();
			System.out.println("Guto disse: " + msg);
		}
	}
	
	public static void main (String[] args) {
		
		try {
			BandecoServer server = new BandecoServer();
			server.start();	
			
		} catch (IOException ex) {
			System.out.println("Erro ao iniciar o servidor: " + ex.getMessage());
		}
		
		System.out.println("Servidor finalizado!");
	}
	
}
