package model;

import java.net.*;

import com.google.gson.Gson;

import java.io.*;

public class NetworkUtil {
    public static String sendJson(Socket socket, String json, Gson gson) throws IOException {
    PrintWriter escritor = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
    BufferedReader leitor = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
    escritor.println(json);
    System.out.println("Enviado: " + json);
    //System.out.println("Recebido: " + leitor.readLine());
    return leitor.readLine();
    
}
}
