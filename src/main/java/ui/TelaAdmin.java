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
    private DefaultListModel<Review> modeloListaUsuarios;
    private JList<Filme> listaFilmesUI;
    private JList<Review> listaReviewsUI;
    private JList<Review> listaUsuariosUI;
    
    public TelaAdmin(Socket socket) {
    	super("Voteflix - Admin");
        this.socket = socket;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        // Garante que o JFrame use BorderLayout
        setLayout(new BorderLayout());

        JTabbedPane abas = new JTabbedPane();

        JPanel painelFilmes = new JPanel(new BorderLayout());
        JPanel painelReviews = new JPanel(new BorderLayout());
        JPanel painelUsuarios = new JPanel(new BorderLayout());

        modeloListaFilmes = new DefaultListModel<>();
        modeloListaReviews = new DefaultListModel<>();
        modeloListaUsuarios = new DefaultListModel<>();

        listaFilmesUI = new JList<>(modeloListaFilmes);
        listaReviewsUI = new JList<>(modeloListaReviews);
        listaUsuariosUI = new JList<>(modeloListaUsuarios);

        listaFilmesUI.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        listaReviewsUI.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        listaUsuariosUI.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JScrollPane scrollPane = new JScrollPane(listaFilmesUI);
        JScrollPane scrollPane2 = new JScrollPane(listaReviewsUI);
        JScrollPane scrollPane3 = new JScrollPane(listaUsuariosUI);

        listaFilmesUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	
        painelFilmes.add(scrollPane, BorderLayout.CENTER);
        painelReviews.add(scrollPane2, BorderLayout.CENTER);
        painelUsuarios.add(scrollPane3, BorderLayout.CENTER);

        abas.addTab("Filmes Cadastrados", painelFilmes);
        abas.addTab("Reviews Cadastradas", painelReviews);
        abas.addTab("Usuarios Cadastrados", painelUsuarios);

        add(abas, BorderLayout.CENTER);
        
    }
}
