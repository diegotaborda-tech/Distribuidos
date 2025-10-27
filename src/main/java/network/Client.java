package network;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import model.Filme;

public class Client {

    public static void main(String[] args) {
        final String HOST = "127.0.0.1";
        final int PORTA = 12345;
        Gson gson = new Gson();

        try (
            Socket socket = new Socket(HOST,PORTA);
            PrintWriter escritor = new PrintWriter(socket.getOutputStream(),true);
            //ler input do usuario no console
            BufferedReader leitorConsole = new BufferedReader(new InputStreamReader(System.in));
            //ler input do server
            BufferedReader leitorServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            System.out.println("Conectado ao Servidor");
            String mensagemUsuario;

            while (true) {
                System.out.println("User:");
                mensagemUsuario = leitorConsole.readLine();

                //Filme filme = new Filme("Teste", "2024", new String[]{"Ação", "Drama"}, "Um filme de teste", new model.Review[]{});
                //String respostaJson = gson.toJson(filme);
                //escritor.println(respostaJson);
                
                String respostaServidor = leitorServer.readLine();
                System.out.println("Server:"+respostaServidor);
            }
        } catch (IOException e) {
            // TODO: handle exception
        }
    }
}