package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Resposta {
    @Override
    public String toString() {
        return "Resposta{" +
                "status='" + status + '\'' +
                ", token='" + token + '\'' +
                ", mensagem='" + mensagem + '\'' +
                '}';
    }

    private String status;
    private String token;
    private String mensagem; // novo campo que será serializado para o cliente
    private Object dados; // Usar Object permite que este campo contenha qualquer coisa
    private Object usuario;
    private Object filmes;
    private Object review;
    private List<Usuario> usuarios;
    
    

    public Resposta(String status, Object dados) {
        this.status = status;
        this.dados = dados;
        this.mensagem = computeMensagemFromStatus();
    }
    
    public Resposta(String status, Object dados, String tipo) {
        this.status = status;

        if (tipo.equals("usuario")) {
            this.usuario = dados;
        } else if (tipo.equals("review")) {
            this.review = dados;
        } else if (tipo.equals("filme")) {
            this.filmes = dados;
        }
        this.mensagem = computeMensagemFromStatus();
    }

    public Resposta(String status, List<Usuario> dados, String tipo) {
        this.status = status;

        if (tipo.equals("lista_usuario")) {
            this.usuarios = dados;
        } else if (tipo.equals("lista_review")) {
            this.review = dados;
        } else if (tipo.equals("lista_filme")) {
            this.filmes = dados;
        }
        
    }


    public Resposta(String status) {
        this.status = status;
        this.token = null; // token is optional
        this.mensagem = computeMensagemFromStatus();
    }

    public Resposta() {
        this.status = null;
        this.token = null; // token is optional
        this.mensagem = computeMensagemFromStatus();
    }

    public Resposta(String status, String token) {
        this.status = status;
        this.token = token;
        this.mensagem = computeMensagemFromStatus();
    }

    public boolean isSucesso() {
        return (this.status == "200" || this.status == "201") ? true : false;
    }

    // Retorna o campo mensagem (se foi setado) — usado pelo cliente/UI
    public String getMensagem() {
        return this.mensagem;
    }

    // Permite sobrescrever a mensagem manualmente
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    // Computa uma mensagem padrão a partir do status consultando o arquivo de guia de erros
    private String computeMensagemFromStatus() {
        String caminho = "src/main/java/rsc/guia_erros.txt";
        if (this.status == null) return "";
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
        // atualizar mensagem sempre que o status mudar
        this.mensagem = computeMensagemFromStatus();
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

    public List<Usuario> getListaUsuarios() {
        return usuarios;
    }
}
