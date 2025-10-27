package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class TelaConexao extends JFrame {

    private JTextField campoIP;
    private JTextField campoPorta;
    private JButton botaoConectar;

    public TelaConexao() {
        super("Conectar ao Servidor");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Componentes da UI
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("IP do Servidor:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        campoIP = new JTextField("127.0.0.1", 15);
        add(campoIP, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Porta:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        campoPorta = new JTextField("20000", 15);
        add(campoPorta, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        botaoConectar = new JButton("Conectar");
        add(botaoConectar, gbc);

        // Ação do botão
        botaoConectar.addActionListener(e -> tentarConexao());
    }

    private void tentarConexao() {
        String ip = campoIP.getText().trim();
        String portaStr = campoPorta.getText().trim();

        if (ip.isEmpty() || portaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "IP e Porta são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int porta = Integer.parseInt(portaStr);
            botaoConectar.setEnabled(false);
            botaoConectar.setText("Conectando...");

            // A tentativa de conexão DEVE ser em uma thread separada
            new Thread(() -> {
                try {
                    // Tenta criar a conexão
                    Socket socket = new Socket(ip, porta);
                    
                    // Se conseguiu, fecha esta tela e abre a de login na thread da UI
                    SwingUtilities.invokeLater(() -> {
                        dispose(); // Fecha a tela de conexão
                        new TelaLogin(socket).setVisible(true); // Abre a tela de login, passando o socket
                    });

                } catch (IOException ex) {
                    // Se falhou, mostra o erro na thread da UI
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Falha ao conectar: " + ex.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
                        botaoConectar.setEnabled(true);
                        botaoConectar.setText("Conectar");
                    });
                }
            }).start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "A porta deve ser um número válido.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaConexao().setVisible(true));
    }
}