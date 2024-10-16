package utils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

public class Menu {

    private static LinkedList<Mensagem> buffer = new LinkedList<Mensagem>();

    public Menu(){

    }

    private static void limpaConsole() throws IOException, InterruptedException {

        Scanner scanner = new Scanner(System.in);
        String texto = scanner.next();

        //Limpa a tela no windows, no linux e no MacOS
        if (System.getProperty("os.name").contains("Windows"))
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        else
            Runtime.getRuntime().exec("clear");

    }

    public static void adicionarAoBuffer(Mensagem msg){

        if(buffer.size() > 10){

            buffer.removeFirst();
        }

        buffer.add(msg);
    }

    private static void printMenu(){

        System.out.println("CHAT DO BANDECO ");
        System.out.println("DIGITE /TROCAR PARA TROCAR DE BANDECO");
        System.out.println("       /SAIR PARA DESCONECTAR");
    }

    private static void printMensagens(){

        for (Mensagem msg : buffer) {

            System.out.println("[" + msg.getLogin() + "]: " + msg.getConteudo());
        }
    }

    public static void atualizar(){

        /*
        try {
            limpaConsole();
        } catch (IOException e) {
            System.err.println("Erro ao atualizar a interface: " + e.getMsg());
        } catch (InterruptedException e) {
            System.err.println("Erro de interrupção ao atualizar a interface: " + e.getMsg());
        }
        */

        printMenu();
        System.out.println();

        printMensagens();
        System.out.println();

        System.out.print("Digite: ");
    }

}