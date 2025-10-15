package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Resposta {
    @Override
    public String toString() {
        return "Resposta{" +
                "status='" + status + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

    private String status;
    private String token;
    private Object dados; // Usar Object permite que este campo contenha qualquer coisa
    private Object usuario;
    private Object filme;
    private Object review;

    public Resposta(String status, Object dados) {
        this.status = status;
        this.dados = dados;
    }
    public Resposta(String status, Object dados, String tipo) {
        this.status = status;

        if (tipo.equals("usuario")) {
            this.usuario = dados;
        } else if (tipo.equals("review")) {
            this.review = dados;
        } else if (tipo.equals("filme")) {
            this.filme = dados;
        }
    }

    public Resposta(String status) {
        this.status = status;
        this.token = null; // token is optional
    }

    public Resposta() {
        this.status = null;
        this.token = null; // token is optional
    }

    public Resposta(String status, String token) {
        this.status = status;
        this.token = token;
    }

    public boolean isSucesso() {
        return (this.status == "200" || this.status == "201") ? true : false;
    }

    public String getMensagem() {
        String caminho = "src/main/java/rsc/guia_erros.txt";
        try (BufferedReader br = Files.newBufferedReader(Paths.get(caminho), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";", 3);
                if (parts.length >= 3 && parts[0].trim().equals(this.status)) {
                    return parts[2].trim();
                }
            }
        } catch (IOException e) {
            // fallback message
        }
        return "Erro desconhecido.";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public Object getDados() {
        return dados;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Object getUsuario() {
        return usuario;
    }

    public void setUsuario(Object usuario) {
        this.usuario = usuario;
    }

    public String getErrorMessage(String code) {
        // Deprecated: use getMensagem() instead, which now reads from guia_erros.txt
        return getMensagem();
    }
}
