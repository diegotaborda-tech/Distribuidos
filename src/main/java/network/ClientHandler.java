package network;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson; // Importar Gson

import ch.qos.logback.core.subst.Token;
import model.Filme;
import model.Login;
import model.Querys;
import model.Requisicao;
import model.Resposta;
import model.Review;
import model.SessaoUsuario;
import model.TokenUtil;
import model.Usuario;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private static Gson gson = new Gson();
    private TokenUtil tokenUtil = new TokenUtil();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {

        System.out.println("Nova thread para o cliente:" + clientSocket.getInetAddress().getHostAddress());

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                PrintWriter escritor = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"),
                        true);

        ) {
            String json;
            Querys db = new Querys();
            db.inicializarBanco();

            while ((json = reader.readLine()) != null) {

                System.out.println("Recebido: " + json);
                Requisicao msgRecebida = gson.fromJson(json, Requisicao.class);

                switch (msgRecebida.getOperacao()) {
                    case "CRIAR_USUARIO": {
                        String userObj = gson.toJson(msgRecebida.getUsuario());

                        Usuario userCreate = gson.fromJson(userObj, Usuario.class);

                        // String ReqObj = gson.toJson(userCreate.getDado)
                        // System.out.println(userCreate.toString());
                        Resposta resposta = new Resposta("");

                        String res = db.CriarUsuario(userCreate.getNome(), userCreate.getSenha());

                        resposta.setStatus(res);
                        if (res.equals("201")) {
                            resposta.setMensagem("Sucesso: Usuário criado com sucesso");
                        } else if (res.equals("409")) {
                            resposta.setMensagem("Erro: Recurso ja existe");
                        } else if (res.equals("405")) {
                            resposta.setMensagem("Erro: Campos inválidos, verifique o tipo e quantidade de caracteres");
                        }

                        String jsonResposta = gson.toJson(resposta);
                        escritor.println(jsonResposta);
                        System.out.println("Enviado: " + jsonResposta);

                        break;
                    }
                    case "LOGIN": {

                        Login userLogin = gson.fromJson(json, Login.class);
                        // System.out.println(userLogin.toString());
                        Resposta resposta = new Resposta("");

                        resposta = db.Login(userLogin.getUsuario(), userLogin.getSenha());

                        // resposta.setStatus(res);
                        String jsonResposta = gson.toJson(resposta);
                        escritor.println(jsonResposta);
                        System.out.println("Enviado: " + jsonResposta);

                        break;
                    }
                    case "LISTAR_FILMES": {

                        // System.out.println("teste");
                        Resposta resposta = new Resposta();
                        resposta = db.listarFilmes();

                        // resposta.setStatus(res);
                        String jsonResposta = gson.toJson(resposta);
                        escritor.println(jsonResposta);
                        System.out.println("Enviado: " + jsonResposta);
                        break;
                    }
                    case "LISTAR_REVIEWS_USUARIO": {

                        String tokenString = msgRecebida.getToken();
                        Resposta resposta = new Resposta();

                        if (!TokenUtil.isTokenValido(tokenString)) {
                            resposta.setStatus("401");
                        } else {

                            resposta = db.listarReviews();
                        }

                        // resposta.setStatus(res);
                        String jsonResposta = gson.toJson(resposta);
                        escritor.println(jsonResposta);
                        System.out.println("Enviado: " + jsonResposta);
                        break;
                    }
                    case "CRIAR_REVIEW": {

                        Requisicao req = gson.fromJson(json, Requisicao.class);
                        Object dados = req.getReview();
                        String dadosJson = gson.toJson(dados);

                        Review review = gson.fromJson(dadosJson, Review.class);

                        // System.out.println("ID Filme: " + review.getId_filme());
                        // System.out.println("Nota: " + review.getNota());
                        // System.out.println("Título: " + review.getTitulo());
                        // System.out.println("Descrição: " + review.getDescricao());
                        Resposta resposta = new Resposta();

                        resposta = db.criarReview(review);
                        escritor.println(resposta);
                        System.out.println("Enviado: " + dadosJson);

                        break;
                    }
                    case "LOGOUT": {
                        String tokenString = msgRecebida.getToken();
                        Resposta resposta = new Resposta();

                        if (!TokenUtil.isTokenValido(tokenString)) {
                            resposta.setStatus("401");
                            resposta.setMensagem("Erro: Token inválido");
                        } else {
                            resposta.setStatus("200");
                            resposta.setMensagem("Sucesso: Operação realizada com sucesso");
                        }
                        String reString = gson.toJson(resposta);
                        escritor.println(reString);
                        System.out.println("Enviado: " + reString);
                        break;
                    }
                    case "EDITAR_PROPRIO_USUARIO": {

                        String tokenString = msgRecebida.getToken();
                        Resposta resposta = new Resposta();

                        if (!TokenUtil.isTokenValido(tokenString)) {
                            resposta.setStatus("401");
                            resposta.setMensagem("Erro: Token inválido");
                        } else {
                            // String res;
                            String userJson = gson.toJson(msgRecebida.getUsuario());
                            Usuario user = gson.fromJson(userJson, Usuario.class);
                            DecodedJWT jwt = TokenUtil.decodeToken(tokenString);

                            String usuarioNome = jwt.getSubject();
                            resposta = db.alterarSenha(user.getSenha(), usuarioNome);
                            // resposta.setStatus("200");
                        }
                        String reString = gson.toJson(resposta);
                        System.out.println("Enviado: " + reString);
                        escritor.println(reString);

                        break;
                    }
                    case "LISTAR_PROPRIO_USUARIO": {
                        String tokenString = msgRecebida.getToken();
                        Resposta resposta = new Resposta();
                        Login user = new Login();

                        if (!TokenUtil.isTokenValido(tokenString)) {
                            user.setStatus("401");
                            user.setMensagem("Erro: Token inválido");
                        } else {
                            DecodedJWT jwt = TokenUtil.decodeToken(tokenString);
                            String usuarioNome = jwt.getSubject();

                            user.setUsuario(usuarioNome);
                            user.setStatus("200");
                            user.setMensagem("Sucesso: operação realizada com sucesso");
                        }

                        String reString = gson.toJson(user);
                        System.out.println("Enviado: " + reString);
                        escritor.println(reString);
                        break;
                    }
                    case "EXCLUIR_PROPRIO_USUARIO": {
                        String tokenString = msgRecebida.getToken();
                        Resposta resposta = new Resposta();

                        if (!TokenUtil.isTokenValido(tokenString)) {
                            resposta.setStatus("401");
                            resposta.setMensagem("Erro: Token inválido");
                        } else {
                            DecodedJWT jwt = TokenUtil.decodeToken(tokenString);
                            String usuarioNome = jwt.getSubject();
                            resposta = db.deletarUsuario(usuarioNome);
                        }
                        String reString = gson.toJson(resposta);
                        System.out.println("Enviado: " + reString);
                        escritor.println(reString);

                        break;
                    }
                    case "LISTAR_USUARIOS": {

                        String tokenString = msgRecebida.getToken();
                        Resposta resposta = new Resposta();

                        if (!TokenUtil.decodeToken(tokenString).getClaim("role").asString().equals("admin")) {
                            resposta.setStatus("403");
                            resposta.setMensagem("Erro: sem permissão");
                        } else if (!TokenUtil.isTokenValido(tokenString)) {
                            resposta.setStatus("401");
                            resposta.setMensagem("Erro: Token inválido");
                        } else {

                            resposta = db.listarUsuarios();
                        }

                        String jsonResposta = gson.toJson(resposta);
                        escritor.println(jsonResposta);
                        System.out.println("Enviado: " + jsonResposta);

                        break;
                    } // Dentro do switch(msgRecebida.getOperacao())

                    case "CRIAR_FILME": {
                        if (TokenUtil.isTokenValido(msgRecebida.getToken())) { // Exemplo de verificação de admin
                            // Extrai o payload do Filme
                            String dadosJson = gson.toJson(msgRecebida.getFilme());
                            Filme filmeParaCriar = gson.fromJson(dadosJson, Filme.class);

                            Resposta resposta = db.criarFilme(filmeParaCriar);
                            escritor.println(gson.toJson(resposta));
                        } else {
                            escritor.println(gson.toJson(
                                    new Resposta("403", "Acesso negado. Requer privilégios de administrador.")));
                        }
                        break;
                    }
                    case "ADMIN_DELETAR_FILME": {
                        // Verificamos se o token é de um admin
                        if (!TokenUtil.isTokenValido(msgRecebida.getToken())) {
                            escritor.println(gson.toJson(
                                    new Resposta("403", "Acesso negado. Requer privilégios de administrador.")));
                            break;
                        }

                        try {
                            // O ID do filme foi enviado no campo 'dados'
                            String filmeIdParaDeletar = (String) msgRecebida.getFilme();

                            Resposta resposta = db.deletarFilme(filmeIdParaDeletar);
                            escritor.println(gson.toJson(resposta));

                        } catch (Exception e) {
                            escritor.println(gson.toJson(new Resposta("400", "Payload (ID do filme) inválido.")));
                        }
                        break;
                    }
                    default:
                        System.out.println("Operação desconhecida: " + msgRecebida.getOperacao());
                        break;
                }
                // System.out.println("Recebido de " + clientSocket.getInetAddress() + ": " +
                // msgRecebida.getConteudo());

                // Mensagem msgResposta = new Mensagem("Servidor", "Mensagem '" +
                // msgRecebida.getConteudo() + "' recebida.");
                // String jsonResposta = gson.toJson(msgResposta);
                // .println(jsonResposta);
            }
        } catch (IOException e) {
            System.err.println("Erro na thread do cliente: " + e.getMessage());
        } finally {
            try {
                System.out.println("Fechando conexão com o cliente: " + clientSocket.getInetAddress());
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}