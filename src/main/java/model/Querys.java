package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Querys {
    private final String urlBanco = "jdbc:sqlite:src/main/java/Database/voteflix.db";
    

    public void inicializarBanco() {

        String createSql = "CREATE TABLE IF NOT EXISTS usuarios ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " usuario TEXT NOT NULL UNIQUE,"
                + " senha_hash TEXT NOT NULL"
                + ");";

        try (Connection conn = DriverManager.getConnection(urlBanco);
                Statement stmt = conn.createStatement()) {
            stmt.execute(createSql); // Create the new table
            System.out.println("Banco de dados inicializado com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o banco de dados: " + e.getMessage());
        }
    }

    public String VerificarCredenciais(String usuario, String senha, String acao) {

        String sql1 = "SELECT * FROM usuarios WHERE usuario = ?";
        String sql2 = "SELECT * FROM usuarios WHERE usuario = ? AND senha = ?";

        try (Connection conn = DriverManager.getConnection(urlBanco)) {
            if(acao.equals("LOGIN")){
            try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                stmt2.setString(1, usuario);
                stmt2.setString(2, senha);
                ResultSet rs2 = stmt2.executeQuery();
                if (rs2.next()) {
                    return "200"; // Login e senha encontrados
                } else {
                    return "403"; // Usuario Não encontrado
                }
            }} else{
            try (PreparedStatement stmt1 = conn.prepareStatement(sql1)) {
                stmt1.setString(1, usuario);
                ResultSet rs1 = stmt1.executeQuery();
                if (rs1.next()) {
                    return "409"; // User já existe
                } else {
                    return "200"; // User livre para ser criado
                }
            }}

            // Check if username and password match
        } catch (SQLException e) {
            System.err.println("Erro ao verificar credenciais: " + e.getMessage());
            return "500";
        }
    }

    public String CriarUsuario(String usuario, String senha) {
        String res = VerificarCredenciais(usuario, senha, "CRIAR_USUARIO");
        if (senha.length() < 3) {
            return "405";
        }
        try {
            if (res == "409" || res == "500") {
                
                return res;
            } else {
                try (Connection conn = DriverManager.getConnection(urlBanco)) {
                    String sql = "INSERT INTO usuarios (usuario, senha) VALUES (?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, usuario);
                        stmt.setString(2, senha);
                        int rowsInserted = stmt.executeUpdate();
                        return rowsInserted > 0 ? "201" : "500";
                    }
                } catch (SQLException e) {
                    System.err.println("Erro no Banco: " + e.getMessage());
                    return "500"; // Erro genérico
                }
            }

        } catch (Exception e) {
            System.err.println("Erro no Banco " + e.getMessage());
            return "500"; // Erro genérico
        }

    }

    public Resposta Login(String usuario, String senha) {
        // if ((usuario == null || senha == null) || (usuario.isEmpty() || senha.isEmpty())) {
        //     Resposta res = new Resposta("400");
        //     return res; // Bad Request
        // }
        //System.out.println("Antes da verificação");
        String verificacao = VerificarCredenciais(usuario, senha, "LOGIN");
        //System.out.println(verificacao);
        try {
            if (verificacao.equals("200")) {
            	String token;
            	if(usuario.equals("admin") && senha.equals("admin")) {
            		token = TokenUtil.generateToken(usuario,"admin");
            	}else{
            		token = TokenUtil.generateToken(usuario,"user");            		
            	}
                Resposta res = new Resposta("200", token);
                res.setMensagem("Sucesso: operação realizada com sucesso");
                return res;
            } else {
                Resposta res = new Resposta(verificacao);
                res.setMensagem("Erro: sem permissão");
                return res; // Erro genérico
            }
        } catch (Exception e) {
            System.err.println("Erro no Banco: " + e.getMessage());
            Resposta res = new Resposta("500", "");
            res.setMensagem("Erro: Falha interna do servidor");
            return res; // Erro genérico
        }
    }

    public Resposta listarFilmes() {
        List<Filme> filmes = new ArrayList<>();
        List<String> generos = new ArrayList<>();
        String sql = "SELECT * FROM Filmes";
        try (Connection conn = DriverManager.getConnection(urlBanco);
                PreparedStatement stmt = conn.prepareStatement(sql);
                // 1. Execute a query e obtenha o ResultSet
                ResultSet rs = stmt.executeQuery()) {

            // 3. Percorra os Resultados
            while (rs.next()) {
                // 4. Extraia os Dados de cada coluna
                int id = rs.getInt("id");
                String titulo = rs.getString("nome");
                String ano = rs.getString("ano");
                String generosComoString = rs.getString("generos");
                String sinopse = rs.getString("sinopse");
                List<String> listaDeGeneros = new ArrayList<>();
                double nota = rs.getDouble("nota");
                int qtd_avaliacoes = rs.getInt("qtd_avaliacoes");
                String diretor = rs.getString("diretor");
                if (generosComoString != null && !generosComoString.isEmpty()) {
                    // 2. Divide a string pela vírgula, criando um array
                    String[] generosArray = generosComoString.split(",");

                    // 3. Converte o array para uma lista
                    listaDeGeneros = Arrays.asList(generosArray);
                }

                String idStr = String.valueOf(id);
                String notaStr = String.valueOf(nota);
                String qtdStr = String.valueOf(qtd_avaliacoes);
                Filme filme = new Filme(idStr, titulo, ano, listaDeGeneros, sinopse, notaStr, qtdStr, diretor);
                filmes.add(filme);
            }
            Resposta res = new Resposta("200", filmes, "filme");
            res.setMensagem("Sucesso: operação realizada com sucesso");
            return res;
        } catch (Exception e) {
            System.err.println("Erro no Banco: " + e.getMessage());
            return new Resposta("500");
        }
    }

    public Resposta listarReviews() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*,f.nome AS nome_filme FROM reviews AS r JOIN filmes AS f ON r.filme_id = f.id;";
        try (Connection conn = DriverManager.getConnection(urlBanco);
                PreparedStatement stmt = conn.prepareStatement(sql);
                // 1. Execute a query e obtenha o ResultSet
                ResultSet rs = stmt.executeQuery()) {

            // 3. Percorra os Resultados
            while (rs.next()) {
                // 4. Extraia os Dados de cada coluna
                int id = rs.getInt("id");
                String filme_id = rs.getString("nome_filme");
                String usuario = rs.getString("usuario");
                String comentario = rs.getString("comentario");
                int avaliacao = rs.getInt("nota");
                String data = rs.getString("data");

                String idStr = String.valueOf(id);
                String avaliacaoStr = String.valueOf(avaliacao);
                Review review = new Review(idStr, filme_id, usuario, avaliacaoStr, data, comentario);
                reviews.add(review);
            }
            return new Resposta("200", reviews);
        } catch (Exception e) {
            System.err.println("Erro no Banco: " + e.getMessage());
            return new Resposta("500");
        }
    }

    public Resposta criarReview(Review review) {

        // System.out.println("Dentro do try do criar review antes do try:");
        // System.out.println("ID Filme: " + review.getId_filme());
        // System.out.println("Nota: " + review.getNota());
        // System.out.println("Título: " + review.getTitulo());
        // System.out.println("Descrição: " + review.getDescricao());

        Resposta res = new Resposta();
        String sql = "INSERT INTO reviews (filme_id, usuario, nota, titulo, comentario, data) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(urlBanco);
            PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
            LocalDate data_hoje = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE; 
            String dataString = data_hoje.format(formatter);

            
            int filmeId = Integer.parseInt(review.getId_filme());
            stmt.setInt(1, filmeId);
            stmt.setString(2, "Diego");
            stmt.setString(3, review.getNota());
            stmt.setString(4, review.getTitulo());
            stmt.setString(5, review.getDescricao());
            stmt.setString(6, dataString);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                res.setStatus("201");
                return  res;
            }
            res.setStatus("500");
            return res;
        } catch (Exception e) {
            res.setStatus("500");
            System.out.println("Erro ao criar review: " + e);
            return res;
        }
    }

    public Resposta alterarSenha(String senha, String user) {
        Resposta resposta = new Resposta();
        
        if (senha.length() < 3) {
            resposta.setStatus("405");
            resposta.setMensagem("Erro: Campos inválidos, verifique o tipo e quantidade de caracteres");
            return resposta;
        }

        String sql = "UPDATE usuarios SET senha = ? WHERE usuario = ?";
        
        try (Connection conn = DriverManager.getConnection(urlBanco);
            PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
        
            stmt.setString(1, senha);
            stmt.setString(2, user);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                resposta.setStatus("200");
                resposta.setMensagem("Sucesso: operação realizada com sucesso");
                return resposta;
            } else {
                resposta.setStatus("500");
                return resposta;
            }

        } catch (Exception e) {
            System.err.println("Problema no banco: " + e);
        }

        return resposta;
    }

    public Resposta deletarUsuario(String user) {
        Resposta resposta = new Resposta();
        
        String sql = "DELETE FROM usuarios WHERE usuario = ?";
        
        try (Connection conn = DriverManager.getConnection(urlBanco);
            PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
        
            stmt.setString(1, user);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                resposta.setStatus("200");
                resposta.setMensagem("Sucesso: operação realizada com sucesso");
                return resposta;
            } else {
                resposta.setStatus("500");
                resposta.setMensagem("Erro: Falha interna do servidor");
                return resposta;
            }

        } catch (Exception e) {
            System.err.println("Problema no banco: " + e);
        }

        return resposta;
    }

    public Resposta listarUsuarios() {
        String sql = "SELECT id, usuario FROM usuarios";
        
        // Preparamos a lista que será retornada.
        List<Usuario> listaDeUsuarios = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(urlBanco);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Criamos um objeto Usuario para cada linha
                Usuario user = new Usuario();

                // Populamos o objeto APENAS com os dados públicos
                user.setId(rs.getString("id"));
                user.setNome(rs.getString("usuario")); // Mapeando a coluna 'usuario' para o campo 'nome'
                // A senha (user.setSenha) é intencionalmente deixada como nula.

                listaDeUsuarios.add(user);
            }
            
            // Retornamos a resposta de sucesso, com a lista no campo 'dados'.
            return new Resposta("200", listaDeUsuarios, "lista_usuario");

        } catch (SQLException e) {
            System.err.println("Erro de SQL ao listar usuários: " + e.getMessage());
            // Retorna um erro genérico para o cliente, sem expor detalhes do banco.
            return new Resposta("500");
        }
    }

    // Em model/Querys.java

    public Resposta criarFilme(Filme filme) {
        // Primeiro verifica se já existe um filme com a mesma combinação (título, diretor, ano)
        String sqlVerificar = "SELECT id FROM filmes WHERE nome = ? AND diretor = ? AND ano = ?";
        
        try (Connection conn = DriverManager.getConnection(urlBanco);
             PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificar)) {
            
            stmtVerificar.setString(1, filme.getTitulo());
            stmtVerificar.setString(2, filme.getDiretor());
            stmtVerificar.setString(3, filme.getAno());
            
            ResultSet rs = stmtVerificar.executeQuery();
            if (rs.next()) {
                Resposta res = new Resposta("409");
                res.setMensagem("Já existe um filme com este título, diretor e ano.");
                return res;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar duplicidade: " + e.getMessage());
            Resposta res = new Resposta("500");
            res.setMensagem("Erro ao verificar existência do filme.");
            return res;
        }
        
        // Se não existe, insere o novo filme
        String sql = "INSERT INTO filmes (nome, ano, generos, sinopse, diretor) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(urlBanco);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Converte a List<String> de gêneros de volta para uma string "A,B,C"
            String generosComoString = String.join(",", filme.getGenero());

            stmt.setString(1, filme.getTitulo());
            stmt.setString(2, filme.getAno());
            stmt.setString(3, generosComoString);
            stmt.setString(4, filme.getSinopse());
            stmt.setString(5, filme.getDiretor());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                Resposta res = new Resposta("201");
                res.setMensagem("Filme criado com sucesso.");
                return res;
            }
            Resposta res = new Resposta("500");
            res.setMensagem("Falha ao inserir filme no banco.");
            return res;

        } catch (SQLException e) {
            System.err.println("Erro de SQL ao criar filme: " + e.getMessage());
            if (e.getErrorCode() == 19) { // SQLITE_CONSTRAINT
                Resposta res = new Resposta("409");
                res.setMensagem("Um filme com este título já existe.");
                return res;
            }
            Resposta res = new Resposta("500");
            res.setMensagem("Erro interno do servidor.");
            return res;
        }
    }

    public Resposta editarFilme(Filme filme) {
        // Validações de entrada
        if (filme == null) {
            Resposta res = new Resposta("422");
            res.setMensagem("Erro: Chaves faltantes ou invalidas");
            return res;
        }
        
        if (filme.getId() == null || filme.getId().trim().isEmpty()) {
            Resposta res = new Resposta("422");
            res.setMensagem("Erro: Chaves faltantes ou invalidas");
            return res;
        }
        
        if (filme.getTitulo() == null || filme.getTitulo().trim().isEmpty()) {
            Resposta res = new Resposta("422");
            res.setMensagem("Erro: Chaves faltantes ou invalidas");
            return res;
        }
        
        if (filme.getDiretor() == null || filme.getDiretor().trim().isEmpty()) {
            Resposta res = new Resposta("422");
            res.setMensagem("Erro: Chaves faltantes ou invalidas");
            return res;
        }
        
        if (filme.getAno() == null || filme.getAno().trim().isEmpty()) {
            Resposta res = new Resposta("422");
            res.setMensagem("Erro: Chaves faltantes ou invalidas");
            return res;
        }
        
        if (filme.getGenero() == null || filme.getGenero().isEmpty()) {
            Resposta res = new Resposta("422");
            res.setMensagem("Erro: Chaves faltantes ou invalidas");
            return res;
        }
        
        // Validação do ano
        try {
            int ano = Integer.parseInt(filme.getAno());
            if (ano < 1888 || ano > 2100) { // Primeiro filme foi em 1888
                Resposta res = new Resposta("405");
                res.setMensagem("Erro: Campos inválidos, verifique o tipo e quantidade de caracteres");
                return res;
            }
        } catch (NumberFormatException e) {
            Resposta res = new Resposta("405");
            res.setMensagem("Erro: Campos inválidos, verifique o tipo e quantidade de caracteres");
            return res;
        }
        
        // Validação do ID
        int filmeId;
        try {
            filmeId = Integer.parseInt(filme.getId());
        } catch (NumberFormatException e) {
            Resposta res = new Resposta("400");
            res.setMensagem("Erro: Operação não encontrada ou inválida");
            return res;
        }
        
        // Verifica se o filme existe
        String sqlVerificar = "SELECT id FROM filmes WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(urlBanco);
             PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificar)) {
            
            stmtVerificar.setInt(1, filmeId);
            ResultSet rs = stmtVerificar.executeQuery();
            
            if (!rs.next()) {
                Resposta res = new Resposta("404");
                res.setMensagem("Erro: Recurso inexistente");
                return res;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar filme: " + e.getMessage());
            Resposta res = new Resposta("500");
            res.setMensagem("Erro: Falha interna do servidor");
            return res;
        }
        
        // Verifica se já existe outro filme com a mesma combinação (título, diretor, ano)
        String sqlVerificarDuplicidade = "SELECT id FROM filmes WHERE nome = ? AND diretor = ? AND ano = ? AND id != ?";
        try (Connection conn = DriverManager.getConnection(urlBanco);
             PreparedStatement stmtDuplicidade = conn.prepareStatement(sqlVerificarDuplicidade)) {
            
            stmtDuplicidade.setString(1, filme.getTitulo());
            stmtDuplicidade.setString(2, filme.getDiretor());
            stmtDuplicidade.setString(3, filme.getAno());
            stmtDuplicidade.setInt(4, filmeId);
            
            ResultSet rs = stmtDuplicidade.executeQuery();
            if (rs.next()) {
                Resposta res = new Resposta("409");
                res.setMensagem("Erro: Recurso ja existe");
                return res;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar duplicidade: " + e.getMessage());
            Resposta res = new Resposta("500");
            res.setMensagem("Erro: Falha interna do servidor");
            return res;
        }
        
        // Atualiza o filme
        String sql = "UPDATE filmes SET nome = ?, ano = ?, generos = ?, sinopse = ?, diretor = ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(urlBanco);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Converte a List<String> de gêneros para uma string "A,B,C"
            String generosComoString = String.join(",", filme.getGenero());

            stmt.setString(1, filme.getTitulo());
            stmt.setString(2, filme.getAno());
            stmt.setString(3, generosComoString);
            stmt.setString(4, filme.getSinopse());
            stmt.setString(5, filme.getDiretor());
            stmt.setInt(6, filmeId);

            int rowsUpdated = stmt.executeUpdate();
            
            if (rowsUpdated > 0) {
                Resposta res = new Resposta("200");
                res.setMensagem("Sucesso: operação realizada com sucesso");
                return res;
            }
            Resposta res = new Resposta("500");
            res.setMensagem("Erro: Falha interna do servidor");
            return res;

        } catch (SQLException e) {
            System.err.println("Erro de SQL ao editar filme: " + e.getMessage());
            
            // Verifica se é um erro de constraint (nome duplicado)
            if (e.getErrorCode() == 19) { // SQLITE_CONSTRAINT
                Resposta res = new Resposta("409");
                res.setMensagem("Erro: Recurso ja existe");
                return res;
            }
            
            Resposta res = new Resposta("500");
            res.setMensagem("Erro: Falha interna do servidor");
            return res;
        }
    }

    public Resposta deletarFilme(String filmeId) {
    String sqlDeleteReviews = "DELETE FROM reviews WHERE filme_id = ?";
    String sqlDeleteFilme = "DELETE FROM filmes WHERE id = ?";
    Connection conn = null; // Declaramos a conexão fora do try para o rollback funcionar

    try {
        int id = Integer.parseInt(filmeId); // Converte o ID para int, como está no DB

        conn = DriverManager.getConnection(urlBanco);
        // --- INÍCIO DA TRANSAÇÃO ---
        conn.setAutoCommit(false);

        // 1. Deletar as Reviews primeiro (devido à Foreign Key)
        try (PreparedStatement stmtReviews = conn.prepareStatement(sqlDeleteReviews)) {
            stmtReviews.setInt(1, id);
            stmtReviews.executeUpdate();
            // Não precisamos verificar o número de linhas, pois pode não haver reviews
        }

        // 2. Deletar o Filme
        int rowsAffected;
        try (PreparedStatement stmtFilme = conn.prepareStatement(sqlDeleteFilme)) {
            stmtFilme.setInt(1, id);
            rowsAffected = stmtFilme.executeUpdate();
        }

        // Se chegamos aqui, ambas as queries funcionaram.
        conn.commit(); // --- FIM DA TRANSAÇÃO (SUCESSO) ---

        if (rowsAffected > 0) {
            Resposta res = new Resposta("200");
            res.setMensagem("Filme e suas reviews foram deletados com sucesso.");
            return res;
        } else {
            Resposta res = new Resposta("404");
            res.setMensagem("Filme não encontrado.");
            return res;
        }

    } catch (NumberFormatException e) {
        Resposta res = new Resposta("400");
        res.setMensagem("ID do filme inválido.");
        return res;
    } catch (SQLException e) {
        // Se algo deu errado, desfazemos TODAS as alterações
        System.err.println("Erro de SQL na transação de exclusão: " + e.getMessage());
        try {
            if (conn != null) {
                conn.rollback(); // --- ROLLBACK DA TRANSAÇÃO (FALHA) ---
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao tentar reverter o rollback: " + ex.getMessage());
        }
        Resposta res = new Resposta("500");
        res.setMensagem("Erro interno no servidor ao deletar filme.");
        return res;
    } finally {
        // Garante que a conexão volte ao modo auto-commit e seja fechada
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar a conexão: " + e.getMessage());
        }
    }
}
}
