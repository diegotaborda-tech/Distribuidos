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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private static Gson gson = new Gson();
    private TokenUtil tokenUtil = new TokenUtil();
    
    // Gêneros pré-cadastrados válidos
    private static final Set<String> GENEROS_VALIDOS = new HashSet<>(Arrays.asList(
        "Ação", "Aventura", "Comédia", "Drama", "Fantasia", 
        "Ficção Científica", "Terror", "Romance", "Documentário", 
        "Musical", "Animação"
    ));

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

                        resposta.setMensagem("Sucesso: operação realizada com sucesso");
                        String jsonResposta = gson.toJson(resposta);
                        escritor.println(jsonResposta);
                        System.out.println("Enviado: " + jsonResposta);

                        break;
                    } // Dentro do switch(msgRecebida.getOperacao())

                    case "CRIAR_FILME": {
                        if (!TokenUtil.isTokenValido(msgRecebida.getToken())) {
                            Resposta respostaErro = new Resposta("403");
                            respostaErro.setMensagem("Acesso negado. Requer privilégios de administrador.");
                            String jsonResposta = gson.toJson(respostaErro);
                            escritor.println(jsonResposta);
                            System.out.println("Enviado: " + jsonResposta);
                            break;
                        }
                        
                        // Extrai o payload do Filme
                        String dadosJson = gson.toJson(msgRecebida.getFilme());
                        Filme filmeParaCriar = gson.fromJson(dadosJson, Filme.class);
                        
                        // Valida o filme
                        if (!validarFilme(filmeParaCriar)) {
                            Resposta respostaErro = new Resposta("405");
                            respostaErro.setMensagem("Erro: Campos inválidos, verifique o tipo e quantidade de caracteres");
                            String jsonResposta = gson.toJson(respostaErro);
                            escritor.println(jsonResposta);
                            System.out.println("Enviado: " + jsonResposta);
                            break;
                        }

                        Resposta resposta = db.criarFilme(filmeParaCriar);
                        String jsonResposta = gson.toJson(resposta);
                        escritor.println(jsonResposta);
                        System.out.println("Enviado: " + jsonResposta);
                        break;
                    }
                    
                    case "EDITAR_FILME": {
                        if (!TokenUtil.isTokenValido(msgRecebida.getToken())) {
                            Resposta respostaErro = new Resposta("403");
                            respostaErro.setMensagem("Acesso negado. Requer privilégios de administrador.");
                            String jsonResposta = gson.toJson(respostaErro);
                            escritor.println(jsonResposta);
                            System.out.println("Enviado: " + jsonResposta);
                            break;
                        }
                        
                        // Extrai o payload do Filme
                        String dadosJson = gson.toJson(msgRecebida.getFilme());
                        Filme filmeParaEditar = gson.fromJson(dadosJson, Filme.class);
                        
                        // Valida o filme (mesmas regras que criação)
                        if (!validarFilme(filmeParaEditar)) {
                            Resposta respostaErro = new Resposta("405");
                            respostaErro.setMensagem("Erro: Campos inválidos, verifique o tipo e quantidade de caracteres");
                            String jsonResposta = gson.toJson(respostaErro);
                            escritor.println(jsonResposta);
                            System.out.println("Enviado: " + jsonResposta);
                            break;
                        }

                        Resposta resposta = db.editarFilme(filmeParaEditar);
                        resposta.setMensagem("Sucesso: operação realizada com sucesso");
                        String jsonResposta = gson.toJson(resposta);
                        escritor.println(jsonResposta);
                        System.out.println("Enviado: " + jsonResposta);
                        break;
                    }
                    
                    case "EXCLUIR_FILME":
                    case "ADMIN_DELETAR_FILME": {
                        // Verificamos se o token é de um admin
                        if (!TokenUtil.isTokenValido(msgRecebida.getToken())) {
                            Resposta respostaErro = new Resposta("403");
                            respostaErro.setMensagem("Acesso negado. Requer privilégios de administrador.");
                            String jsonResposta = gson.toJson(respostaErro);
                            escritor.println(jsonResposta);
                            System.out.println("Enviado: " + jsonResposta);
                            break;
                        }

                        try {
                            // O ID do filme pode estar no campo 'id' ou no campo 'filme'
                            String filmeIdParaDeletar = msgRecebida.getId();
                            if (filmeIdParaDeletar == null || filmeIdParaDeletar.isEmpty()) {
                                filmeIdParaDeletar = (String) msgRecebida.getFilme();
                            }

                            Resposta resposta = db.deletarFilme(filmeIdParaDeletar);
                            
                            String jsonResposta = gson.toJson(resposta);
                            escritor.println(jsonResposta);
                            System.out.println("Enviado: " + jsonResposta);

                        } catch (Exception e) {
                            Resposta respostaErro = new Resposta("400");
                            respostaErro.setMensagem("Payload (ID do filme) inválido: " + e.getMessage());
                            String jsonResposta = gson.toJson(respostaErro);
                            escritor.println(jsonResposta);
                            System.out.println("Enviado: " + jsonResposta);
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
    
    /**
     * Valida os dados de um filme segundo as regras de negócio.
     * Retorna true se válido, false se inválido.
     */
    private boolean validarFilme(Filme filme) {
        // Validação de campos obrigatórios
        if (filme == null) {
            return false;
        }
        
        // Título: min 3, max 30
        if (filme.getTitulo() == null || filme.getTitulo().trim().isEmpty()) {
            return false;
        }
        String titulo = filme.getTitulo().trim();
        if (titulo.length() < 3 || titulo.length() > 30) {
            return false;
        }
        
        // Ano: min 3, max 4, apenas dígitos
        if (filme.getAno() == null || filme.getAno().trim().isEmpty()) {
            return false;
        }
        String ano = filme.getAno().trim();
        if (!ano.matches("\\d+") || ano.length() < 3 || ano.length() > 4) {
            return false;
        }
        
        // Diretor: min 3, max 30
        if (filme.getDiretor() == null || filme.getDiretor().trim().isEmpty()) {
            return false;
        }
        String diretor = filme.getDiretor().trim();
        if (diretor.length() < 3 || diretor.length() > 30) {
            return false;
        }
        
        // Gêneros: deve ter pelo menos um, e todos devem estar na lista pré-cadastrada
        if (filme.getGenero() == null || filme.getGenero().isEmpty()) {
            return false;
        }
        for (String genero : filme.getGenero()) {
            if (genero == null || genero.trim().isEmpty()) {
                return false;
            }
            if (!GENEROS_VALIDOS.contains(genero.trim())) {
                return false;
            }
        }
        
        // Sinopse: max 250
        if (filme.getSinopse() == null || filme.getSinopse().trim().isEmpty()) {
            return false;
        }
        String sinopse = filme.getSinopse().trim();
        if (sinopse.length() > 250) {
            return false;
        }
        
        return true; // Válido
    }
}