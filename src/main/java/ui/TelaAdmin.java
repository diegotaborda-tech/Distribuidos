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

public class TelaAdmin extends JFrame{
	
	private Socket socket;
    private Gson gson = new Gson();
    
    private DefaultListModel<Filme> modeloListaFilmes;
    private DefaultListModel<Review> modeloListaReviews;
    private DefaultListModel<Usuario> modeloListaUsuarios;
    private JList<Filme> listaFilmesUI;
    private JList<Review> listaReviewsUI;
    private JList<Usuario> listaUsuariosUI;

    public TelaAdmin(Socket socket) {
    	super("Voteflix - Admin");
        this.socket = socket;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        // Garante que o JFrame use BorderLayout
        setLayout(new BorderLayout());

        JTabbedPane abas = new JTabbedPane();

        JPanel painelFilmes = new JPanel(new BorderLayout()); //
        modeloListaFilmes = new DefaultListModel<>();
        listaFilmesUI = new JList<>(modeloListaFilmes);
        listaFilmesUI.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        listaFilmesUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(listaFilmesUI);
        painelFilmes.add(scrollPane, BorderLayout.CENTER);

        JPanel acoesFilmesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDeletarFilme = new JButton("Deletar Filme Selecionado");
        btnDeletarFilme.setEnabled(false); // Começa desabilitado
        acoesFilmesPanel.add(btnDeletarFilme);
        painelFilmes.add(acoesFilmesPanel, BorderLayout.SOUTH);

        JPanel painelReviews = new JPanel(new BorderLayout());
        modeloListaReviews = new DefaultListModel<>();
        listaReviewsUI = new JList<>(modeloListaReviews);
        listaReviewsUI.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JScrollPane scrollPane2 = new JScrollPane(listaReviewsUI);
        painelReviews.add(scrollPane2, BorderLayout.CENTER);

        JPanel painelUsuarios = new JPanel(new BorderLayout());
        modeloListaUsuarios = new DefaultListModel<>();
        listaUsuariosUI = new JList<>(modeloListaUsuarios);
        listaUsuariosUI.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JScrollPane scrollPane3 = new JScrollPane(listaUsuariosUI);
        painelUsuarios.add(scrollPane3, BorderLayout.CENTER);

        abas.addTab("Filmes Cadastrados", painelFilmes);
        abas.addTab("Reviews Cadastradas", painelReviews);
        abas.addTab("Usuarios Cadastrados", painelUsuarios);
        add(abas, BorderLayout.CENTER);

        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCriarFilme = new JButton("Criar Novo Filme");
        btnCriarFilme.addActionListener(e -> abrirDialogoCriarFilme());
        painelAcoes.add(btnCriarFilme);
        add(painelAcoes, BorderLayout.SOUTH);

        listaFilmesUI.addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            btnDeletarFilme.setEnabled(listaFilmesUI.getSelectedIndex() != -1);
        }
        });

    // Ação do botão "Deletar"
        btnDeletarFilme.addActionListener(e -> deletarFilmeSelecionado());

        // modeloListaFilmes = new DefaultListModel<>();
        // modeloListaReviews = new DefaultListModel<>();
        // modeloListaUsuarios = new DefaultListModel<>();

        // listaFilmesUI = new JList<>(modeloListaFilmes);
        // listaReviewsUI = new JList<>(modeloListaReviews);
        // listaUsuariosUI = new JList<>(modeloListaUsuarios);

        // listaFilmesUI.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        // listaReviewsUI.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        // listaUsuariosUI.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // JScrollPane scrollPane = new JScrollPane(listaFilmesUI);
        // JScrollPane scrollPane2 = new JScrollPane(listaReviewsUI);
        // JScrollPane scrollPane3 = new JScrollPane(listaUsuariosUI);

        // listaFilmesUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	
        // painelFilmes.add(scrollPane, BorderLayout.CENTER);
        // painelReviews.add(scrollPane2, BorderLayout.CENTER);
        // painelUsuarios.add(scrollPane3, BorderLayout.CENTER);

        // abas.addTab("Filmes Cadastrados", painelFilmes);
        // abas.addTab("Reviews Cadastradas", painelReviews);
        // abas.addTab("Usuarios Cadastrados", painelUsuarios);

        // add(abas, BorderLayout.CENTER);
        
        // JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Alinhado à direita
        // JButton btnCriarFilme = new JButton("Criar Novo Filme");
        
        // btnCriarFilme.addActionListener(e -> abrirDialogoCriarFilme());
        
        // painelAcoes.add(btnCriarFilme);
        // add(painelAcoes, BorderLayout.SOUTH);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            buscarDadosADM();
        }
    }

    // Em ui/TelaAdmin.java

private void deletarFilmeSelecionado() {
    Filme filmeParaDeletar = listaFilmesUI.getSelectedValue();
    if (filmeParaDeletar == null) {
        return; // Nada selecionado
    }

    // Pede confirmação
    int confirmacao = JOptionPane.showConfirmDialog(this,
            "Tem certeza que quer deletar o filme: '" + filmeParaDeletar.getTitulo() + "'?\n"
            + "TODAS as reviews associadas a ele também serão apagadas PERMANENTEMENTE.",
            "Confirmar Exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

    if (confirmacao != JOptionPane.YES_OPTION) {
        return;
    }

    // Envia a requisição em uma nova thread
    new Thread(() -> {
        try {
            String token = SessaoUsuario.getInstance().getToken();
            
            // O protocolo
            // define "ADMIN_DELETAR_FILME". Vamos enviar o ID dentro do payload 'dados'.
            Requisicao req = new Requisicao("EXCLUIR_FILME", token, filmeParaDeletar.getId());//TODO ajustar envio
            String jsonReq = gson.toJson(req);

            String respostaJson = NetworkUtil.sendJson(socket, jsonReq, gson);
            Resposta resposta = gson.fromJson(respostaJson, Resposta.class);

            if (resposta != null && "200".equals(resposta.getStatus())) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Filme deletado com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    // Atualiza as listas na tela
                    buscarDadosADM(); 
                });
            } else {
                String msgErro = resposta != null ? resposta.getMensagem() : "Resposta inválida.";
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msgErro, "Erro ao Deletar", JOptionPane.ERROR_MESSAGE));
            }

        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro de Rede", JOptionPane.ERROR_MESSAGE));
        }
    }).start();
}

    private void abrirDialogoCriarFilme() {
        
        FilmeDialog dialog = new FilmeDialog(this, socket, gson);
        dialog.setVisible(true); 

        buscarDadosADM(); 
    }

    private void buscarDadosADM() {
        String token = SessaoUsuario.getInstance().getToken();

        new Thread(() -> {
            try{
                Requisicao req = new Requisicao("LISTAR_USUARIOS",token);
                String jsonReq = gson.toJson(req);
                String respostaJson = NetworkUtil.sendJson(socket, jsonReq, gson);
                System.out.println("Recebido: " + respostaJson);

                Resposta resposta = gson.fromJson(respostaJson, Resposta.class);
                
                if (resposta != null && "200".equals(resposta.getStatus())) {
                    String dadosJson = gson.toJson(resposta.getListaUsuarios());
                    java.lang.reflect.Type tipoListaUsuarios = new TypeToken<List<Usuario>>() {}.getType();
                    List<Usuario> usuariosRecebidos = gson.fromJson(dadosJson, tipoListaUsuarios);
                    
                    SwingUtilities.invokeLater(() -> {
                            modeloListaUsuarios.clear();
                            for (Usuario usuario : usuariosRecebidos) {
                                modeloListaUsuarios.addElement(usuario);
                            }
                        });
                }else {
                String msgErro = resposta != null ? resposta.getMensagem() : "Resposta inválida do servidor.";
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msgErro, "Erro ao Carregar Usuários", JOptionPane.ERROR_MESSAGE));
            }
            } catch (Exception e) {
                e.printStackTrace(); 
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro de Rede", JOptionPane.ERROR_MESSAGE));
            }

            try {
            Requisicao reqFilmes = new Requisicao("LISTAR_FILMES", token);
            String jsonReq = gson.toJson(reqFilmes);
            String respostaJson = NetworkUtil.sendJson(socket, jsonReq, gson);
            System.out.println("Recebido (Filmes): " + respostaJson);
            
            RespostaFilmes respostaFilmes = gson.fromJson(respostaJson, RespostaFilmes.class);
            
            if (respostaFilmes != null && "200".equals(respostaFilmes.getStatus())) {
                    List<Filme> filmesRecebidos = respostaFilmes.getFilmes();
                    SwingUtilities.invokeLater(() -> {
                        modeloListaFilmes.clear();
                        for (Filme filme : filmesRecebidos) {
                            modeloListaFilmes.addElement(filme);
                        }
                    });
                } else {
                String msgErro = respostaFilmes != null ? respostaFilmes.getMensagem() : "Resposta inválida (Filmes).";
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msgErro, "Erro ao Carregar Filmes", JOptionPane.ERROR_MESSAGE));
            }
        } catch (Exception e) {
            e.printStackTrace(); 
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Erro de comunicação (Filmes): " + e.getMessage(), "Erro de Rede", JOptionPane.ERROR_MESSAGE));
        }

        try {
            // O seu protocolo
            // define "LISTAR_TODAS_REVIEWS" para admin.
            Requisicao reqReviews = new Requisicao("LISTAR_TODAS_REVIEWS", token);
            String jsonReq = gson.toJson(reqReviews);
            String respostaJson = NetworkUtil.sendJson(socket, jsonReq, gson);
            System.out.println("Recebido (Reviews): " + respostaJson);

            Resposta resposta = gson.fromJson(respostaJson, Resposta.class);
            
            if (resposta != null && "200".equals(resposta.getStatus())) {
                String dadosJson = gson.toJson(resposta.getDados());
                // O método listarReviews() em Querys.java retorna List<Review>
                java.lang.reflect.Type tipoListaReviews = new TypeToken<List<Review>>() {}.getType();
                List<Review> reviewsRecebidas = gson.fromJson(dadosJson, tipoListaReviews);
                
                SwingUtilities.invokeLater(() -> {
                        modeloListaReviews.clear();
                        for (Review review : reviewsRecebidas) {
                            modeloListaReviews.addElement(review);
                        }
                    });
            } else {
                String msgErro = resposta != null ? resposta.getMensagem() : "Resposta inválida (Reviews).";
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msgErro, "Erro ao Carregar Reviews", JOptionPane.ERROR_MESSAGE));
            }
        } catch (Exception e) {
            e.printStackTrace(); 
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Erro de comunicação (Reviews): " + e.getMessage(), "Erro de Rede", JOptionPane.ERROR_MESSAGE));
        }

        }).start();
    }
}

class RespostaFilmes {
private String status;
private String mensagem;
private List<Filme> filmes;

public String getStatus() { return status; }
public String getMensagem() { return mensagem; }
public List<Filme> getFilmes() { return filmes; }
}
