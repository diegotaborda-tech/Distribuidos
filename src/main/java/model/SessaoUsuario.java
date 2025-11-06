package model;

/**
 * Classe Singleton para gerenciar a sessão do usuário, incluindo o token JWT.
 * Esta classe armazena o token apenas em memória.
 */
public class SessaoUsuario {

    private static SessaoUsuario instance;
    private String token;
    private String role;
    // Você pode adicionar outros dados do usuário aqui, como nome, id, etc.

    // O construtor é privado para impedir a criação de novas instâncias
    private SessaoUsuario() {}

    /**
     * Método para obter a única instância da sessão.
     * @return A instância de SessaoUsuario.
     */
    public static synchronized SessaoUsuario getInstance() {
        if (instance == null) {
            instance = new SessaoUsuario();
        }
        return instance;
    }

    /**
     * Salva o token JWT na sessão.
     * @param token O token recebido do servidor.
     */
    public void salvarToken(String token) {
        this.token = token;
    }

    /**
     * Recupera o token JWT salvo.
     * @return O token JWT, ou null se não houver sessão.
     */
    public String getToken() {
        return token;
    }

    /**
     * Limpa a sessão, efetivamente fazendo o logout do usuário.
     */
    public void limparSessao() {
        this.token = null;
        // Limpe outros dados do usuário se houver
    }

    public void setRole(String role) {
        this.role = role;
    }
}