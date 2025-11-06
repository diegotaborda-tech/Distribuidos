package ui;

import com.google.gson.Gson;
import model.Login;
import model.NetworkUtil;
import model.Requisicao;
import model.Resposta;
import model.SessaoUsuario;
import model.TokenUtil;
import model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;

public class TelaLogin extends JFrame {
    private JTextField campoUsuario;
    private JPasswordField campoSenha;
    private JButton botaoEntrar;
    private JButton botaoCriarUsuario;
    private Gson gson = new Gson();
    private Socket socket;

    public TelaLogin(Socket socket) {
        super("Login");
        this.socket = socket;

        // Configurações da Janela
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Layout e Componentes
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Label e Campo de Usuário
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Usuário:"), gbc);
        gbc.gridx = 1;
        campoUsuario = new JTextField(15);
        add(campoUsuario, gbc);

        // Label e Campo de Senha
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        campoSenha = new JPasswordField(15);
        add(campoSenha, gbc);

        // Painel de botões para melhor organização
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botaoEntrar = new JButton("Entrar");
        botaoCriarUsuario = new JButton("Criar Usuário");
        buttonPanel.add(botaoEntrar);
        buttonPanel.add(botaoCriarUsuario);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);
        
        // Listeners dos botões
        botaoEntrar.addActionListener(e -> login());
        botaoCriarUsuario.addActionListener(e -> criarUsuario());
    }

    private void criarUsuario() {
        String login = campoUsuario.getText();
        String senha = new String(campoSenha.getPassword());

        setBotoesAtivados(false, "Criando...");

        Usuario usuario = new Usuario(login, senha);
        Requisicao criarLogin = new Requisicao("CRIAR_USUARIO", usuario, "usuario");
        String jsonReq = gson.toJson(criarLogin);

        new Thread(() -> {
            try {
                // 1. Recebe a resposta como uma String pura
                String respostaJson = NetworkUtil.sendJson(socket, jsonReq, gson);
                System.out.println("Recebido: " + respostaJson);
                // 2. Converte (cast) a String para o objeto Resposta usando Gson
                Resposta resposta = gson.fromJson(respostaJson, Resposta.class);

                if (resposta != null && "201".equals(resposta.getStatus())) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Usuário criado com sucesso! Agora você pode fazer login.", "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE));
                            limparCampos();
                } else {
                    String mensagemErro = (resposta != null) ? resposta.getMensagem() : "Resposta inválida do servidor.";
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, mensagemErro,
                            "Erro ao criar usuário", JOptionPane.ERROR_MESSAGE));
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "Erro de comunicação: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE));
            } finally {
                // Garante que os botões sejam reativados
                SwingUtilities.invokeLater(() -> setBotoesAtivados(true, null));
            }
        }).start();
    }

    private void login() {
        String usuario = campoUsuario.getText();
        String senha = new String(campoSenha.getPassword());

        setBotoesAtivados(false, "Verificando...");
        
        Login login = new Login(usuario, senha, "LOGIN");
        String jsonReq = gson.toJson(login);
        //System.out.println(jsonReq);

        new Thread(() -> {
            try {
                // 1. Recebe a resposta como uma String pura
                String respostaJson = NetworkUtil.sendJson(socket, jsonReq, gson);
                System.out.println("Recebido: " + respostaJson);

                // 2. Converte (cast) a String para o objeto Resposta usando Gson
                Resposta resposta = gson.fromJson(respostaJson, Resposta.class);

                if (resposta != null && "200".equals(resposta.getStatus())) {
                    SessaoUsuario.getInstance().salvarToken(resposta.getToken());
                    
                    if(usuario.equals("admin") && senha.equals("admin")) {
                    	
                    	SwingUtilities.invokeLater(() -> {
                            dispose(); // Fecha a tela de login
                            new TelaAdmin(socket).setVisible(true);
                        });
                    	
                    } else {
                    	
                    	SwingUtilities.invokeLater(() -> {
                            dispose(); // Fecha a tela de login
                            new TelaPrincipal(socket).setVisible(true);
                        });
                    	
                    }
                    
                } else {
                    //System.out.println(resposta);
                    String mensagemErro = (resposta != null) ? resposta.getMensagem() : "Resposta inválida do servidor.";
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, mensagemErro,
                            "Erro de Login", JOptionPane.ERROR_MESSAGE));
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "Erro de comunicação: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE));
            } finally {
                // Garante que os botões sejam reativados
                SwingUtilities.invokeLater(() -> setBotoesAtivados(true, null));
            }
        }).start();
    }
    
    /**
     * Método auxiliar para habilitar/desabilitar e alterar o texto dos botões.
     * @param ativado True para ativar, false para desativar.
     * @param texto Temporário para o botão de ação (ou null para restaurar).
     */
    private void setBotoesAtivados(boolean ativado, String texto) {
        botaoEntrar.setEnabled(ativado);
        botaoCriarUsuario.setEnabled(ativado);
        if (!ativado) {
            // Se o texto for para o botão de login ou criação, define o texto apropriado
            if ("Verificando...".equals(texto)) {
                botaoEntrar.setText(texto);
            } else if ("Criando...".equals(texto)) {
                botaoCriarUsuario.setText(texto);
            }
        } else {
            // Restaura o texto original dos botões
            botaoEntrar.setText("Entrar");
            botaoCriarUsuario.setText("Criar Usuário");
        }
    }
    // Dentro da classe ui/TelaLogin.java

private void limparCampos() {
    campoUsuario.setText("");
    campoSenha.setText("");
    // Se você tivesse outros campos, adicionaria aqui:
    // outroCampoDeTexto.setText("");
    // umaAreaDeTexto.setText("");
}
}