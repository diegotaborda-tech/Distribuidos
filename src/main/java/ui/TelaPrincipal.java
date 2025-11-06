package ui;

import com.auth0.jwt.interfaces.DecodedJWT; // NOVO: Import necessário para decodificar o token
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout; // NOVO: Import para o layout do painel de botões
import java.awt.Font;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;

import javax.swing.*;

public class TelaPrincipal extends JFrame {

    private Socket socket;
    private Gson gson = new Gson();
    // Removido o 'token' daqui, pois pegaremos sempre o mais atual da sessão
    // private String token = SessaoUsuario.getInstance().getToken();

    private DefaultListModel<Filme> modeloListaFilmes;
    private DefaultListModel<Review> modeloListaReviews;
    private JList<Filme> listaFilmesUI;
    private JList<Review> listaReviewsUI;

    public TelaPrincipal(Socket socket) {
        super("Voteflix - Tela Principal");
        this.socket = socket;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        // Garante que o JFrame use BorderLayout
        setLayout(new BorderLayout());

        JTabbedPane abas = new JTabbedPane();

        JPanel painelFilmes = new JPanel(new BorderLayout());
        JPanel painelReviews = new JPanel(new BorderLayout());

        modeloListaFilmes = new DefaultListModel<>();
        modeloListaReviews = new DefaultListModel<>();

        listaFilmesUI = new JList<>(modeloListaFilmes);
        listaReviewsUI = new JList<>(modeloListaReviews);

        listaFilmesUI.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        listaReviewsUI.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JScrollPane scrollPane = new JScrollPane(listaFilmesUI);
        JScrollPane scrollPane2 = new JScrollPane(listaReviewsUI);

        listaFilmesUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        listaFilmesUI.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int idx = listaFilmesUI.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        Filme selected = modeloListaFilmes.getElementAt(idx);
                        openReviewDialog(selected);
                    }
                }
            }
        });

        painelFilmes.add(scrollPane, BorderLayout.CENTER);
        painelReviews.add(scrollPane2, BorderLayout.CENTER);

        abas.addTab("Filmes", painelFilmes);
        abas.addTab("Minhas Reviews", painelReviews);

        add(abas, BorderLayout.CENTER);

        // --- NOVO: Painel de botões do usuário no rodapé ---
        JPanel painelUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Alinha os botões à direita
        JButton botaoVerUsuario = new JButton("Ver Usuário");
        JButton botaoAlterarSenha = new JButton("Alterar Senha");
        JButton botaoLogout = new JButton("Logout");
        JButton botaoExcluirConta = new JButton("Excluir Conta");

        botaoVerUsuario.addActionListener(e -> mostrarUsuarioLogado());
        botaoAlterarSenha.addActionListener(e -> mostrarDialogoAlterarSenha());
         botaoExcluirConta.addActionListener(e -> mostrarDialogoExcluirConta());
        botaoLogout.addActionListener(e -> fazerLogout());

        painelUsuario.add(botaoVerUsuario);
        painelUsuario.add(botaoAlterarSenha);
        painelUsuario.add(botaoExcluirConta);
        painelUsuario.add(botaoLogout);

        add(painelUsuario, BorderLayout.SOUTH); // Adiciona o painel na parte de baixo da janela
        // -------------------------------------------------
    }

    private void mostrarDialogoExcluirConta() {
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Você tem certeza que deseja excluir sua conta?\nEsta ação é permanente e não pode ser desfeita.",
                "Confirmar Exclusão de Conta",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacao == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                try {
                    String token = SessaoUsuario.getInstance().getToken();
                    Requisicao req = new Requisicao("EXCLUIR_PROPRIO_USUARIO", token);
                    String jsonReq = gson.toJson(req);

                    String resJson = NetworkUtil.sendJson(socket, jsonReq, gson);
                    System.out.println("Recebido: " + resJson);

                    Resposta res = gson.fromJson(resJson, Resposta.class);

                    if (res != null && "200".equals(res.getStatus())) {
                        JOptionPane.showMessageDialog(this, "Usuário excluído com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        // Força o logout e o retorno para a tela de conexão
                        SessaoUsuario.getInstance().limparSessao();
                        dispose();
                        SwingUtilities.invokeLater(() -> new TelaConexao().setVisible(true));
                    } else {
                        String msgErro = res != null ? res.getMensagem() : "Resposta inválida do servidor.";
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            msgErro, "Erro ao Excluir Conta", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception e) {
                     SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Erro de comunicação ao excluir a conta: " + e.getMessage(), "Erro de Rede", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        }
    }

    private void mostrarDialogoAlterarSenha() {
        // Usamos um JPasswordField dentro de um JOptionPane para mascarar a senha
        JPasswordField pf = new JPasswordField(20);
        int okCxl = JOptionPane.showConfirmDialog(this, pf, "Digite a nova senha", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (okCxl == JOptionPane.OK_OPTION) {
            String novaSenha = new String(pf.getPassword());
            if (novaSenha.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "A senha não pode estar em branco.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Envia a requisição para o servidor em uma nova thread
            new Thread(() -> {
                try {
                    String token = SessaoUsuario.getInstance().getToken();
                    Usuario usuario = new Usuario(novaSenha);
                    Requisicao req = new Requisicao("EDITAR_PROPRIO_USUARIO",usuario,token,"usuario");

                    String jsonReq = gson.toJson(req);
                    System.out.println(jsonReq);

                    String resJson = NetworkUtil.sendJson(socket, jsonReq, gson);
                    System.out.println("Recebido: " + resJson);

                    Resposta res = gson.fromJson(resJson, Resposta.class);

                    if (res != null && res.getStatus().equals("200")) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Senha alterada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE));
                    } else {
                        String msgErro = res != null ? res.getMensagem() : "Resposta inválida do servidor.";
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            msgErro, "Erro: " + (res != null ? res.getStatus() : "N/A"), JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Erro de comunicação ao alterar a senha: " + e.getMessage(), "Erro de Rede", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        }
    }

    private void mostrarUsuarioLogado() {
        String token = SessaoUsuario.getInstance().getToken();
        new Thread(() -> {
            if (token != null && !token.isEmpty()) {
                try {
                    
                    Requisicao req = new Requisicao("LISTAR_PROPRIO_USUARIO", token);
                    String reqJson = gson.toJson(req);
                    System.out.println(reqJson);
                    
                    String resJson = NetworkUtil.sendJson(socket, reqJson, gson);
                    System.out.println("Recebido: " + resJson);

                    Login res = gson.fromJson(resJson, Login.class);
                    //Object usuario = res.getUsuario();
                    // strUser = gson.toJson(usuario);
                    
                    //Usuario user = gson.fromJson(strUser, Usuario.class);
                    
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Usuário logado: " + res.getUsuario(), "Informações do Usuário",
                        JOptionPane.INFORMATION_MESSAGE));

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Não foi possível verificar os dados do usuário (token inválido).",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
        } else {
            JOptionPane.showMessageDialog(this, "Não há usuário logado na sessão.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
        }).start();;
        
    }

    // --- NOVO: Método para a ação do botão "Logout" ---
    private void fazerLogout() {

        new Thread(() -> {
            try {
                String token = SessaoUsuario.getInstance().getToken();
                Requisicao req = new Requisicao("LOGOUT", token);
                String reqJson = gson.toJson(req);

                String resJson = NetworkUtil.sendJson(socket, reqJson, gson);
                System.out.println("Recebido: " + resJson);
                Resposta res = gson.fromJson(resJson, Resposta.class);
                if (res.getStatus().equals("200")) {
                    SessaoUsuario.getInstance().limparSessao();
                    dispose();
                    SwingUtilities.invokeLater(() -> new TelaConexao().setVisible(true));
                } else {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        res.getMensagem(), "Erro: "+res.getStatus(), JOptionPane.ERROR_MESSAGE));
                }

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        e.toString(), "Erro ao fazer logout", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            buscarDados();
        }
    }

    private void buscarDados() {
        String token = SessaoUsuario.getInstance().getToken();

        Requisicao reqFilmes = new Requisicao("LISTAR_FILMES");
        // Requisicao reqReviews = new Requisicao("LISTAR_REVIEWS_USUARIO");
        String jsonReqFilmes = gson.toJson(reqFilmes);
        // String jsonReqReviews = gson.toJson(reqReviews);

        new Thread(() -> {
            try {
                // ATENÇÃO: Enviar duas requisições seguidas no mesmo socket desta forma pode
                // dar errado.
                // O servidor precisaria de uma lógica para saber onde uma resposta termina e a
                // outra começa.
                // Para simplificar, vamos fazer uma requisição de cada vez.

                // Busca Filmes
                String respostaJsonFilmes = NetworkUtil.sendJson(socket, jsonReqFilmes, gson);
                System.out.println("Recebido: " + respostaJsonFilmes);
                RespostaFilmes respostaFilmes = gson.fromJson(respostaJsonFilmes, RespostaFilmes.class);

                // Busca Reviews
                // String respostaJsonReviews = NetworkUtil.sendJson(socket, jsonReqReviews, gson);
                // System.out.println("Recebido: " + respostaJsonReviews);
                // Resposta respostaReviews = gson.fromJson(respostaJsonReviews, Resposta.class);

                // Processa as respostas
                if (respostaFilmes != null && "200".equals(respostaFilmes.getStatus())) {
                    List<Filme> filmesRecebidos = respostaFilmes.getFilmes();
                    SwingUtilities.invokeLater(() -> {
                        modeloListaFilmes.clear();
                        for (Filme filme : filmesRecebidos) {
                            modeloListaFilmes.addElement(filme);
                        }
                    });
                } // Adicione um 'else' para tratar erro de filmes se necessário

                // if (respostaReviews != null && "200".equals(respostaReviews.getStatus())) {
                //     String dadosJsonReviews = gson.toJson(respostaReviews.getDados());
                //     java.lang.reflect.Type tipoListaReviews = new TypeToken<List<Review>>() {
                //     }.getType();
                //     List<Review> reviewsRecebidas = gson.fromJson(dadosJsonReviews, tipoListaReviews);

                //     SwingUtilities.invokeLater(() -> {
                //         modeloListaReviews.clear();
                //         for (Review review : reviewsRecebidas) {
                //             modeloListaReviews.addElement(review);
                //         }
                //     });
                // } // Adicione um 'else' para tratar erro de reviews se necessário

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "Erro de comunicação: " + e.getMessage(), "Erro de Rede", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private void openReviewDialog(Filme filme) {
        SwingUtilities.invokeLater(() -> {
            ReviewDialog dlg = new ReviewDialog(this, filme, socket, gson);
            dlg.setVisible(true);
        });
    }
}

class RespostaFilmes {
    private String status;
    private List<Filme> filmes;

    public String getStatus() { return status; }
    public List<Filme> getFilmes() { return filmes; }
}