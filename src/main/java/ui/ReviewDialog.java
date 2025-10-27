package ui;

import model.Filme;
import model.NetworkUtil;
import model.Requisicao;
import model.Resposta;
import model.Review;
import model.SessaoUsuario;

import com.google.gson.Gson;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.Socket;

public class ReviewDialog extends JDialog {
    private final Filme filme;
    private final Socket socket;
    private final Gson gson;

    private JTextArea txtReview;
    private JSpinner spinnerRating;
    private JTextField txtTitulo; // NOVO: Campo de texto para o título

    public ReviewDialog(JFrame owner, Filme filme, Socket socket, Gson gson) {
        super(owner, "Review - " + filme.getTitulo(), true);
        this.filme = filme;
        this.socket = socket;
        this.gson = gson;

        initComponents();
        setSize(500, 400);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));

        // Topo: Título do filme
        JLabel lblTitle = new JLabel(filme.getTitulo() + " (" + filme.getAno() + ")");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        add(lblTitle, BorderLayout.NORTH);

        // Centro: sinopse, título da review e texto da review
        JPanel centerPanel = new JPanel(new BorderLayout(6, 6));

        JTextArea txtSinopse = new JTextArea(filme.getSinopse());
        txtSinopse.setLineWrap(true);
        txtSinopse.setWrapStyleWord(true);
        txtSinopse.setEditable(false);
        txtSinopse.setBackground(getBackground());
        txtSinopse.setRows(4);
        JScrollPane sinopseScroll = new JScrollPane(txtSinopse);
        sinopseScroll.setBorder(BorderFactory.createTitledBorder("Sinopse"));
        
        // NOVO: Painel para os campos de input da review (título e texto)
        JPanel reviewInputPanel = new JPanel(new BorderLayout(6, 6));

        // NOVO: Painel para o título da review
        JPanel titlePanel = new JPanel(new BorderLayout(4, 4));
        titlePanel.add(new JLabel("Título da Review:"), BorderLayout.WEST);
        txtTitulo = new JTextField();
        titlePanel.add(txtTitulo, BorderLayout.CENTER);
        reviewInputPanel.add(titlePanel, BorderLayout.NORTH);

        // Área de texto principal da review
        txtReview = new JTextArea();
        txtReview.setLineWrap(true);
        txtReview.setWrapStyleWord(true);
        JScrollPane reviewScroll = new JScrollPane(txtReview);
        reviewScroll.setBorder(BorderFactory.createTitledBorder("Sua Review"));
        reviewInputPanel.add(reviewScroll, BorderLayout.CENTER); // Adiciona ao painel de input

        centerPanel.add(sinopseScroll, BorderLayout.NORTH);
        centerPanel.add(reviewInputPanel, BorderLayout.CENTER); // Adiciona o painel de input ao centro
        add(centerPanel, BorderLayout.CENTER);

        // Rodapé: nota e botões
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        spinnerRating = new JSpinner(new SpinnerNumberModel(5.0, 0.0, 10.0, 0.5));
        spinnerRating.setPreferredSize(new Dimension(80, spinnerRating.getPreferredSize().height));
        bottom.add(new JLabel("Nota:"));
        bottom.add(spinnerRating);

        JButton btnSubmit = new JButton("Enviar");
        btnSubmit.addActionListener(this::onSubmit);
        JButton btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());

        bottom.add(btnCancel);
        bottom.add(btnSubmit);
        add(bottom, BorderLayout.SOUTH);
    }

    private void onSubmit(ActionEvent e) {
        String reviewText = txtReview.getText().trim();
        String rating = spinnerRating.getValue().toString();
        String titulo = txtTitulo.getText().trim();


        new Thread(() -> {
            if (reviewText.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "A review não pode ser vazia.", "Erro", JOptionPane.ERROR_MESSAGE);
                });
            //int res = JOptionPane.showConfirmDialog(this, "A review está vazia. Deseja enviar mesmo assim?", "Confirmar", JOptionPane.YES_NO_OPTION);
            }

            // Build an object to send to the server (example)
            //ReviewPayload payload = new ReviewPayload(filme.getId(), reviewText, rating);
            Review rev = new Review(filme.getId(), titulo, reviewText, rating);
            String token = SessaoUsuario.getInstance().getToken();
            Requisicao req = new Requisicao("CRIAR_REVIEW",rev, token,"review");
            // System.out.println("ReviewDialog:");
            // System.out.println("ID Filme: " + rev.getId_filme());
            // System.out.println("Nota: " + rev.getNota());
            // System.out.println("Título: " + rev.getTitulo());
            // System.out.println("Descrição: " + rev.getDescricao());

            // If you have socket + gson, send it; otherwise just print or store locally.
            if (socket != null && gson != null) {
                // Example: send JSON to server (you probably have a NetworkUtil; adapt as necessary)
                try {
                    String json = gson.toJson(req);
                    System.out.println("JSON: "+json);
                    String jsonResponse;
                    Resposta res;

                    jsonResponse = NetworkUtil.sendJson(socket, json, gson);
                    res = gson.fromJson(jsonResponse, Resposta.class);

                    
                        if (res.getStatus().equals("201")) {
                            
                            SwingUtilities.invokeLater(() -> 
                                JOptionPane.showMessageDialog(this, "Review enviada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE));
                            limparCampos();

                        } else {
                            SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(this, "Erro ao enviar review: " + res.getMensagem(), "Erro", JOptionPane.ERROR_MESSAGE));
                        }
                    
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao enviar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // no network: just print to console or show a message
                //System.out.println("Review payload: " + gson.toJson(req));
                JOptionPane.showMessageDialog(this, "Review criada localmente.", "OK", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });
    }

    // simple payload class (can be moved to model package)
    // Dentro da classe ui/TelaLogin.java

    private void limparCampos() {
        
        txtTitulo.setText("");
        txtReview.setText("");
        spinnerRating.setValue(5);
    }
}
