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

    // Componentes do formulário
    private JTextField txtTitulo;
    private JTextField txtDiretor;
    private JTextField txtAno;
    private JTextField txtGeneros;
    private JTextArea txtSinopse;

    public FilmeDialog(JFrame owner, Socket socket, Gson gson) {
        super(owner, "Criar Novo Filme", true); // true = modal
        this.socket = socket;
        this.gson = gson;

        setSize(600, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        add(criarPainelFormulario(), BorderLayout.CENTER);
        add(criarPainelBotoes(), BorderLayout.SOUTH);
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
        
        // Usamos o construtor apropriado da sua classe Filme
        Filme novoFilme = new Filme(titulo, ano, generosList, sinopse, diretor);

        // Envia para o servidor em uma thread separada
        new Thread(() -> {
            try {
                String token = SessaoUsuario.getInstance().getToken();
                Requisicao req = new Requisicao("CRIAR_FILME", novoFilme, token, "filme");
                String jsonReq = gson.toJson(req);

                String respostaJson = NetworkUtil.sendJson(socket, jsonReq, gson);
                Resposta resposta = gson.fromJson(respostaJson, Resposta.class);

                if (resposta != null && "201".equals(resposta.getStatus())) { // 201 = Created
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Filme criado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // Fecha o diálogo
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