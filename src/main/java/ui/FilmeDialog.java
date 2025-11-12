package ui;

import com.google.gson.Gson;
import model.Filme;
import model.NetworkUtil;
import model.Requisicao;
import model.Resposta;
import model.SessaoUsuario;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Um JDialog modal para criar um novo filme.
 */
public class FilmeDialog extends JDialog {

    private final Socket socket;
    private final Gson gson;
    private final Filme filmeParaEditar; // null se for criação, não-null se for edição

    // Componentes do formulário
    private JTextField txtTitulo;
    private JTextField txtDiretor;
    private JTextField txtAno;
    private JTextField txtGeneros;
    private JTextArea txtSinopse;

    // Construtor para criar novo filme
    public FilmeDialog(JFrame owner, Socket socket, Gson gson) {
        this(owner, socket, gson, null);
    }

    // Construtor para editar filme existente
    public FilmeDialog(JFrame owner, Socket socket, Gson gson, Filme filmeParaEditar) {
        super(owner, filmeParaEditar == null ? "Criar Novo Filme" : "Editar Filme", true); // true = modal
        this.socket = socket;
        this.gson = gson;
        this.filmeParaEditar = filmeParaEditar;

        setSize(600, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        add(criarPainelFormulario(), BorderLayout.CENTER);
        add(criarPainelBotoes(), BorderLayout.SOUTH);

        // Se estiver editando, preenche os campos
        if (filmeParaEditar != null) {
            preencherCampos();
        }
    }

    /**
     * Cria o painel principal com o formulário usando GridBagLayout para alinhamento.
     */
    private JPanel criarPainelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(5, 5, 5, 5); // Espaçamento
        gbc.anchor = GridBagConstraints.WEST; // Alinhar labels à esquerda
        
        // Linha 1: Título
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Título:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtTitulo = new JTextField(30);
        panel.add(txtTitulo, gbc);
        
        // Linha 2: Diretor
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Diretor:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtDiretor = new JTextField();
        panel.add(txtDiretor, gbc);

        // Linha 3: Ano
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Ano:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtAno = new JTextField();
        panel.add(txtAno, gbc);
        
        // Linha 4: Gêneros
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Gêneros (separados por vírgula):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtGeneros = new JTextField();
        panel.add(txtGeneros, gbc);

        // Linha 5: Sinopse
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Sinopse:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; // Ocupa espaço vertical
        txtSinopse = new JTextArea(5, 30);
        txtSinopse.setLineWrap(true);
        txtSinopse.setWrapStyleWord(true);
        JScrollPane scrollSinopse = new JScrollPane(txtSinopse);
        panel.add(scrollSinopse, gbc);
        
        return panel;
    }

    /**
     * Cria o painel inferior com os botões Salvar e Cancelar.
     */
    private JPanel criarPainelBotoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");

        btnCancelar.addActionListener(e -> dispose()); // Fecha a janela
        btnSalvar.addActionListener(e -> salvarFilme());

        panel.add(btnCancelar);
        panel.add(btnSalvar);
        return panel;
    }

    /**
     * Preenche os campos do formulário com os dados do filme a ser editado.
     */
    private void preencherCampos() {
        if (filmeParaEditar != null) {
            txtTitulo.setText(filmeParaEditar.getTitulo());
            txtDiretor.setText(filmeParaEditar.getDiretor());
            txtAno.setText(filmeParaEditar.getAno());
            
            // Converte a lista de gêneros em string separada por vírgulas
            if (filmeParaEditar.getGenero() != null) {
                String generosStr = String.join(", ", filmeParaEditar.getGenero());
                txtGeneros.setText(generosStr);
            }
            
            txtSinopse.setText(filmeParaEditar.getSinopse());
        }
    }

    /**
     * Valida os dados, constrói o objeto Filme e o envia para o servidor.
     */
    private void salvarFilme() {
        // Validação simples
        String titulo = txtTitulo.getText().trim();
        String ano = txtAno.getText().trim();
        String sinopse = txtSinopse.getText().trim();
        String diretor = txtDiretor.getText().trim();
        String generosStr = txtGeneros.getText().trim();

        if (titulo.isEmpty() || ano.isEmpty() || sinopse.isEmpty() || diretor.isEmpty() || generosStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos são obrigatórios.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Converte a string de gêneros "A, B, C" em uma List<String>
        List<String> generosList = Arrays.stream(generosStr.split(","))
                                         .map(String::trim)
                                         .collect(Collectors.toList());
        
        // Se estiver editando, usa o ID existente; senão, cria novo filme
        if (filmeParaEditar != null) {
            // Modo edição
            editarFilmeNoServidor(titulo, diretor, ano, generosList, sinopse);
        } else {
            // Modo criação
            criarFilmeNoServidor(titulo, ano, generosList, sinopse, diretor);
        }
    }

    /**
     * Cria um novo filme no servidor.
     */
    private void criarFilmeNoServidor(String titulo, String ano, List<String> generosList, String sinopse, String diretor) {
        // Usamos o construtor apropriado da sua classe Filme
        Filme novoFilme = new Filme(titulo, ano, generosList, sinopse, diretor);

        // Envia para o servidor em uma thread separada
        new Thread(() -> {
            try {
                String token = SessaoUsuario.getInstance().getToken();
                Requisicao req = new Requisicao("CRIAR_FILME", novoFilme, token, "filme");
                String jsonReq = gson.toJson(req);

                System.out.println("=== REQUISIÇÃO ENVIADA (CRIAR_FILME) ===");
                System.out.println(jsonReq);
                System.out.println("=========================================");

                String respostaJson = NetworkUtil.sendJson(socket, jsonReq, gson);
                
                System.out.println("=== RESPOSTA RECEBIDA (CRIAR_FILME) ===");
                System.out.println(respostaJson);
                System.out.println("========================================");
                
                Resposta resposta = gson.fromJson(respostaJson, Resposta.class);

                if (resposta != null && "201".equals(resposta.getStatus())) { // 201 = Created
                    SwingUtilities.invokeLater(() -> {
                        dispose(); // Fecha o diálogo primeiro
                        java.awt.Window parent = getOwner();
                        JOptionPane.showMessageDialog(parent != null ? parent : null, "Filme criado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    });
                } else {
                    String msgErro = resposta != null ? resposta.getMensagem() : "Resposta inválida.";
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msgErro, "Erro do Servidor", JOptionPane.ERROR_MESSAGE));
                }

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro de Rede", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    /**
     * Edita um filme existente no servidor.
     */
    private void editarFilmeNoServidor(String titulo, String diretor, String ano, List<String> generosList, String sinopse) {
        // Envia para o servidor em uma thread separada
        new Thread(() -> {
            try {
                String token = SessaoUsuario.getInstance().getToken();
                
                // Cria o objeto filme com todos os dados atualizados
                Filme filmeAtualizado = new Filme(
                    filmeParaEditar.getId(),
                    titulo,
                    ano,
                    generosList,
                    sinopse,
                    filmeParaEditar.getNota(),
                    filmeParaEditar.getQtd_avaliacoes(),
                    diretor
                );
                
                // Cria a requisição no formato esperado pelo servidor
                Requisicao req = new Requisicao("EDITAR_FILME", filmeAtualizado, token, "filme");
                String jsonReq = gson.toJson(req);

                System.out.println("=== REQUISIÇÃO ENVIADA (EDITAR_FILME) ===");
                System.out.println(jsonReq);
                System.out.println("==========================================");

                String respostaJson = NetworkUtil.sendJson(socket, jsonReq, gson);
                
                System.out.println("=== RESPOSTA RECEBIDA (EDITAR_FILME) ===");
                System.out.println(respostaJson);
                System.out.println("=========================================");
                
                Resposta resposta = gson.fromJson(respostaJson, Resposta.class);

                if (resposta != null && "200".equals(resposta.getStatus())) {
                    SwingUtilities.invokeLater(() -> {
                        dispose(); // Fecha o diálogo primeiro
                        java.awt.Window parent = getOwner();
                        JOptionPane.showMessageDialog(parent != null ? parent : null, "Filme editado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    });
                } else {
                    String msgErro = resposta != null ? resposta.getMensagem() : "Resposta inválida.";
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msgErro, "Erro do Servidor", JOptionPane.ERROR_MESSAGE));
                }

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro de Rede", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }
}