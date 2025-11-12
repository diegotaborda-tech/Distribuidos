package ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class TelaAdmin extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Socket socket;
    private final Gson gson = new Gson();

    private final DefaultListModel<Filme> modeloListaFilmes = new DefaultListModel<>();
    private final DefaultListModel<Usuario> modeloListaUsuarios = new DefaultListModel<>();
    private final DefaultListModel<Review> modeloListaReviews = new DefaultListModel<>(); // manter tab, requisição comentada

    private final JList<Filme> listaFilmesUI = new JList<>(modeloListaFilmes);
    private final JList<Usuario> listaUsuariosUI = new JList<>(modeloListaUsuarios);
    private final JList<Review> listaReviewsUI = new JList<>(modeloListaReviews);

    private final JButton btnEditarFilme = new JButton("Editar Filme Selecionado");
    private final JButton btnDeletarFilme = new JButton("Deletar Filme Selecionado");

    public TelaAdmin(Socket socket) {
        super("Voteflix - Admin");
        this.socket = socket;
        configurarUI();
    }

    private void configurarUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Tabs
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Filmes", criarPainelFilmes());
        abas.addTab("Reviews", criarPainelReviews()); // conteúdo estático por enquanto
        abas.addTab("Usuários", criarPainelUsuarios());
        add(abas, BorderLayout.CENTER);

        // Ações globais
        JPanel acoesGlobais = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCriarFilme = new JButton("Criar Novo Filme");
        btnCriarFilme.addActionListener(e -> abrirDialogoCriarFilme());
        acoesGlobais.add(btnCriarFilme);
        add(acoesGlobais, BorderLayout.SOUTH);

        // Listeners seleção
        listaFilmesUI.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean selected = listaFilmesUI.getSelectedIndex() >= 0;
                btnEditarFilme.setEnabled(selected);
                btnDeletarFilme.setEnabled(selected);
            }
        });

        // Actions
        btnEditarFilme.addActionListener(e -> editarFilmeSelecionado());
        btnDeletarFilme.addActionListener(e -> deletarFilmeSelecionado());
    }

    private JPanel criarPainelFilmes() {
        JPanel painel = new JPanel(new BorderLayout());
        listaFilmesUI.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        listaFilmesUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        painel.add(new JScrollPane(listaFilmesUI), BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEditarFilme.setEnabled(false);
        btnDeletarFilme.setEnabled(false);
        botoes.add(btnEditarFilme);
        botoes.add(btnDeletarFilme);
        painel.add(botoes, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel criarPainelUsuarios() {
        JPanel painel = new JPanel(new BorderLayout());
        listaUsuariosUI.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        painel.add(new JScrollPane(listaUsuariosUI), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelReviews() {
        JPanel painel = new JPanel(new BorderLayout());
        listaReviewsUI.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        painel.add(new JScrollPane(listaReviewsUI), BorderLayout.CENTER);
        JLabel info = new JLabel("Carregamento de reviews desativado (requisições comentadas)");
        info.setHorizontalAlignment(SwingConstants.CENTER);
        painel.add(info, BorderLayout.SOUTH);
        return painel;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            carregarDados();
        }
    }

    private void abrirDialogoCriarFilme() {
        FilmeDialog dialog = new FilmeDialog(this, socket, gson);
        dialog.setVisible(true);
        carregarDados();
    }

    private void editarFilmeSelecionado() {
        Filme selecionado = listaFilmesUI.getSelectedValue();
        if (selecionado == null) return;
        FilmeDialog dialog = new FilmeDialog(this, socket, gson, selecionado);
        dialog.setVisible(true);
        carregarDados();
    }

    private void deletarFilmeSelecionado() {
        Filme filme = listaFilmesUI.getSelectedValue();
        if (filme == null) return;
        int opt = JOptionPane.showConfirmDialog(this,
                "Confirmar exclusão do filme: '" + filme.getTitulo() + "'?", "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;

        new Thread(() -> {
            try {
                String token = SessaoUsuario.getInstance().getToken();
                if (token == null || token.isEmpty()) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Sessão inválida.", "Erro", JOptionPane.ERROR_MESSAGE));
                    return;
                }
                Requisicao req = new Requisicao("EXCLUIR_FILME", token, filme.getId());
                String reqJson = gson.toJson(req);
                log("REQ EXCLUIR_FILME", reqJson);
                String respJson = NetworkUtil.sendJson(socket, reqJson, gson);
                log("RESP EXCLUIR_FILME", respJson);
                Resposta resposta = gson.fromJson(respJson, Resposta.class);
                SwingUtilities.invokeLater(() -> {
                    if (resposta != null && "200".equals(resposta.getStatus())) {
                        JOptionPane.showMessageDialog(this, "Filme deletado.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        carregarDados();
                    } else {
                        String msg = resposta != null ? resposta.getMensagem() : "Resposta inválida.";
                        JOptionPane.showMessageDialog(this, msg, "Erro ao deletar", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Falha de comunicação: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private void carregarDados() {
        String token = SessaoUsuario.getInstance().getToken();
        if (token == null || token.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Token ausente. Faça login novamente.", "Sessão Expirada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            carregarUsuarios(token);
            carregarFilmes(token);
            // carregarReviews(token); // REQUISIÇÃO COMENTADA POR SOLICITAÇÃO
        }).start();
    }

    private void carregarUsuarios(String token) {
        try {
            Requisicao req = new Requisicao("LISTAR_USUARIOS", token);
            String reqJson = gson.toJson(req);
            log("REQ LISTAR_USUARIOS", reqJson);
            String respJson = NetworkUtil.sendJson(socket, reqJson, gson);
            log("RESP LISTAR_USUARIOS", respJson);
            Resposta resposta = gson.fromJson(respJson, Resposta.class);
            if (resposta != null && "200".equals(resposta.getStatus())) {
                String dadosJson = gson.toJson(resposta.getListaUsuarios());
                java.lang.reflect.Type tipoListaUsuarios = new TypeToken<List<Usuario>>() {}.getType();
                List<Usuario> usuarios = gson.fromJson(dadosJson, tipoListaUsuarios);
                if (usuarios == null) usuarios = Collections.emptyList();
                final List<Usuario> usuariosFinal = usuarios;
                SwingUtilities.invokeLater(() -> {
                    modeloListaUsuarios.clear();
                    usuariosFinal.forEach(modeloListaUsuarios::addElement);
                });
            } else {
                String msg = resposta != null ? resposta.getMensagem() : "Resposta inválida usuários.";
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Erro Usuários", JOptionPane.ERROR_MESSAGE));
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Erro usuários: " + e.getMessage(), "Rede", JOptionPane.ERROR_MESSAGE));
        }
    }

    private void carregarFilmes(String token) {
        try {
            Requisicao req = new Requisicao("LISTAR_FILMES", token);
            String reqJson = gson.toJson(req);
            log("REQ LISTAR_FILMES", reqJson);
            String respJson = NetworkUtil.sendJson(socket, reqJson, gson);
            log("RESP LISTAR_FILMES", respJson);
            RespostaFilmes resposta = gson.fromJson(respJson, RespostaFilmes.class);
            if (resposta != null && "200".equals(resposta.getStatus())) {
                List<Filme> filmes = resposta.getFilmes();
                if (filmes == null) filmes = Collections.emptyList();
                final List<Filme> filmesFinal = filmes;
                SwingUtilities.invokeLater(() -> {
                    modeloListaFilmes.clear();
                    filmesFinal.forEach(modeloListaFilmes::addElement);
                });
            } else {
                String msg = resposta != null ? resposta.getMensagem() : "Resposta inválida filmes.";
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Erro Filmes", JOptionPane.ERROR_MESSAGE));
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Erro filmes: " + e.getMessage(), "Rede", JOptionPane.ERROR_MESSAGE));
        }
    }

    // private void carregarReviews(String token) {
    //     try {
    //         Requisicao req = new Requisicao("LISTAR_TODAS_REVIEWS", token);
    //         String reqJson = gson.toJson(req);
    //         log("REQ LISTAR_TODAS_REVIEWS", reqJson);
    //         String respJson = NetworkUtil.sendJson(socket, reqJson, gson);
    //         log("RESP LISTAR_TODAS_REVIEWS", respJson);
    //         Resposta resposta = gson.fromJson(respJson, Resposta.class);
    //         if (resposta != null && "200".equals(resposta.getStatus())) {
    //             String dadosJson = gson.toJson(resposta.getDados());
    //             java.lang.reflect.Type tipoListaReviews = new TypeToken<List<Review>>() {}.getType();
    //             List<Review> reviews = gson.fromJson(dadosJson, tipoListaReviews);
    //             if (reviews == null) reviews = Collections.emptyList();
    //             final List<Review> reviewsFinal = reviews;
    //             SwingUtilities.invokeLater(() -> {
    //                 modeloListaReviews.clear();
    //                 reviewsFinal.forEach(modeloListaReviews::addElement);
    //             });
    //         } else {
    //             String msg = resposta != null ? resposta.getMensagem() : "Resposta inválida reviews.";
    //             SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Erro Reviews", JOptionPane.ERROR_MESSAGE));
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Erro reviews: " + e.getMessage(), "Rede", JOptionPane.ERROR_MESSAGE));
    //     }
    // }

    private void log(String titulo, String conteudo) {
        System.out.println("=== " + titulo + " ===\n" + conteudo + "\n" + repeatChar('=', 6 + titulo.length()));
    }

    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }

    // Classe auxiliar para desserialização de filmes
    static class RespostaFilmes {
        private String status;
        private String mensagem;
        private List<Filme> filmes;
        public String getStatus() { return status; }
        public String getMensagem() { return mensagem; }
        public List<Filme> getFilmes() { return filmes; }
    }
}
