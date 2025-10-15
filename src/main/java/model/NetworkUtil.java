package model;

import java.net.*;

import com.google.gson.Gson;

import java.io.*;

public class NetworkUtil {
    public static String sendJson(Socket socket, String json, Gson gson) throws IOException {
    PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
    BufferedReader leitor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    escritor.println(json);
    return leitor.readLine();
    
}
}
